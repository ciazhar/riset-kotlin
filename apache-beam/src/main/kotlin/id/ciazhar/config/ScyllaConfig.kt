package id.ciazhar.config

data class ScyllaConfig (
    val port: Int,
    val hosts: List<String>,
    val username : String,
    val password : String,
    val keyspace : String,

)
