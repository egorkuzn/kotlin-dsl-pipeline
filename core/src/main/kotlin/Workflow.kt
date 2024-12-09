package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.*

class Workflow(

    private val nodes: List<Node>,
    name: String,
    dispatcher: CoroutineDispatcher,
    countStackContext: Int,
    enableSecurityDeadLock: Boolean,
    enableWarringCyclePipe: Boolean
) {
    private val coroutineScope = CoroutineScope(dispatcher)
    private val jobs = mutableListOf<Job>()

    fun start() {
        nodes.forEach { node -> jobs.add(coroutineScope.launch { node.actions.invoke(coroutineScope) }) }
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
        name: String,
        input: Pipe.Single<T>,
        output: Pipe.Single<Q>,
        action: suspend (Pipe.Single<T>.Consumer, Pipe.Single<Q>.Producer) -> Unit
    ) {
        nodes.add(Node(name, listOf(input), listOf(output)) { coroutineScope ->
            action.invoke(input.Consumer(coroutineScope), output.Producer(coroutineScope))
        })
    }

    fun <Q> initial(
        name: String, output: Pipe.Single<Q>, action: suspend (Pipe.Single<Q>.Producer) -> Unit
    ) {
        nodes.add(Node(name, listOf(), listOf(output)) { coroutineScope->
            action.invoke(output.Producer(coroutineScope))
        })
    }

//    fun <T,Q> initial(
//        name: String, output: Pair<Pipe.Single<T>,Pipe.Single<Q>>, action: suspend (Pipe.Single<T>.Producer,Pipe.Single<Q>.Producer) -> Unit
//    ) {
//        val (outputPipe1, outputPipe2) = output
//        nodes.add(Node(name, listOf(), listOf(outputPipe1, outputPipe2)) {
//            action.invoke(outputPipe1.Producer(), outputPipe2.Producer())
//        })
//    }

    fun <T> finish(
        name: String, input: Pipe.Single<T>, action: suspend (Pipe.Single<T>.Consumer) -> Unit
    ) {
        nodes.add(Node(name, listOf(input), listOf()) { coroutineScope->
            action.invoke(input.Consumer(coroutineScope))
        })
    }

    fun <T, Q, S, P> node(
        name: String,
        input: Pair<Pipe.Single<T>, Pipe.Single<Q>>,
        output: Pair<Pipe.Single<S>, Pipe.Single<P>>,
        action: suspend (Pipe.Single<T>.Consumer, Pipe.Single<Q>.Consumer, Pipe.Single<S>.Producer, Pipe.Single<P>.Producer) -> Unit
    ) {
        nodes.add(Node(name, input.toList(), output.toList()) { corutone ->
            val (inputPipe1, inputPipe2) = input
            val (outputPipe3, outputPipe4) = output
            action.invoke(
                inputPipe1.Consumer(corutone),
                inputPipe2.Consumer(corutone),
                outputPipe3.Producer(corutone),
                outputPipe4.Producer(corutone)
            )
        })
    }

    fun sharedWorkflow(sharedWorkflow: () -> SharedWorkflow) {
        sharedWorkflow.invoke().getNodes().forEach(nodes::add)
    }

    fun build(
        name: String,
        dispatcher: CoroutineDispatcher,
        countStackContext: Int,
        enableSecurityDeadLock: Boolean,
        enableWarringCyclePipe: Boolean,
    ): Workflow {
        return Workflow(nodes, name, dispatcher, countStackContext, enableSecurityDeadLock, enableWarringCyclePipe)
    }

    fun buildSharedWorkflow(): SharedWorkflow {
        return SharedWorkflow(nodes)
    }
}

fun Workflow(
    name: String,
    countStackContext: Int = 10,
    enableSecurityDeadLock: Boolean = false,
    enableWarringCyclePipe: Boolean = false,
    dispatcher: CoroutineDispatcher = Dispatchers.Default, init: WorkflowBuilder.() -> Unit
): Workflow {
    return WorkflowBuilder().apply(init)
        .build(name, dispatcher, countStackContext, enableSecurityDeadLock, enableWarringCyclePipe)
}

fun SharedWorkflow(init: WorkflowBuilder.() -> Unit): SharedWorkflow {
    return WorkflowBuilder().apply(init).buildSharedWorkflow()
}