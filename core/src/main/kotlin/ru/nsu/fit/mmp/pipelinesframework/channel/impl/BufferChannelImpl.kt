package ru.nsu.fit.mmp.pipelinesframework.channel.impl

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelIterator
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.selects.SelectClause1
import kotlinx.coroutines.selects.SelectClause2
import ru.nsu.fit.mmp.pipelinesframework.channel.BufferChannel

class BufferChannelImpl<E> : BufferChannel<E> {
    private val channel = Channel<E>(Channel.BUFFERED)
    private val listeners = mutableListOf<(List<E>) -> Unit>()

    private val buffer = mutableListOf<E>()
    private val lock = Any()

    override fun bufferElements(): List<E> {
        synchronized(lock) {
            return buffer.toList()
        }
    }

    override fun onListenerBuffer(listener: (List<E>) -> Unit) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    private fun notifyListeners() {
        synchronized(listeners) {
            listeners.forEach { it(buffer) }
        }
    }

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

    override fun close(cause: Throwable?): Boolean {
        synchronized(lock) {
            buffer.clear()
            notifyListeners()
        }
        return channel.close(cause)
    }

    override fun invokeOnClose(handler: (cause: Throwable?) -> Unit) = channel.invokeOnClose(handler)

    override fun iterator(): ChannelIterator<E> = channel.iterator()

    override suspend fun receive(): E {
        val element = channel.receive()

        synchronized(lock) {
            buffer.remove(element)
            notifyListeners()
        }
        return element
    }

    override suspend fun receiveCatching(): ChannelResult<E> {
        val result = channel.receiveCatching()
        result.getOrNull()?.let { element ->
            synchronized(lock) {
                buffer.remove(element)
                notifyListeners()
            }
        }
        return result
    }

    override fun tryReceive(): ChannelResult<E> {
        val result = channel.tryReceive()
        result.getOrNull()?.let { element ->
            synchronized(lock) {
                buffer.remove(element)
                notifyListeners()
            }
        }
        return result
    }

    override fun trySend(element: E): ChannelResult<Unit> {
        val result = channel.trySend(element)
        if (result.isSuccess) {
            synchronized(lock) {
                buffer.add(element)
                notifyListeners()
            }
        }
        return result
    }

    override suspend fun send(element: E) {
        synchronized(lock) {
            buffer.add(element)
            notifyListeners()
        }
        channel.send(element)
    }

}