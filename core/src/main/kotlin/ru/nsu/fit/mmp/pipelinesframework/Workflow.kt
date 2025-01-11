package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.*
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe

class Workflow(
    private val nodes: List<Node>, dispatcher: CoroutineDispatcher,
) {
    private val coroutineScope = CoroutineScope(dispatcher)
    private val jobs = mutableListOf<Job>()

    // -----> [x] -->

    /**
     * Довольно плохое isAnyChannelClosed
     * Помогает, но плохо решает проблему, так как всё равно стреляет ошибка,
     * которую я интерпретирую, что происходит попытка отправки.
     */
    fun start() {
        nodes.forEach { node ->
            jobs.add(coroutineScope.launch {
//                while (!node.isAnyChannelClosed()) node.actions.invoke()
                node.actions.invoke(coroutineScope)
            })
        }
    }

    suspend fun stop() {
        nodes.forEach { it.destroy() }

        joinAll(*jobs.toTypedArray())
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
class WorkflowBuilder {
    private val nodes = mutableListOf<Node>()

    companion object {
        const val ERROR_MESSAGE = """
            Количество элементов, которые возвращает action, не соответсвует количеству каналов-получателей
        """
    }

    /**
     * Многие-ко-многим
     * А -> x
     * B -> x
     * C -> xxxx
     */
    fun <T, Q> node(
        name: String,
        inputs: Pipe<T>,
        outputs: Pipe<Q>,
        action: suspend (Pipe<T>.Consumer, Pipe<Q>.Producer) -> Unit
    ) {
        nodes.add(Node(name, listOf(inputs), listOf(outputs)) {
            action.invoke(inputs.Consumer(it), outputs.Producer(it))
//            val inputElems = inputs.map { input -> input.tryReceive().getOrNull() ?: return@Node }
//            val outputElems = action.invoke(inputElems)
//            if (outputElems.size != outputs.size) throw IllegalStateException(ERROR_MESSAGE)
//            outputs.mapIndexed { index, output -> output.send(outputElems[index]) }
        })
    }

    fun <T> initial(
        name: String,
        output: Pipe<T>,
        action: suspend (Pipe<T>.Producer) -> Unit,
    ) {
        nodes.add(Node(name, emptyList(), listOf(output)) {
            action.invoke(output.Producer(it))
//            val inputElems = input.map { it.tryReceive().getOrNull() ?: return@Node }
//            inputElems.map { action.invoke(it) }
        })
    }

    /**
     * Вывод
     */
    fun <T> terminate(
        name: String,
        input: Pipe<T>,
        action: suspend (Pipe<T>.Consumer) -> Unit,
    ) {
        nodes.add(Node(name, listOf(input), emptyList()) {
            action.invoke(input.Consumer(it))
//            val inputElems = input.map { it.tryReceive().getOrNull() ?: return@Node }
//            inputElems.map { action.invoke(it) }
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

//    fun <E> Pipe(): BufferChannel<E> = BufferChannel.of(Channel<E>())

//    @OptIn(ExperimentalTypeInference::class, ExperimentalCoroutinesApi::class)
//    fun <E> produce(
//        @BuilderInference block: suspend ProducerScope<E>.() -> Unit
//    ): ReceiveBufferChannel<E> = ReceiveBufferChannel.of(pproduce(block = block))
}

fun Workflow(
    dispatcher: CoroutineDispatcher = Dispatchers.Default, init: WorkflowBuilder.() -> Unit,
): Workflow {
    return WorkflowBuilder().apply(init).build(dispatcher)
}

fun SharedWorkflow(init: WorkflowBuilder.() -> Unit): SharedWorkflow {
    return WorkflowBuilder().apply(init).buildSharedWorkflow()
}