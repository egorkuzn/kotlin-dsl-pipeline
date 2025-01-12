package ru.nsu.fit.mmp.pipelinesframework.pipe

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.nsu.fit.mmp.pipelinesframework.channel.BufferChannel

class Pipe<T> {
    private val channel = BufferChannel.of<T>();
    val context = Context()

    inner class Context {
        private val listeners = mutableListOf<(Context) -> Unit>()

        init {
            channel.onListenerBuffer {
                handleBufferChange(it)
            }
        }

        fun onListener(action: (context: Context) -> Unit) {
            listeners.add(action)
        }

        private fun handleBufferChange(buffer: List<T>) {
            //TODO buffer нужен для логирования
            listeners.forEach {
                it.invoke(this)
            }
        }
    }

    inner class Consumer(private val coroutineScope: CoroutineScope) {
        private val receiveChannel = channel

        suspend fun recive(): T {
            return channel.receive()
        }

        fun onListener(action: suspend (T) -> Unit) {
            coroutineScope.launch {
                for (p in channel) {
                    action(p)
                }
            }
        }

        operator fun <U> plus(other: Pipe<U>.Consumer): DualPipe<T, U>.Consumer {
            return DualPipe(channel, other.receiveChannel).Consumer(coroutineScope)
        }
    }

    inner class Producer {
        private val sendChannel = channel

        suspend fun commit(value: T) {
            //TODO Обработка ошибки отправки
            channel.send(value)
        }

        operator fun <U> plus(other: Pipe<U>.Producer): DualPipe<T, U>.Producer {
            return DualPipe(channel, other.sendChannel).Producer()
        }
    }

    fun close() {
        channel.close()
        channel.cancel()
    }
}