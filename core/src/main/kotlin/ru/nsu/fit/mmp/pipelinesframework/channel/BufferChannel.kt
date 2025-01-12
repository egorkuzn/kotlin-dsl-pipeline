package ru.nsu.fit.mmp.pipelinesframework.channel

import kotlinx.coroutines.channels.Channel
import ru.nsu.fit.mmp.pipelinesframework.channel.impl.BufferChannelImpl

interface BufferChannel<E> : Channel<E>, ReceiveBufferChannel<E>, SendBufferChannel<E> {
    companion object {
        fun <E> of(): BufferChannel<E> = BufferChannelImpl()
    }

    fun bufferElements(): List<E>

    fun onListenerBuffer(listener: (List<E>) -> Unit)
}