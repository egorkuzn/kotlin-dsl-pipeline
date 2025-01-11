package ru.nsu.fit.mmp.pipelinesframework.pipe

import kotlinx.coroutines.*
import ru.nsu.fit.mmp.pipelinesframework.channel.BufferChannel


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