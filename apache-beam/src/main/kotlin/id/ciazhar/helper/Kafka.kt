package id.ciazhar.helper

import id.ciazhar.config.KafkaConfig
import org.apache.beam.sdk.io.kafka.KafkaIO
import org.apache.beam.sdk.transforms.PTransform
import org.apache.beam.sdk.values.KV
import org.apache.beam.sdk.values.PBegin
import org.apache.beam.sdk.values.PCollection
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer

fun writeToKafka(config: KafkaConfig, topic: String): KafkaIO.Write<String, String> {
    return KafkaIO.write<String, String>()
        .withBootstrapServers("${config.host}:${config.port}")
        .withTopic(topic)
        .withKeySerializer(StringSerializer::class.java)
        .withValueSerializer(StringSerializer::class.java)
}

fun readFromKafka(config: KafkaConfig, topic: String): PTransform<PBegin?, PCollection<KV<String, String>>>? {
    return KafkaIO.read<String, String>()
        .withBootstrapServers("${config.host}:${config.port}")
        .withTopic(topic)
        .withKeyDeserializer(StringDeserializer::class.java)
        .withValueDeserializer(StringDeserializer::class.java)
        .updateConsumerProperties(
            mapOf(
                "auto.offset.reset" to "earliest"
            )
        )
        .withoutMetadata()
}