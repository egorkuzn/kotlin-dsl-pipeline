package ru.nsu.fit.mmp.pipelinesframework.pipe

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import ru.nsu.fit.mmp.pipelinesframework.channel.BufferChannel

class Pipe<T> {
    private val _channel = BufferChannel.of<T>();
    val context = Context()

    inner class Context{
        private val listeners = mutableListOf<(Context) -> Unit>()

        init {
            _channel.onListenerBuffer {
                handleBufferChange(it)
            }
        }

        fun onListener(action:  (context: Context) -> Unit) {
            listeners.add(action)
        }

        private fun handleBufferChange(buffer: List<T>){
            listeners.forEach {
                it.invoke(this)
            }
        }
    }

    inner class Consumer(private val coroutineScope: CoroutineScope) {
        private val channel = _channel

        suspend fun listen(action: suspend (T) -> Unit) {
            _channel.receive().apply {
                action(this)
            }
        }

        fun onListener(action: suspend (T) -> Unit) {
            coroutineScope.launch {
                for (p in _channel) {
                    action(p)
                }
            }
        }

        operator fun <U> plus(other: Pipe<U>.Consumer): DualPipe<T, U>.Consumer {
            return DualPipe(_channel, other.channel).Consumer(coroutineScope)
        }
    }

    inner class Producer(private val coroutineScope: CoroutineScope) {
        private val channel = _channel

        @OptIn(DelicateCoroutinesApi::class)
        suspend fun commit(value: T) {
            if (!_channel.isClosedForSend) {
                _channel.send(value)
            } else {
                throw IllegalStateException("Channel is closed")
            }
        }

        operator fun <U> plus(other: Pipe<U>.Producer): DualPipe<T, U>.Producer {
            return DualPipe(_channel, other.channel).Producer(coroutineScope)
        }
    }

    fun close() {
        _channel.close()
    }
}