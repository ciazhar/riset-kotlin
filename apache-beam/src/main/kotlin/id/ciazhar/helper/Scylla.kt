package id.ciazhar.helper

import id.ciazhar.config.ScyllaConfig
import id.ciazhar.model.Person
import org.apache.beam.sdk.io.cassandra.CassandraIO

fun writeToScylla(config: ScyllaConfig): CassandraIO.Write<Person>? {
    return CassandraIO.write<Person>()
        .withHosts(config.hosts)
        .withPort(config.port)
//        .withUsername(config.username)
//        .withPassword(config.password)
        .withKeyspace(config.keyspace)
        .withEntity(Person::class.java)
}