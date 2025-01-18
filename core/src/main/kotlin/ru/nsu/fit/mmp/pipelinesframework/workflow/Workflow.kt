package ru.nsu.fit.mmp.pipelinesframework.workflow

import ch.qos.logback.core.joran.conditional.IfAction
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import ru.nsu.fit.mmp.pipelinesframework.Node
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe
import ru.nsu.fit.mmp.pipelinesframework.util.CycleSearcherUtil

/**
 * Класс, представляющий конвейер, состоящий из множества узлов [Node]
 * Управляет их взаимодействием, обработкой контекста и асинхронным выполнением действий
 *
 * @param nodes Список узлов [Node]
 * @param countStackContext Количество контекстов, хранящиеся в стеке
 * @param dispatcher Диспетчер корутин [CoroutineDispatcher] для выполнения действий в рамках конвейера
 */
class Workflow(
    private val nodes: List<Node>,
    private val countStackContext:  Int,
    private val enableSecurityDeadLock: Boolean,
    enabledWarringCyclePipe: Boolean,
    dispatcher: CoroutineDispatcher,
) {
    private val logger = KotlinLogging.logger {}
    private val coroutineScope = CoroutineScope(dispatcher)

    private val contextHistory = mutableListOf(
        Context(
            mutableMapOf<Long, List<String>>().apply {},
            mutableMapOf<Long, Pipe.Context>().apply {})
    )

    init {
        if (enabledWarringCyclePipe) {
            if (CycleSearcherUtil().isContainsCycle(nodes))
            {
             logger.error { "Внимание обнаружен цикл" }
            }
        }
    }

    /**
     * Класс, представляющий контекст конвейера
     *
     * @param contextNode Список элементов, которые обрабатывает узел [Node]
     * @param contextPipe Список контекстов каналов [Pipe.Context], которые используются в конвейере [Workflow]
     */
    data class Context(
        val contextNode: MutableMap<Long, List<String>>,
        val contextPipe: MutableMap<Long, Pipe.Context>
    ){
        /**
         * Глубокое копирование контекста
         */
        fun deepCopy(): Context {
            return Context(
                contextNode = contextNode.mapValues { entry ->
                    entry.value.map { it }
                }.toMutableMap(),
                contextPipe = contextPipe.mapValues { entry ->
                    entry.value
                }.toMutableMap()
            )
        }

        /**
         * Сравнение контекстов
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Context) return false

            // Сравниваем contextNode
            if (contextNode.size != other.contextNode.size) return false
            if (contextNode.any { (key, value) -> other.contextNode[key] != value }) return false

            // Сравниваем contextPipe
            if (contextPipe.size != other.contextPipe.size) return false
            if (contextPipe.any { (key, value) -> other.contextPipe[key] != value }) return false

            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }


    /**
     * Запуск конвейера
     * Для каждого узла создается корутина, в которой выполняются его действия и обработка контекста
     */
    fun start() {
        nodes.forEach { node ->
            if (enableSecurityDeadLock) {
                node.onContextListener {
                    handleUpdateContext(it)
                }
            }
            node.start(coroutineScope)
        }
    }

    /**
     * Обработчик обновление контекста узлов [Node]
     *
     * @param context Обновленный контекст узла [Node.Context]
     */
    private fun handleUpdateContext(context: Node.Context) {

        val newContext = contextHistory.last().deepCopy()
        context.pipesContext.forEach {
            if (newContext.contextPipe.containsKey(it.id)) {
                if (newContext.contextPipe[it.id]?.state!! < it.state) {
                    newContext.contextPipe[it.id] = it
                }
            } else {
                newContext.contextPipe[it.id] = it
            }
        }
        if (contextHistory.size >= countStackContext ) {
            contextHistory.removeAt(0)
        }

        newContext.contextNode[context.id] = context.buffer

        assert(contextHistory.contains(newContext))
        contextHistory.add(newContext)
    }


    /**
     * Остановка конвейера
     */
    fun stop() {
        nodes.forEach { it.stop() }
    }
}

