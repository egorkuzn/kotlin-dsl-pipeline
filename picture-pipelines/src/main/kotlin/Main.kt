package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import ru.nsu.fit.mmp.pipelinesframework.ru.nsu.fit.mmp.pipelinesframework.Workflow
import kotlin.time.Duration.Companion.seconds

fun line() = println("-".repeat(30))

@OptIn(ExperimentalCoroutinesApi::class)
fun main() {
    line()
    val workflow = Workflow {
        val numbers = produce { (1..10).map {
            send(it)
        } }
        val symbols = Channel<String>()

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