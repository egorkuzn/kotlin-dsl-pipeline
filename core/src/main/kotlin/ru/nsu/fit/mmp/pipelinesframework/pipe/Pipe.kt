package ru.nsu.fit.mmp.pipelinesframework.pipe

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.nsu.fit.mmp.pipelinesframework.channel.BufferChannel

/**
 * Класс, представляющий односторонний канал для асинхронной передачи данных
 *
 * @param T Тип данных, передаваемых через канал
 */
class Pipe<T> {
    private val channel = BufferChannel.of<T>();
    val context = Context()

    /**
     * Класс, предоставляющий контекст Pipe
     * Используется для управления слушателями и обработки событий, связанных с буфером
     */
    inner class Context {
        private val listeners = mutableListOf<(Context) -> Unit>()

        init {
            channel.onListenerBuffer {
                handleBufferChange(it)
            }
        }

        /**
         * Регистрация нового слушателя событий контекста
         *
         * @param action Действие, которое выполняется при изменении контекста
         */
        fun onListener(action: (context: Context) -> Unit) {
            listeners.add(action)
        }

        /**
         * Обработчик изменений в буфере и уведомляет всех слушателей
         *
         * @param buffer Текущее состояние буфера
         */
        private fun handleBufferChange(buffer: List<T>) {
            //TODO buffer нужен для логирования
            listeners.forEach {
                it.invoke(this)
            }
        }
    }

    /**
     * Класс, представляющий потребителя данных [T] из канала [Pipe]
     *
     * @param coroutineScope [CoroutineScope], в рамках которого выполняются операции
     */
    inner class Consumer(private val coroutineScope: CoroutineScope) {
        private val receiveChannel = channel
        private val listeners = mutableListOf<(T) -> Unit>()

        fun onListenerUI(action: (value: T) -> Unit) {
            listeners.add(action)
        }


        private fun handleBufferChange(value: T) {
            listeners.forEach {
                it.invoke(value)
            }
        }
        /**
         * Получение следующего элемента из канала [Pipe]
         *
         * @return Следующий элемент из канала
         * @throws Exception В случае ошибки получения
         */
        suspend fun receive(): T {
            return channel.receive().also { handleBufferChange(it) }
        }

        /**
         * Регистрация слушателя, который будет обрабатывать каждый полученный элемент
         *
         * @param action Действие, выполняемое для каждого элемента
         */
        fun onListener(action: suspend (T) -> Unit) {
            coroutineScope.launch {
                for (p in channel) {
                    action(p).also { handleBufferChange(p) }
                }
            }
        }

        /**
         * Объединение текущего потребителя с другим, создавая двойного потребителя [DualPipe.Consumer]
         *
         * @param other Другой потребитель [Consumer] для объединения
         * @return Новый экземпляр [DualPipe.Consumer]
         */
        operator fun <U> plus(other: Pipe<U>.Consumer): DualPipe<T, U>.Consumer {
            return DualPipe(channel, other.receiveChannel).Consumer(coroutineScope)
        }
    }

    /**
     * Класс, представляющий производителя данных [T] в канал [Pipe]
     */
    inner class Producer {
        private val sendChannel = channel
        private val listeners = mutableListOf<(T) -> Unit>()

        fun onListenerUI(action: (value: T) -> Unit) {
            listeners.add(action)
        }


        private fun handleBufferChange(value: T) {
            listeners.forEach {
                it.invoke(value)
            }
        }

        /**
         * Отправка элемента в канал [Pipe]
         *
         * @param value Элемент типа [T] для отправки
         * @throws Exception В случае ошибки отправки
         */
        suspend fun commit(value: T) {
            //TODO Обработка ошибки отправки
            channel.send(value).also { handleBufferChange(value) }
        }

        /**
         * Объединение текущего производителя с другим, создавая двойного производителя [DualPipe.Producer]
         *
         * @param other Другой производитель для объединения.
         * @return Новый экземпляр [DualPipe.Producer].
         */
        operator fun <U> plus(other: Pipe<U>.Producer): DualPipe<T, U>.Producer {
            return DualPipe(channel, other.sendChannel).Producer()
        }
    }

    /**
     * Освобождение ресурсов и закрытие канала
     */
    fun close() {
        channel.close()
        channel.cancel()
    }
}