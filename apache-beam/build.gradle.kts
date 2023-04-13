plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("some-library") {
//        exclude group: 'com.google.guava', module: 'guava'
        exclude("com.google.guava:guava")
    }

    implementation("com.github.javafaker:javafaker:1.0.2")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.apache.beam:beam-runners-direct-java:2.1.0")
    implementation("org.apache.beam:beam-sdks-java-core:2.1.0")
    implementation("org.apache.beam:beam-sdks-java-io-kafka:2.1.0")
    implementation("org.apache.beam:beam-sdks-java-io-cassandra:2.1.0")
    implementation("org.apache.kafka:kafka-clients:3.4.0")
    implementation("com.scylladb:scylla-driver-mapping:3.11.0.1")
//    implementation("com.google.guava:guava:31.1-jre")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}