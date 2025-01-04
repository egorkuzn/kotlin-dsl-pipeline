package ru.nsu.fit.mmp.pipelinesframework.pipe

import kotlinx.coroutines.channels.ReceiveChannel
import ru.nsu.fit.mmp.pipelinesframework.pipe.impl.ReceivePipeImpl

interface ReceivePipe<out E>: ReceiveChannel<E> {
    companion object {
        fun <E> of(receiveChannel: ReceiveChannel<E>): ReceivePipe<E> = ReceivePipeImpl(receiveChannel)
    }
}