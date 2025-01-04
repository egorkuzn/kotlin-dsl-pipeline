package ru.nsu.fit.mmp.pipelinesframework

import ru.nsu.fit.mmp.pipelinesframework.pipe.ReceivePipe
import kotlin.time.Duration.Companion.seconds

fun line() = println("-".repeat(30))

fun main() {
    line()
    val workflow = Workflow {
        val numbers: ReceivePipe<Int> = produce {
            (1..10).map {
                send(it)
            }
        }

        val symbols = Pipe<String>()

        node(
            name = "Выведи символ 'a' n раз",
            input = numbers,
            output = symbols,
        ) {
            "a".repeat(it)
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