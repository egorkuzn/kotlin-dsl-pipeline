package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.CoroutineScope
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe

/**
 * @param name Название ноды
 * @param input Входной pipe
 * @param output Выходной pipe
 * @see Pipe
 */
class Node(
    name: String,
    private val input: List<Pipe<*>>,
    private val output: List<Pipe<*>>,
    val actions: suspend (coroutineScope: CoroutineScope) -> Unit,
) {
    fun destroy(){
        input.forEach { it.close() }
        output.forEach { it.close() }
    }
}