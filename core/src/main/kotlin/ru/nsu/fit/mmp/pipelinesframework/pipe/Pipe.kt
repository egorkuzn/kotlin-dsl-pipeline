package ru.nsu.fit.mmp.pipelinesframework.pipe

import kotlinx.coroutines.channels.Channel

interface Pipe<E> : Channel<E>, ReceivePipe<E>, SendPipe<E> {
    companion object {
        fun <E> of(channel: Channel<E>): Pipe<E> {

        }
    }
}