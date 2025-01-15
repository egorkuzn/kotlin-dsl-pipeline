package ru.nsu.fit.mmp.pipelinesframework.workflow

import kotlinx.coroutines.*
import ru.nsu.fit.mmp.pipelinesframework.Node

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
                            context.onListener {
                                println("Node '${node.name}' context changed: $it")
                            }
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

