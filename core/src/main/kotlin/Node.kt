package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel

/**
 * @param name Название ноды
 * @param input Входной pipe
 * @param output Выходной pipe
 * @see Pipe
 */
@OptIn(DelicateCoroutinesApi::class)
class Node(
    name: String,
    val input: List<ReceiveChannel<*>>,
    val output: List<SendChannel<*>>,
    val actions: suspend ()->Unit
) {
    fun isAnyChannelClosed(): Boolean = input.any { it.isClosedForReceive } || output.any { it.isClosedForSend }
}