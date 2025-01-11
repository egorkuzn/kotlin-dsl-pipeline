package ru.nsu.fit.mmp.pipelinesframework.channel.impl

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.selects.SelectClause1
import kotlinx.coroutines.selects.SelectClause2
import ru.nsu.fit.mmp.pipelinesframework.channel.BufferChannel

class BufferChannelImpl<E>(private val channel: Channel<E>) : BufferChannel<E> {
    @DelicateCoroutinesApi
    override val isClosedForReceive: Boolean
        get() = channel.isClosedForReceive

    @DelicateCoroutinesApi
    override val isClosedForSend: Boolean
        get() = channel.isClosedForSend

    @ExperimentalCoroutinesApi
    override val isEmpty: Boolean
        get() = channel.isEmpty
    override val onReceive: SelectClause1<E>
        get() = channel.onReceive
    override val onReceiveCatching: SelectClause1<ChannelResult<E>>
        get() = channel.onReceiveCatching
    override val onSend: SelectClause2<E, SendChannel<E>>
        get() = channel.onSend

    override fun cancel(cause: CancellationException?) = channel.cancel(cause)

    @Deprecated(level = DeprecationLevel.HIDDEN, message = "Since 1.2.0, binary compatibility with versions <= 1.1.x")
    override fun cancel(cause: Throwable?) = false

    override fun close(cause: Throwable?): Boolean = channel.close(cause)

    override fun invokeOnClose(handler: (cause: Throwable?) -> Unit) = channel.invokeOnClose(handler)

    override fun iterator(): ChannelIterator<E> = channel.iterator()

    override suspend fun receive(): E = channel.receive()

    override suspend fun receiveCatching(): ChannelResult<E> = channel.receiveCatching()

    override fun tryReceive(): ChannelResult<E> = channel.tryReceive()

    override fun trySend(element: E): ChannelResult<Unit> = channel.trySend(element)

    override suspend fun send(element: E) = channel.send(element)

}