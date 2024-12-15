package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce

fun line() = println("-".repeat(30))

@OptIn(ExperimentalCoroutinesApi::class)
fun main() {
    println("Pipes")
    line()
//    runBlocking {
//        Workflow(dispatcher = Dispatchers.Default.limitedParallelism(4)) {
//            val symbols = Pipe<String>()
//
//            node(
//                name = "Выведи символ 'a' n раз",
//                input = Init(1, 2, 3, 4, 5),
//                output = symbols
//            ) { consumer, producer ->
//                consumer.onListener { launch { producer.commit("a".repeat(it)) } }
//            }
//
//            node(
//                name = "Принтер с улыбкой",
//                input = symbols,
//                output = Finish
//            ) { consumer, _ ->
//                consumer.onListener { println("$it)") }
//            }
//
//            node(
//                name = "Принтер",
//                input = symbols,
//                output = Finish
//            ) { consumer, _ ->
//                consumer.onListener { println(it) }
//            }
//        }.start()
//    }
    line()
    println("Channels")
    line()
    runBlocking {
        val numbers = produce { (1..10).map { send(it) } }
        val symbols = produce {
            for (msg in numbers) launch {
                send("a".repeat(msg))
            }
        }
        launch {
            for (msg in symbols) launch {
                println("${msg})")
            }
        }
        launch {
            for (msg in symbols) launch {
                println(msg)
            }
        }
        delay(1000)
        numbers.cancel()
        symbols.cancel()
    }
    line()
    println("nnode")
    line()
    runBlocking {
        launch {
            Wworkflow(Dispatchers.Default.limitedParallelism(4)) {
                val numbers = pproduce { (1..10).map { send(it) } }
                val symbols = cchanel<String>()

                nnode(
                    name = "Выведи символ 'a' n раз",
                    input = numbers,
                    output = symbols
                ) {
                    "a".repeat(it)
                }

                nnode(
                    name = "Принтер с улыбкой",
                    input = symbols
                ) {
                    println("$it)")
                }

                nnode(
                    name = "Принтер",
                    input = symbols
                ) {
                    println(it)
                }

                println("Yeah")
            }
        }

        launch {
            delay(1000)
            stop()
        }
    }


    line()
}