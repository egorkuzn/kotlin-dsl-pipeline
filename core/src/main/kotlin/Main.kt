package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

fun main() {

    val executor = Executors.newFixedThreadPool(4)

    val workflow = Workflow(
        dispatcher = executor.asCoroutineDispatcher(),
    ) {

        val ip = Pipe<Int>()
        val an = Pipe<Float>()
        val gk = Pipe<Double>()

        sharedWorkflow {
            mySharedFlow(input = ip, output = ip, 11)
        }

        node(
            name = "Первая нода",
            input = Pair(ip, an),
            output = Pair(ip, gk)
        ) { a, b, c, d ->
            // Define the action logic here
            println("Executing action for node 'Первая нода'")
            // e.g., Use producers and consumers as needed
        }
    }

    workflow.start()

    runBlocking {
        workflow.stop()
    }
}


fun mySharedFlow(input: Pipe<Int>, output: Pipe<Int>, firstParam: Int): SharedWorkflow =
    SharedWorkflow {

        node(
            name = "Первая нода",
            input = input,
            output = output
        ) { a, b ->
            // Define the action logic here
            println("Executing action for node 'Первая нода'")
            // e.g., Use producers and consumers as needed
        }
    }

