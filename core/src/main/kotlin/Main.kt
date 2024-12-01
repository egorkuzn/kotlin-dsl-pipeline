package ru.nsu.fit.mmp.pipelinesframework

import java.util.concurrent.ConcurrentLinkedQueue

fun main() {
    println("Hello World!")
}


class Pipe<T> {
    private val queue = ConcurrentLinkedQueue<T>()

    inner class Consumer {
        fun onListener(action: (T) -> Unit) {

        }
    }

    inner class Producer {
        fun commit(value: T) {

        }
    }
}

class Node(
    name: String,
//           inputPipes: List<*>,
//           outputPipes: List<*>
)

class Workflow(val nodes: List<Node>)

class WorkflowBuilder {
    private val nodes = mutableListOf<Node>()

    fun <T, Q> node(
        name: String,
        input: Pipe<T>,
        output: Pipe<Q>,
        action: (Pipe<T>.Consumer, Pipe<Q>.Consumer) -> Unit
    ) {
    }

    fun <T, Q, S, P> node(
        name: String,
        input: Pair<Pipe<T>, Pipe<Q>>,
        output: Pair<Pipe<S>, Pipe<P>>,
        action: (Pipe<T>.Consumer, Pipe<Q>.Consumer, Pipe<S>.Producer, Pipe<P>.Producer) -> Unit
    ) {
    }

    fun sharedWorkflow(workflow: () -> Workflow) {
    }

    fun build(): Workflow {
        return Workflow(nodes)
    }
}

fun Workflow(init: WorkflowBuilder.() -> Unit): Workflow {
    return WorkflowBuilder().apply(init).build()
}

val workflow = Workflow {

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


fun mySharedFlow(input: Pipe<Int>, output: Pipe<Int>, firstParam: Int): Workflow =
    Workflow {

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

