package ru.nsu.fit.mmp.pipelinesframework

import ru.nsu.fit.mmp.pipelinesframework.channel.ReceiveBufferChannel
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe
import kotlin.time.Duration.Companion.seconds

fun line() = println("-".repeat(30))

fun main() {
    line()
    val workflow = Workflow {
        val numbers = Pipe.Single<Int>()

        val symbols = Pipe.Single<String>()

        node(
            name = "Выведи символ 'a' n раз",
            inputs = numbers,
            outputs = symbols
        ) { consumer, producer ->

            //listOf("a".repeat(it.first()))
        }

        terminate(
            name = "Принтер с улыбкой",
            input = symbols
        ) {
            println("$it)")
        }

        terminate(
            name = "Принтер",
            input = symbols
        ) {
            println(it)
        }

        println("Yeah")
    }

    workflow.start()
    workflow.stop(duration = 10.seconds)

    line()
}