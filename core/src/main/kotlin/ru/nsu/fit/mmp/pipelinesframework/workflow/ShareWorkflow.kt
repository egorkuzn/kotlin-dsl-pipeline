package ru.nsu.fit.mmp.pipelinesframework.workflow

import ru.nsu.fit.mmp.pipelinesframework.Node

class SharedWorkflow(private val nodes: List<Node>) {
    fun getNodes(): List<Node> = nodes
}


fun SharedWorkflow(init: WorkflowBuilder.() -> Unit): SharedWorkflow {
    return WorkflowBuilder().apply(init).buildSharedWorkflow()
}