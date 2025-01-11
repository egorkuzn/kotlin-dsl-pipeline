package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.DelicateCoroutinesApi
import ru.nsu.fit.mmp.pipelinesframework.channel.ReceiveBufferChannel
import ru.nsu.fit.mmp.pipelinesframework.channel.SendPipe

/**
 * @param name Название ноды
 * @param input Входной pipe
 * @param output Выходной pipe
 * @see Pipe
 */
@OptIn(DelicateCoroutinesApi::class)
class Node(
    name: String,
    val input: List<ReceiveBufferChannel<*>>,
    val output: List<SendPipe<*>>,
    val actions: suspend () -> Unit,
) {
    fun isAnyChannelClosed(): Boolean = input.any { it.isClosedForReceive } || output.any { it.isClosedForSend }
}