package ru.nsu.fit.mmp.pipelinesframework.workflow

import ru.nsu.fit.mmp.pipelinesframework.node.Node
import ru.nsu.fit.mmp.pipelinesframework.workflow.builder.WorkflowBuilder

/**
 * Класс, представляющий общий конвейер, состоящий из списка узлов
 *
 * @param nodes Список узлов
 */
class SharedWorkflow(private val nodes: List<Node>) {
    /**
     * Возвращает список узлов, входящих в конвейер.
     *
     * @return Список узлов (Node).
     */
    fun getNodes(): List<Node> = nodes
}

/**
 * Элемент DSL, создающий экземпляр [SharedWorkflow]
 *
 * @param init Содержимое общего конвейера
 * @return Новый экземпляр [SharedWorkflow]
 */
fun SharedWorkflow(init: WorkflowBuilder.() -> Unit): SharedWorkflow {
    return WorkflowBuilder().apply(init).buildSharedWorkflow()
}