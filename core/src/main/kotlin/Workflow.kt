package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

class Workflow(
    private val nodes: List<Node>, dispatcher: CoroutineDispatcher
) {
    private val coroutineScope = CoroutineScope(dispatcher)
    private val jobs = mutableListOf<Job>()

    fun start() {
        nodes.forEach { node -> jobs.add(coroutineScope.launch {
            while (!node.isAnyChannelClosed()) node.actions.invoke()
        }) }
    }

    fun stop(duration: Duration) {
        runBlocking {
            delay(duration)
            nodes.forEach { node ->
                node.input.forEach { it.cancel() }
                node.output.forEach { it.close() }
            }
            joinAll(*jobs.toTypedArray())
        }
    }
}

class SharedWorkflow(private val nodes: List<Node>) {
    fun getNodes(): List<Node> = nodes
}

/**
 * Предлагается в случае отправки с разными типами использовать списки Any.
 * Реализация под конкретное количество - это ужасно. Предлагается пользователю самомстоятельно кастовать.
 * В рамках эксперимента использовался массив с типами - от него мало выгоды.
 */
class WorkflowBuilder(override val coroutineContext: CoroutineContext) : CoroutineScope {
    private val nodes = mutableListOf<Node>()

    companion object {
        const val ERROR_MESSAGE = """
            Количество элементов, которые возвращает action, не соответсвует количеству каналов-получателей
        """
    }

    /**
     * Один-к-одному
     */
    fun <T, Q> node(
        name: String,
        input: ReceiveChannel<T>,
        output: SendChannel<Q>,
        action: suspend (T) -> Q
    ) {
        nodes.add(Node(name, listOf(input), listOf(output)) {
            val inputElem = input.receive()
            val outputElem = action.invoke(inputElem)
            output.send(outputElem)
        })
    }

    /**
     * Многие-ко-многим
     */
    fun <T, Q> node(
        name: String,
        inputs: List<ReceiveChannel<T>>,
        outputs: List<SendChannel<Q>>,
        action: suspend (List<T>) -> Array<Q>,
    ) {
        nodes.add(Node(name, inputs, outputs) {
            val inputElems = inputs.map { input -> input.receive() }
            val outputElems = action.invoke(inputElems)
            if (outputElems.size == outputs.size) throw IllegalStateException(ERROR_MESSAGE)
            outputs.mapIndexed { index, output -> output.send(outputElems[index]) }
        })
    }

    /**
     * Один-ко-многим
     */
    fun <T, Q> node(
        name: String,
        input: ReceiveChannel<T>,
        outputs: List<SendChannel<Q>>,
        action: suspend (T) -> Array<Q>,
    ) {
        nodes.add(Node(name, listOf(input), outputs) {
            val inputElem = input.receive()
            val outputElems = action.invoke(inputElem)
            if (outputElems.size == outputs.size) throw IllegalStateException(ERROR_MESSAGE)
            outputs.mapIndexed { index, output -> output.send(outputElems[index]) }
        })
    }

    /**
     * Многие-к-одному
     */
    fun <T, Q> node(
        name: String,
        inputs: List<ReceiveChannel<T>>,
        output: SendChannel<Q>,
        action: suspend (List<T>) -> Q,
    ) {
        nodes.add(Node(name, inputs, listOf(output)) {
            val inputElems = inputs.map { input -> input.receive() }
            val outputElem = action.invoke(inputElems)
            output.send(outputElem)
        })
    }

    /**
     * Один-к-одному
     */
    fun <T> terminate(
        name: String,
        input: ReceiveChannel<T>,
        action: suspend (T) -> Unit
    ) {
        nodes.add(Node(name, listOf(input), emptyList()) {
            val inputElem = input.receive()
            action.invoke(inputElem)
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
    return WorkflowBuilder(dispatcher).apply(init).build(dispatcher)
}

//fun SharedWorkflow(init: WorkflowBuilder.() -> Unit): SharedWorkflow {
//    return WorkflowBuilder().apply(init).buildSharedWorkflow()
//}