package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

fun main() {

    val executor = Executors.newFixedThreadPool(4)

    val workflow = Workflow(
        "Сумматор",
        dispatcher = executor.asCoroutineDispatcher(),
    ) {

        val randomPipe1 = pipe<Int>()
        val outputPipe1 = pipe<Double>()
        val randomPipe2 = pipe<Int>()
        val outputPipe2 = pipe<Double>()

        sharedWorkflow {
            sequenceGenerator(randomPipe1)
        }

        sharedWorkflow {
            sequenceGenerator(randomPipe2)
        }

        node(
            "Лишняя нода",
            input = Pair(randomPipe1, randomPipe2),
            output = Pair(outputPipe1, outputPipe2)
        ) { consumer, consumer2, producer, producer2 ->
            (consumer + consumer2).onListener { value1, value2 ->
                (producer + producer2).commit(value1.toDouble(), value2.toDouble())
            }

            println("hello")
        }

        finish(
            "Лишняя нода 2",
            input = outputPipe1,
        ) { consumer ->
            consumer.onListener{
                value ->
                println("Лишняя нода 2 $value")
            }
        }


        finish(
            name = "Print", input = randomPipe1
        ) { consumer ->
            consumer.onListener { value ->
                println(value)
            }
        }
    }

    workflow.start()

    runBlocking {
        workflow.stop()
    }
}


fun sequenceGenerator(output: Pipe.Single<Int>): SharedWorkflow = SharedWorkflow {

    initial(
        name = "Генератор последовательности", output = output
    ) { producer ->
        var count = 0
        while (true) {
            delay(1000)
            producer.commit(count++)
        }
    }
}

