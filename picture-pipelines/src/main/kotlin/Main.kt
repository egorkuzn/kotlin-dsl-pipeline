package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun line() = println("-".repeat(30))

@OptIn(ExperimentalCoroutinesApi::class)
fun main() {
    println("Pipes")
    line()
    runBlocking {
        Workflow(dispatcher = Dispatchers.Default.limitedParallelism(4)) {
            val symbols = Pipe<String>()

            node(
                name = "Выведи символ 'a' n раз",
                input = Init(1, 2, 3, 4, 5),
                output = symbols
            ) { consumer, producer ->
                consumer.onListener { launch { producer.commit("a".repeat(it)) } }
            }

            node(
                name = "Принтер с улыбкой",
                input = symbols,
                output = Finish
            ) { consumer, _ ->
                consumer.onListener { println("$it)") }
            }

            node(
                name = "Принтер",
                input = symbols,
                output = Finish
            ) { consumer, _ ->
                consumer.onListener { println(it) }
            }
        }.start()
    }
    line()
    println("Channels")
    line()
    runBlocking {
        Workflow(dispatcher = Dispatchers.Default.limitedParallelism(4)) {
            val numbers = produce { (1..5).map { send(it) } }
            val symbols = produce { for (msg in numbers)  send("a".repeat(msg)) }
            launch { for (msg in symbols) println("${msg})")  }
            launch { for (msg in symbols) println(msg)  }
        }.start()
    }
    line()
}