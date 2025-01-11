package ru.nsu.fit.mmp.pipelinesframework.channel

import kotlinx.coroutines.channels.ReceiveChannel
import ru.nsu.fit.mmp.pipelinesframework.channel.impl.ReceiveBufferChannelImpl

interface ReceiveBufferChannel<out E>: ReceiveChannel<E> {
    companion object {
        fun <E> of(receiveChannel: ReceiveChannel<E>): ReceiveBufferChannel<E> = ReceiveBufferChannelImpl(receiveChannel)
    }
}