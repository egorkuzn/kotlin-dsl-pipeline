package ru.nsu.fit.mmp.pipelinesframework.channel

import kotlinx.coroutines.channels.Channel
import ru.nsu.fit.mmp.pipelinesframework.channel.impl.BufferChannelImpl

interface BufferChannel<E> : Channel<E>, ReceiveBufferChannel<E>, SendPipe<E> {
    companion object {
        fun <E> of(channel: Channel<E>): BufferChannel<E> = BufferChannelImpl(channel)
    }
}