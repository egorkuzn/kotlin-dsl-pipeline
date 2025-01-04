package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.DelicateCoroutinesApi
import ru.nsu.fit.mmp.pipelinesframework.pipe.ReceivePipe
import ru.nsu.fit.mmp.pipelinesframework.pipe.SendPipe

/**
 * @param name Название ноды
 * @param input Входной pipe
 * @param output Выходной pipe
 * @see Pipe
 */
@OptIn(DelicateCoroutinesApi::class)
class Node(
    name: String,
    val input: List<ReceivePipe<*>>,
    val output: List<SendPipe<*>>,
    val actions: suspend () -> Unit,
) {
    fun isAnyChannelClosed(): Boolean = input.any { it.isClosedForReceive } || output.any { it.isClosedForSend }
}