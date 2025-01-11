package ru.nsu.fit.mmp.pipelinesframework.pipe

import kotlinx.coroutines.*
import ru.nsu.fit.mmp.pipelinesframework.channel.BufferChannel

class Pipe<T> {
    private val _channel = BufferChannel.of<T>();

    inner class Context {
        fun getElements(): List<T> {
            return _channel.bufferElements();
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
                return DualPipe<T,U>(_channel, other.channel).Producer(coroutineScope)
            }
    }

    fun close() {
        _channel.close()
    }
}

class DualPipe<T, U>(
    private val _channelT: BufferChannel<T>,
    private val _channelU: BufferChannel<U>,
) {

    inner class Consumer(private val coroutineScope: CoroutineScope) {
        suspend fun listen(action: (T, U) -> Unit) {
            val jobs = coroutineScope.launch {
                val valueT = async { _channelT.receive() }
                val valueU = async { _channelU.receive() }

                action.invoke(valueT.await(), valueU.await())
            }

            jobs.join()
        }

        @OptIn(DelicateCoroutinesApi::class)
        fun onListener(action: (T, U) -> Unit) {
            coroutineScope.launch {
                while (!_channelT.isClosedForReceive && !_channelU.isClosedForSend) {
                    val valueT = async { _channelT.receive() }
                    val valueU = async { _channelU.receive() }

                    action.invoke(valueT.await(), valueU.await())
                }
            }
        }
    }

    inner class Producer(private val coroutineScope: CoroutineScope) {
        suspend fun commit(value1: T, value2: U) {
            val jobs = coroutineScope.launch {
                val valueT = async { _channelT.send(value1) }
                val valueU = async { _channelU.send(value2) }
                awaitAll(valueT, valueU)
            }
            jobs.join()
        }
    }
}