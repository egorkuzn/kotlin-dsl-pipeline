package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import ru.nsu.fit.mmp.pipelinesframework.channel.BufferChannel
import ru.nsu.fit.mmp.pipelinesframework.channel.ReceiveBufferChannel
import ru.nsu.fit.mmp.pipelinesframework.channel.SendPipe
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe
import kotlin.coroutines.CoroutineContext
import kotlin.experimental.ExperimentalTypeInference
import kotlin.time.Duration
import kotlinx.coroutines.channels.produce as pproduce

class Workflow(
    private val nodes: List<Node>, dispatcher: CoroutineDispatcher,
) {
    private val coroutineScope = CoroutineScope(dispatcher)
    private val jobs = mutableListOf<Job>()

    // -----> [x] -->

    /**
     * Довольно плохое isAnyChannelClosed
     * Помогает, но плохо  решает проблему, так как всё равно стреляет ошибка,
     * которую я интерпретирую, что происходит попытка отправки.
     */
    fun start() {
        nodes.forEach { node ->
            jobs.add(coroutineScope.launch {
//                while (!node.isAnyChannelClosed()) node.actions.invoke()
            })
        }
    }

    fun stop(duration: Duration) {
        runBlocking {
            delay(duration)
            nodes.forEach { node ->
//                node.input.forEach { it.cancel() }
//                node.output.forEach { it.close() }
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
     * Многие-ко-многим
     * А -> x
     * B -> x
     * C -> xxxx
     */
    fun <T, Q> node(
        name: String,
        inputs: Pipe.Single<T>,
        outputs: Pipe.Single<Q>,
        action: suspend (Pipe.Single<T>.Consumer, Pipe.Single<Q>.Producer) -> Unit
    ) {
        nodes.add(Node(name, listOf(inputs), listOf(outputs)) {
//            val inputElems = inputs.map { input -> input.tryReceive().getOrNull() ?: return@Node }
//            val outputElems = action.invoke(inputElems)
//            if (outputElems.size != outputs.size) throw IllegalStateException(ERROR_MESSAGE)
//            outputs.mapIndexed { index, output -> output.send(outputElems[index]) }
        })
    }

    /**
     * Вывод
     */
    fun <T> terminate(
        name: String,
        input: Pipe.Single<T>,
        action: suspend (Pipe.Single<T>.Producer) -> Unit,
    ) {
        nodes.add(Node(name, listOf(input), emptyList()) {
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

    fun <E> Pipe(): BufferChannel<E> = BufferChannel.of(Channel<E>())

    @OptIn(ExperimentalTypeInference::class, ExperimentalCoroutinesApi::class)
    fun <E> produce(
        @BuilderInference block: suspend ProducerScope<E>.() -> Unit
    ): ReceiveBufferChannel<E> = ReceiveBufferChannel.of(pproduce(block = block))
}

fun Workflow(
    dispatcher: CoroutineDispatcher = Dispatchers.Default, init: WorkflowBuilder.() -> Unit,
): Workflow {
    return WorkflowBuilder(dispatcher).apply(init).build(dispatcher)
}

fun SharedWorkflow(init: WorkflowBuilder.() -> Unit): SharedWorkflow {
    return WorkflowBuilder(
        TODO("Вот а тут что для SharedWorkflow - вопросы предложения")
    ).apply(init).buildSharedWorkflow()
}