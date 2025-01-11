package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.*
import ru.nsu.fit.mmp.pipelinesframework.Node.Context
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe

class Workflow(
    private val nodes: List<Node>, dispatcher: CoroutineDispatcher,
) {
    private val coroutineScope = CoroutineScope(dispatcher)
    private val jobs = mutableListOf<Job>()
    private val context = Context()


    inner class Context {
        private val listeners = mutableListOf<(Context) -> Unit>()

        init {
            nodes.forEach { node ->
                node.context.onListener {
                    handleNodeContextChange(node)
                }
            }
        }

        fun onListener(listener: (Context) -> Unit) {
            listeners.add(listener)
        }

        private fun handleNodeContextChange(node: Node) {
            //TODO node нужен для логирования
            listeners.forEach {
                it.invoke(context)
            }
        }
    }

    /**
     * Довольно плохое isAnyChannelClosed
     * Помогает, но плохо решает проблему, так как всё равно стреляет ошибка,
     * которую я интерпретирую, что происходит попытка отправки.
     */
    fun start() {
        nodes.forEach { node ->
            jobs.add(
                coroutineScope
                    .launch {
                        launch {
                            context.onListener { println("Node '${node.name}' context changed: $it") }
                        }
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
        })
    }

    fun <T> initial(
        name: String,
        output: Pipe<T>,
        action: suspend (Pipe<T>.Producer) -> Unit,
    ) {
        nodes.add(Node(name, emptyList(), listOf(output)) {
            action.invoke(output.Producer(it))
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
    dispatcher: CoroutineDispatcher = Dispatchers.Default, init: WorkflowBuilder.() -> Unit,
): Workflow {
    return WorkflowBuilder().apply(init).build(dispatcher)
}

fun SharedWorkflow(init: WorkflowBuilder.() -> Unit): SharedWorkflow {
    return WorkflowBuilder().apply(init).buildSharedWorkflow()
}