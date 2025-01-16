package ru.nsu.fit.mmp.pipelinesframework.channel

import kotlinx.coroutines.channels.SendChannel

/**
 * Интерфейс, представляющий канал для записи с буферизацией, наследующий функциональность [SendChannel]
 *
 * @param E Тип элементов, которые могут быть отправлены в канал
 */
interface SendBufferChannel<in E>: SendChannel<E>
