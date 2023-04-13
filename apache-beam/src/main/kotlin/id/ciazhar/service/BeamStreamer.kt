package id.ciazhar.service

import com.github.javafaker.Faker
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import id.ciazhar.config.KafkaConfig
import id.ciazhar.config.KafkaTopic
import id.ciazhar.config.ScyllaConfig
import id.ciazhar.helper.readFromKafka
import id.ciazhar.helper.writeToKafka
import id.ciazhar.helper.writeToScylla
import id.ciazhar.model.Person
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.io.kafka.KafkaIO
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.sdk.transforms.Create
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.GroupByKey
import org.apache.beam.sdk.transforms.PTransform
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.transforms.Values
import org.apache.beam.sdk.transforms.windowing.FixedWindows
import org.apache.beam.sdk.transforms.windowing.Window
import org.apache.beam.sdk.values.KV
import org.apache.beam.sdk.values.PBegin
import org.apache.beam.sdk.values.PCollection
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.joda.time.Duration
import java.util.*

val kafkaConfig = KafkaConfig(
    port = 9092,
    host = "localhost"
)

val scyllaConfig = ScyllaConfig(
    port = 9042,
    hosts = listOf("localhost"),
    username = "",
    password = "",
    keyspace = "test"
)

fun generateDataToKafka() {
    val faker = Faker()
    val people: MutableList<KV<String, String>> = ArrayList()
    val gson = Gson()

    //generate fake data
    for (i in 1..100) {
        val person = Person(
            id = faker.internet().uuid(),
            name = faker.name().fullName(),
            age = faker.number().numberBetween(10, 100),
            race = faker.demographic().race()
        )
        people.add(KV.of(person.id, gson.toJson(person)))
    }

    //create pipeline
    val options = PipelineOptionsFactory.create()
    val p = Pipeline.create(options)
    p.apply(Create.of(people))
        .apply(writeToKafka(kafkaConfig, KafkaTopic.PERSON))

    //run pipeline
    p.run().waitUntilFinish()
}

fun streamDataFromKafka() {
    val options = PipelineOptionsFactory.create()
    val standardMinutes = Duration.standardMinutes(1)
    val pipeline = Pipeline.create(options)

    //base pipeline
    val base = pipeline.apply(readFromKafka(kafkaConfig, KafkaTopic.PERSON))
        .apply(Window.into(FixedWindows.of(standardMinutes)))
        .apply(Values.create())
        .apply(ParDo.of(MapStringToPerson()))

    //write to scylla
//    base.apply(writeToScylla(scyllaConfig)) --> still getting error guava

    //stream to kafka
    base.apply(ParDo.of(MapPersonToKV()))
//        .apply(GroupByKey.create())
//        .apply(ParDo.of(MapKVToKV2())) --> still getting error mapping data
        .apply(writeToKafka(kafkaConfig, KafkaTopic.PERSON_BY_RACE))

    //run pipeline
    val result = pipeline.run()
    try {
        result.waitUntilFinish()
    } catch (exc: Exception) {
        result.cancel()
    }
}

class MapStringToPerson : DoFn<String?, Person?>() {
    @ProcessElement
    fun processElement(c: ProcessContext) {
        val gson = GsonBuilder()
            .registerTypeAdapter(
                Date::class.java,
                JsonDeserializer { json, _, _ -> Date(json.asJsonPrimitive.asLong) } as JsonDeserializer<*>?)
            .registerTypeAdapter(
                Date::class.java,
                JsonSerializer<Date> { date, _, _ -> JsonPrimitive(date.time) } as JsonSerializer<*>?)
            .create()

        val event = gson.fromJson(c.element(), Person::class.java)

        c.output(event)
    }
}

class MapPersonToKV : DoFn<Person, KV<String, String>>() {
    @ProcessElement
    fun processElement(c: ProcessContext) {
        val gson = Gson()
        c.output(KV.of(c.element().race, gson.toJson(c.element())))
    }
}

class MapKVToKV2 : DoFn<KV<String, Iterable<String>>, KV<String, String>>() {
    @ProcessElement
    fun processElement(c: ProcessContext) {
        val gson = Gson()
        c.output(KV.of(c.element().key, gson.toJson(c.element().value)))
    }
}
