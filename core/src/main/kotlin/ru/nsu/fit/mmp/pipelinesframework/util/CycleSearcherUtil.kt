package ru.nsu.fit.mmp.pipelinesframework.util

import ru.nsu.fit.mmp.pipelinesframework.Node

object CycleSearcherUtil {
    /**
     * Проверяет наличие цикла в списке нод
     * @param nodes Список нод, в которых проверяем наличие цикла
     */
    fun isContainsCycle(nodes: List<Node>): Boolean {
        for (initialNode in nodes) {
            val otherNodes = nodes.filter { it != initialNode }

            for (node in otherNodes) {
                if (isConnected(initialNode, node)
                    && isContainsCycle(node, otherNodes.filter { it != node }, initialNode)) return true
            }
        }

        return false
    }

    /**
     * Проверка наличия цикла
     * @param currentNode Текущая вершина
     * @param otherNodes Список непройденных
     * @param initialNode Начальная вершина
     */
    fun isContainsCycle(currentNode: Node, otherNodes: List<Node>, initialNode: Node): Boolean {
        if (isConnected(currentNode, initialNode)) return true

        for (node in otherNodes) {
            if (isConnected(currentNode, node) &&
                isContainsCycle(node, otherNodes.filter { it != node }, initialNode)) return true
        }

        return false
    }

    /**
     * Проверяет связаны-ли вершины [nodeA] и [nodeB]
     * каким-либо путём из [nodeA] в [nodeB]
     */
    fun isConnected(nodeA: Node, nodeB: Node): Boolean {
        for (output in nodeA.output) {
            for (input in nodeB.input) {
                if (input == output) return true
            }
        }

        return false
    }
}