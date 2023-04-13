package id.ciazhar.model

import com.datastax.driver.mapping.annotations.Table
import java.io.Serializable


@Table(
    keyspace = "test", name = "person", readConsistency = "QUORUM", writeConsistency = "QUORUM"
)
data class Person(
    val id: String,
    val name: String,
    val age: Int,
    val race: String
) : Serializable
