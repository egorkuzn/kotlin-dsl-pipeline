package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.*

class Workflow(
    private val nodes: List<Node>, dispatcher: CoroutineDispatcher
) {
    private val coroutineScope = CoroutineScope(dispatcher)
    private val jobs = mutableListOf<Job>()

    fun start() {
        nodes.forEach { node -> jobs.add(coroutineScope.launch { node.actions.invoke() }) }
    }

    suspend fun stop() {
        joinAll(*jobs.toTypedArray())
    }
}

class SharedWorkflow(private val nodes: List<Node>) {
    fun getNodes(): List<Node> = nodes
}

class WorkflowBuilder {
    private val nodes = mutableListOf<Node>()

    fun <T, Q> node(
        name: String, input: Pipe<T>, output: Pipe<Q>, action: suspend (Pipe<T>.Consumer, Pipe<Q>.Producer) -> Unit
    ) {
        nodes.add(Node(name, listOf(input), listOf(output)) {
            action.invoke(input.Consumer(), output.Producer())
        })
    }

    fun <Q> initial(
        name: String, output: Pipe<Q>, action: suspend (Pipe<Q>.Producer) -> Unit
    ) {
        nodes.add(Node(name, listOf(), listOf(output)) {
            action.invoke(output.Producer())
        })
    }

    fun <T> finish(
        name: String, input: Pipe<T>,action: suspend (Pipe<T>.Consumer) -> Unit
    ) {
        nodes.add(Node(name, listOf(input), listOf()) {
            action.invoke(input.Consumer())
        })
    }

    fun <T, Q, S, P> node(
        name: String,
        input: Pair<Pipe<T>, Pipe<Q>>,
        output: Pair<Pipe<S>, Pipe<P>>,
        action: suspend (Pipe<T>.Consumer, Pipe<Q>.Consumer, Pipe<S>.Producer, Pipe<P>.Producer) -> Unit
    ) {
        nodes.add(Node(name, input.toList(), output.toList()) {
            val (inputPipe1, inputPipe2) = input
            val (outputPipe3, outputPipe4) = output
            action.invoke(inputPipe1.Consumer(), inputPipe2.Consumer(), outputPipe3.Producer(), outputPipe4.Producer())
        })
    }

    fun sharedWorkflow(sharedWorkflow: () -> SharedWorkflow) {
        sharedWorkflow.invoke().getNodes().forEach(nodes::add)
    }

    fun build(dispatcher: CoroutineDispatcher): Workflow {
        return Workflow(nodes, dispatcher)
    }

    fun buildSharedWorkflow(): SharedWorkflow {
        return SharedWorkflow(nodes)
    }
}

fun Workflow(
    dispatcher: CoroutineDispatcher = Dispatchers.Default, init: WorkflowBuilder.() -> Unit
): Workflow {
    return WorkflowBuilder().apply(init).build(dispatcher)
}

fun SharedWorkflow(init: WorkflowBuilder.() -> Unit): SharedWorkflow {
    return WorkflowBuilder().apply(init).buildSharedWorkflow()
}