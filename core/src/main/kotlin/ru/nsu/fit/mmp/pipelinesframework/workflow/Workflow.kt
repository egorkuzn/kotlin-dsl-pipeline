package ru.nsu.fit.mmp.pipelinesframework.workflow

import kotlinx.coroutines.*
import ru.nsu.fit.mmp.pipelinesframework.Node

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
    private val context = Context()

    /**
     * Вложенный класс, представляющий контекст [Workflow]
     * Управляет слушателями и реагирует на изменения в контексте узлов.
     */
    inner class Context {
        private val listeners = mutableListOf<(Context) -> Unit>()

        init {
            nodes.forEach { node ->
                node.context.onListener {
                    handleNodeContextChange(node)
                }
            }
        }

        /**
         * Регистрация слушателя, который будет реагировать на изменения контекста [Workflow]
         *
         * @param listener Действие, выполняемое при изменении контекста.
         */
        fun onListener(listener: (Context) -> Unit) {
            listeners.add(listener)
        }

        /**
         * Обработчик изменения контекста в указанном узле
         *
         * @param node Узел, в контексте которого произошло изменение
         */
        private fun handleNodeContextChange(node: Node) {
            //TODO node нужен для логирования
            listeners.forEach {
                it.invoke(context)
            }
        }
    }

    /**
     * Запуск конвейера
     * Для каждого узла создается корутина, в которой выполняются его действия и обработка контекста
     */
    fun start() {
        nodes.forEach { node ->
            node.start(coroutineScope)
//                coroutineScope
//                    .launch {
//                        launch {
//                            context.onListener {
//                                println("Node '${node.name}' context changed: $it")
//                            }
//                        }
//                        node
//                    })
        }
    }

    /**
     * Остановка конвейера
     */
    fun stop() {
        nodes.forEach { it.stop() }
    }

}

