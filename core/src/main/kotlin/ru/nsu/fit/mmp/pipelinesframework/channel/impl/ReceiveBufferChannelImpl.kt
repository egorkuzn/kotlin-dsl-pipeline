package ru.nsu.fit.mmp.pipelinesframework.channel.impl

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ChannelIterator
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.selects.SelectClause1
import ru.nsu.fit.mmp.pipelinesframework.channel.ReceiveBufferChannel

class ReceiveBufferChannelImpl<out E>(private val receiveChannel: ReceiveChannel<E>): ReceiveBufferChannel<E> {
    @DelicateCoroutinesApi
    override val isClosedForReceive: Boolean
        get() = receiveChannel.isClosedForReceive

    @ExperimentalCoroutinesApi
    override val isEmpty: Boolean
        get() = receiveChannel.isEmpty
    override val onReceive: SelectClause1<E>
        get() = receiveChannel.onReceive
    override val onReceiveCatching: SelectClause1<ChannelResult<E>>
        get() = receiveChannel.onReceiveCatching

    override fun cancel(cause: CancellationException?) = receiveChannel.cancel(cause)

    @Deprecated("Since 1.2.0, binary compatibility with versions <= 1.1.x", level = DeprecationLevel.HIDDEN)
    override fun cancel(cause: Throwable?): Boolean = false

    override fun iterator(): ChannelIterator<E> = receiveChannel.iterator()

    override suspend fun receive(): E = receiveChannel.receive()

    override suspend fun receiveCatching(): ChannelResult<E> = receiveChannel.receiveCatching()

    override fun tryReceive(): ChannelResult<E> = receiveChannel.tryReceive()
}