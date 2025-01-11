package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe
import kotlin.time.Duration.Companion.seconds

fun line() = println("-".repeat(30))

fun main() {
    line()
    val workflow = Workflow {
        val numbers = Pipe<Int>()

        val symbols = Pipe<String>()

        node(
            name = "Выведи символ 'a' n раз",
            inputs = numbers,
            outputs = symbols
        ) { consumer, producer ->
            consumer.onListener { producer.commit("a".repeat(it)) }
            println("www")
        }

        initial(
            name = "Поток чисел 1",
            output = numbers
        ) { producer ->
            (1..10).map {
                producer.commit(it)
            }
        }

        terminate(
            name = "Принтер с улыбкой",
            input = symbols
        ) { consumer ->

            consumer.listen { println("$it)") }
            println("www2")

        }

        terminate(
            name = "Принтер",
            input = symbols
        ) { consumer ->
            consumer.onListener { println("Принтер $it)") }
        }
    }

    workflow.start()

    runBlocking {
        delay(10.seconds)
        workflow.stop()
    }

    line()

//    val channel = BufferChannel.of<Int>()
//    runBlocking {
//        channel.send(10)
//        channel.send(30)
//
//        channel.send(20)
//        channel.send(30)
//        channel.send(30)
//
//       channel.bufferElements().forEach(::println)
//
//        for (p in channel) {
//            println("element: $p")
//        }
//    }
}

