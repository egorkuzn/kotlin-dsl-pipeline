package ru.nsu.fit.mmp.pipelinesframework.workflow

import kotlinx.coroutines.*
import ru.nsu.fit.mmp.pipelinesframework.Node
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe

/**
 * Класс, представляющий конвейер, состоящий из множества узлов [Node]
 * Управляет их взаимодействием, обработкой контекста и асинхронным выполнением действий
 *
 * @param nodes Список узлов [Node]
 * @param dispatcher Диспетчер корутин [CoroutineDispatcher] для выполнения действий в рамках конвейера
 */
class Workflow(
    private val nodes: List<Node>, dispatcher: CoroutineDispatcher,
) {
    private val coroutineScope = CoroutineScope(dispatcher)

    private val contextHistory = mutableListOf(
        Context(
            mutableMapOf<Long, List<Any?>>().apply {},
            mutableMapOf<Long, Pipe.Context<*>>().apply {})
    )

    data class Context(
        val contextNode: MutableMap<Long, List<Any?>>,
        val contextPipe: MutableMap<Long, Pipe.Context<*>>
    )


    /**
     * Запуск конвейера
     * Для каждого узла создается корутина, в которой выполняются его действия и обработка контекста
     */
    fun start() {
        nodes.forEach { node ->
            node.onContextListener {
                handleUpdateContext(it)
            }
            node.start(coroutineScope)
        }
    }

    private fun handleUpdateContext(context: Node.Context) {

        val d = contextHistory.last().copy()
        context.pipesContext.forEach {
            if (d.contextPipe.containsKey(it.id)) {
                if (d.contextPipe[it.id]?.state!! < it.state) {
                    d.contextPipe[it.id] = it
                }
            } else {
                d.contextPipe[it.id] = it
            }
        }
        d.contextNode[context.id] = context.buffer
        contextHistory.add(d)

    }


    /**
     * Остановка конвейера
     */
    fun stop() {
        nodes.forEach { it.stop() }

        for (context in contextHistory) {
            println("_________________")
            context.contextNode.forEach {
                println("node ${it.key} have ${it.value}")
            }
            println("()()()()()()()()()()()()")

            context.contextPipe.forEach {
                println("pipe ${it.key} have ${it.value}")
            }
            println("_________________")
        }


    }

}

