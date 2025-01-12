package ru.nsu.fit.mmp.pipelinesframework.pipe

import kotlinx.coroutines.*
import ru.nsu.fit.mmp.pipelinesframework.channel.BufferChannel


class DualPipe<T, U>(
    private val channelT: BufferChannel<T>,
    private val channelU: BufferChannel<U>,
) {

    inner class Consumer(private val coroutineScope: CoroutineScope) {
        suspend fun recive(): Pair<T, U> {
            return Pair(channelT.receive(), channelU.receive())
        }

        fun onListener(action: (T, U) -> Unit) {
            coroutineScope.launch {
                while (true) {
                    val valueT = channelT.tryReceive().getOrNull()
                    val valueU = channelU.tryReceive().getOrNull()
                    if (valueT != null && valueU != null) {
                        action(valueT, valueU)
                    } else {
                        break
                    }
                }
            }
        }
    }

    inner class Producer {

        suspend fun commit(value1: T, value2: U) {
            channelT.send(value1)
            channelU.send(value2)
        }
    }
}