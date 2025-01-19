package ru.nsu.fit.mmp.pipelinesframework.util

import io.github.oshai.kotlinlogging.KotlinLogging
import ru.nsu.fit.mmp.pipelinesframework.node.Node

class CycleSearcherUtil {
    private val logger = KotlinLogging.logger {}

    /**
     * Проверяет наличие цикла в списке нод
     * @param nodes Список нод, в которых проверяем наличие цикла
     */
    fun isContainsCycle(nodes: List<Node>): Boolean {
        for (initialNode in nodes) {
            val otherNodes = nodes.filter { it != initialNode }

            for (node in otherNodes) {
                if (isConnected(initialNode, node)) getCycleChain(
                    node,
                    otherNodes.filter { it != node },
                    initialNode
                ).let { chain ->
                    if (chain.isNotEmpty()) {
                        logger.debug { printChain(chain + initialNode) }
                        return true
                    }
                }
            }
        }

        return false
    }

    private fun printChain(chain: List<Node>): String = chain.joinToString(
        prefix = "[",
        separator = " --> ",
        postfix = "]"
    ) { it.name }

    /**
     * Получение цепочки цикла
     * @param currentNode Текущая вершина
     * @param otherNodes Список непройденных
     * @param initialNode Начальная вершина
     */
    fun getCycleChain(currentNode: Node, otherNodes: List<Node>, initialNode: Node): List<Node> {
        if (isConnected(currentNode, initialNode)) return listOf(initialNode, currentNode)

        for (node in otherNodes) {
            if (isConnected(currentNode, node)) getCycleChain(
                node,
                otherNodes.filter { it != node },
                initialNode
            ).let { chain ->
                if (chain.isNotEmpty()) return chain + currentNode
            }
        }

        return emptyList()
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