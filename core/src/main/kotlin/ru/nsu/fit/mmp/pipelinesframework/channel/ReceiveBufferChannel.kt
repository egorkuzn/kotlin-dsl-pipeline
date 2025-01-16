package ru.nsu.fit.mmp.pipelinesframework.channel

import kotlinx.coroutines.channels.ReceiveChannel
import ru.nsu.fit.mmp.pipelinesframework.channel.impl.ReceiveBufferChannelImpl

/**
 * Интерфейс, представляющий канал для чтения с буферизацией, наследующий функциональность [ReceiveChannel]
 *
 * @param E Тип элементов, доступных для чтения из канала
 */
interface ReceiveBufferChannel<out E>: ReceiveChannel<E> {
    companion object {
        /**
         * Создает новый экземпляр реализации [ReceiveBufferChannel]
         *
         * @param receiveChannel Исходный канал [ReceiveChannel], на основе которого создается буферизованный канал
         * @param E Тип элементов в канале
         * @return Экземпляр [ReceiveBufferChannel]
         */
        fun <E> of(receiveChannel: ReceiveChannel<E>): ReceiveBufferChannel<E> = ReceiveBufferChannelImpl(receiveChannel)
    }
}