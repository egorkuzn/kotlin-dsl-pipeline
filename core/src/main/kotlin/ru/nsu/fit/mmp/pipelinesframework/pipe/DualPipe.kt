package ru.nsu.fit.mmp.pipelinesframework.pipe

import kotlinx.coroutines.*
import ru.nsu.fit.mmp.pipelinesframework.channel.BufferChannel

/**
 * Представляет связку из двух каналов (pipe) для работы с парами данных.
 *
 * @param T Тип данных для первого канала.
 * @param U Тип данных для второго канала.
 * @param channelT Канал для передачи данных типа T.
 * @param channelU Канал для передачи данных типа U.
 */
class DualPipe<T, U>(
    private val channelT: BufferChannel<T>,
    private val channelU: BufferChannel<U>,
) {

    /**
     * Класс, представляющий потребителя (consumer) данных из двух каналов.
     *
     * @param coroutineScope CoroutineScope, в рамках которого выполняются операции.
     */
    inner class Consumer(private val coroutineScope: CoroutineScope) {

        /**
         * Получает пару значений из двух каналов.
         *
         * @return Пара значений (T, U), полученная из каналов.
         * @throws Exception Если получение данных не удалось.
         */
        suspend fun recive(): Pair<T, U> {
            return Pair(channelT.receive(), channelU.receive())
        }

        /**
         * Регистрирует слушателя для обработки пар данных из каналов.
         *
         * @param action Действие, которое выполняется для каждой пары данных (T, U).
         */
        @OptIn(DelicateCoroutinesApi::class)
        fun onListener(action: (T, U) -> Unit) {
            coroutineScope.launch {
                while (!channelT.isClosedForReceive && !channelU.isClosedForReceive) {
                    val c1 = async { channelT.receive() }
                    val c2 = async { channelU.receive() }

                    action.invoke(c1.await(), c2.await())
                }
            }
        }
    }

    /**
     * Класс, представляющий производителя (producer) данных для двух каналов.
     */
    inner class Producer {

        /**
         * Отправляет пару значений в два канала.
         *
         * @param value1 Значение для первого канала (T).
         * @param value2 Значение для второго канала (U).
         * @throws Exception Если отправка данных не удалась.
         */
        suspend fun commit(value1: T, value2: U) {
            channelT.send(value1)
            channelU.send(value2)
        }
    }
}