package id.ciazhar

import id.ciazhar.service.generateDataToKafka
import id.ciazhar.service.streamDataFromKafka

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
//        generateDataToKafka()
        streamDataFromKafka()
    }
}