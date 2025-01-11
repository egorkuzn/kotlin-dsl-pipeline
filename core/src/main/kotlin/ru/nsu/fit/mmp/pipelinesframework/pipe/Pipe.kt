package ru.nsu.fit.mmp.pipelinesframework.pipe

import kotlinx.coroutines.DelicateCoroutinesApi
import ru.nsu.fit.mmp.pipelinesframework.channel.BufferChannel

sealed class Pipe {
    abstract fun close()

    abstract class Context <T> {
        abstract fun getElements(): List<T>
    }

    abstract fun onListenerContext(context: (Context<*>) -> Unit);

    class Single<T> : Pipe() {
        private val channel = BufferChannel.of<T>();
        private val listeners = mutableListOf<(Context<T>) -> Unit>()

        inner class SingleContext(private val element: List<T>) : Context<T>() {
            override fun getElements(): List<T> {
                return element;
            }
        }

        inner class Consumer {
            suspend fun listen(action: suspend (T) -> Unit) {
                channel.receive().apply {
                    action(this)
                    notifyListeners()

                }
            }

            suspend fun onListener(action: suspend (T) -> Unit) {
                for (p in channel) {
                    action(p)
                    notifyListeners()
                }
            }

//            operator fun <U> plus(other: Single<U>.Consumer): Dual<T, U>.Consumer {
//                return Dual<T,U>().Consumer(coroutineScope)
//            }
        }

        inner class Producer {


            @OptIn(DelicateCoroutinesApi::class)
            suspend fun commit(value: T) {
                if (!channel.isClosedForSend) {
                    channel.send(value)
                    notifyListeners() // Уведомляем всех слушателей
                } else {
                    throw IllegalStateException("Channel is closed")
                }
            }

//            operator fun <U> plus(other: Single<U>.Producer): Dual<T, U>.Producer {
//                return Dual<T,U>().Producer()
//            }
        }

        override fun close() {
            channel.close()
        }

        override fun onListenerContext(context: (Context<*>) -> Unit) {
            synchronized(listeners) {
                listeners.add(context) // Добавляем нового слушателя
            }
        }


        private fun notifyListeners() {
            synchronized(listeners) {
                val bufferedElements = channel.bufferElements()
                val context = SingleContext(bufferedElements)
                listeners.forEach { it(context) } // Уведомляем всех слушателей
            }
        }
    }

//    class Dual<T, U>(
//
//    ) : Pipe() {
//
//
//        inner class Consumer(private val coroutineScope: CoroutineScope) {
//            fun onListener(action: (T, U) -> Unit) {
//
//            }
//        }
//
//        inner class Producer {
//            suspend fun commit(value1: T, value2: U) {
//
//            }
//        }
//    }
}