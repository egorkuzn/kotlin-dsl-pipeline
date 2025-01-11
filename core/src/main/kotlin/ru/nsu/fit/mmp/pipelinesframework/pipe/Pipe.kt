package ru.nsu.fit.mmp.pipelinesframework.pipe

import kotlinx.coroutines.*
import ru.nsu.fit.mmp.pipelinesframework.channel.BufferChannel

class Pipe<T> {
    private val _channel = BufferChannel.of<T>();
    private val listeners = mutableListOf<(Context) -> Unit>()

    inner class Context(private val element: List<T>) {
        fun getElements(): List<T> {
            return element;
        }
    }

    inner class Consumer(private val coroutineScope: CoroutineScope) {
        val channel = _channel
        suspend fun listen(action: suspend (T) -> Unit) {
            _channel.receive().apply {
                action(this)
                notifyListeners()

            }
        }

        fun onListener(action: suspend (T) -> Unit) {
            coroutineScope.launch {
                for (p in _channel) {
                    action(p)
                    notifyListeners()
                }
            }
        }

        operator fun <U> plus(other: Pipe<U>.Consumer): DualPipe<T, U>.Consumer {
            return DualPipe(_channel, other.channel).Consumer(coroutineScope)
        }
    }

    inner class Producer {

        @OptIn(DelicateCoroutinesApi::class)
        suspend fun commit(value: T) {
            if (!_channel.isClosedForSend) {
                _channel.send(value)
                notifyListeners()
            } else {
                throw IllegalStateException("Channel is closed")
            }
        }

//            operator fun <U> plus(other: Single<U>.Producer): Dual<T, U>.Producer {
//                return Dual<T,U>().Producer()
//            }
    }

    fun close() {
        _channel.close()
    }

    fun onListenerContext(context: (Context) -> Unit) {
        synchronized(listeners) {
            listeners.add(context) // Добавляем нового слушателя
        }
    }


    private fun notifyListeners() {
        synchronized(listeners) {
            val bufferedElements = _channel.bufferElements()
            val context = Context(bufferedElements)
            listeners.forEach { it(context) } // Уведомляем всех слушателей
        }
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