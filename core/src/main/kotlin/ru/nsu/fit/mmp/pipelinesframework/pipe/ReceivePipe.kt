package ru.nsu.fit.mmp.pipelinesframework.pipe

import kotlinx.coroutines.channels.ReceiveChannel

interface ReceivePipe<out E>: ReceiveChannel<E> {
    companion object {
        fun <E> of(pproduce: ReceiveChannel<E>): ReceivePipe<E> {

        }
    }
}