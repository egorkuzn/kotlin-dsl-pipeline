package ru.nsu.fit.mmp.pipelinesframework.channel

import kotlinx.coroutines.channels.Channel
import ru.nsu.fit.mmp.pipelinesframework.channel.impl.BufferChannelImpl

/**
 * Интерфейс, представляющий буферизованный канал [Channel]
 *
 * @param E Тип элементов, передаваемых через канал
 */
interface BufferChannel<E> : Channel<E>, ReceiveBufferChannel<E>, SendBufferChannel<E> {
    companion object {
        /**
         * Создание экземпляра реализации [BufferChannel]
         *
         * @return Экземпляр [BufferChannel]
         */
        fun <E> of(): BufferChannel<E> = BufferChannelImpl()
    }

//    /**
//     * Список элементов, находящихся в буфере канала
//     *
//     * @return Список элементов в буфере
//     */
    fun context(): Context<E>

    data class Context<E>(val code: Long, val buffer: List<E>)
}