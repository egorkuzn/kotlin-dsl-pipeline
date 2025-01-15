package ru.nsu.fit.mmp.pipelinesframework.pipe

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.nsu.fit.mmp.pipelinesframework.channel.BufferChannel

/**
 * Генерик-класс, представляющий односторонний канал для асинхронной передачи данных.
 *
 * @param T Тип данных, передаваемых через pipe.
 */
class Pipe<T> {
    private val channel = BufferChannel.of<T>();
    val context = Context()

    /**
     * Класс, предоставляющий контекст Pipe
     * Используется для управления слушателями и обработки событий, связанных с буфером.
     */
    inner class Context {
        private val listeners = mutableListOf<(Context) -> Unit>()

        init {
            channel.onListenerBuffer {
                handleBufferChange(it)
            }
        }

        /**
         * Регистрирует нового слушателя для реагирования на события контекста.
         *
         * @param action Действие, которое выполняется при изменении контекста.
         */
        fun onListener(action: (context: Context) -> Unit) {
            listeners.add(action)
        }

        /**
         * Обрабатывает изменения в буфере и уведомляет всех слушателей.
         *
         * @param buffer Текущее состояние буфера.
         */
        private fun handleBufferChange(buffer: List<T>) {
            //TODO buffer нужен для логирования
            listeners.forEach {
                it.invoke(this)
            }
        }
    }

    /**
     * Класс, представляющий потребителя (consumer) данных.
     *
     * @param coroutineScope [CoroutineScope], в рамках которого выполняются операции.
     */
    inner class Consumer(private val coroutineScope: CoroutineScope) {
        private val receiveChannel = channel

        /**
         * Получает следующий элемент из канала.
         *
         * @return Следующий элемент из канала.
         * @throws Exception В случае ошибки получения.
         */
        suspend fun receive(): T {
            return channel.receive()
        }

        /**
         * Регистрирует слушателя, который будет обрабатывать каждый полученный элемент.
         *
         * @param action Действие, выполняемое для каждого элемента.
         */
        fun onListener(action: suspend (T) -> Unit) {
            coroutineScope.launch {
                for (p in channel) {
                    action(p)
                }
            }
        }

        /**
         * Объединяет этого потребителя с другим, создавая двойного потребителя.
         *
         * @param other Другой потребитель для объединения.
         * @return Новый экземпляр [DualPipe.Consumer].
         */
        operator fun <U> plus(other: Pipe<U>.Consumer): DualPipe<T, U>.Consumer {
            return DualPipe(channel, other.receiveChannel).Consumer(coroutineScope)
        }
    }

    /**
     * Класс, представляющий производителя (producer) данных.
     */
    inner class Producer {
        private val sendChannel = channel

        /**
         * Отправляет элемент в канал.
         *
         * @param value Элемент для отправки.
         * @throws Exception В случае ошибки отправки.
         */
        suspend fun commit(value: T) {
            //TODO Обработка ошибки отправки
            channel.send(value)
        }

        /**
         * Объединяет этого производителя с другим, создавая двойного производителя.
         *
         * @param other Другой производитель для объединения.
         * @return Новый экземпляр [DualPipe.Producer].
         */
        operator fun <U> plus(other: Pipe<U>.Producer): DualPipe<T, U>.Producer {
            return DualPipe(channel, other.sendChannel).Producer()
        }
    }

    /**
     * Закрывает pipe и освобождает все связанные ресурсы.
     */
    fun close() {
        channel.close()
        channel.cancel()
    }
}