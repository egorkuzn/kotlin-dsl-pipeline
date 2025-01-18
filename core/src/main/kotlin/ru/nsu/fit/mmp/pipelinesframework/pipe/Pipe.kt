package ru.nsu.fit.mmp.pipelinesframework.pipe

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.nsu.fit.mmp.pipelinesframework.channel.BufferChannel
import java.util.*

/**
 * Класс, представляющий односторонний канал для асинхронной передачи данных
 *
 * @param T Тип данных, передаваемых через канал
 */
class Pipe<T> {
    private val channel = BufferChannel.of<T>();
    private val id = Random().nextLong()

    private val buffer = mutableListOf<T>()
    private var stateBuffer = 0L

    private val lock = Any()

    private val context get() = Context(id, stateBuffer, buffer.map { it.toString() })

    /**
     * Класс, представляющий контекст канала [Pipe]
     *
     * @param id Индефикатор канала
     * @param state Номер состояния
     * @param buffer Список элементов из буффера
     */
    data class Context(val id: Long, val state: Long, val buffer: List<String>)

    /**
     * Класс, представляющий потребителя данных [T] из канала [Pipe]
     *
     * @param coroutineScope [CoroutineScope], в рамках которого выполняются операции
     */
    inner class Consumer(private val coroutineScope: CoroutineScope) {
        private val receiveChannel = channel
        private val contextListeners = mutableListOf<(Context, T) -> Unit>()

        /**
         * Добавление обработчиков изменения буффера
         *
         * @param action Обработчик
         */
        fun onContextListener(action: (Context, T) -> Unit) {
            contextListeners.add(action)
        }

        /**
         * Обработчик изменений состояния буффера
         *
         * @param value Новое значение
         */
        private fun handleBufferChange(value: T) {
            contextListeners.forEach {
                synchronized(lock) {
                    buffer.remove(value)
                    stateBuffer++
                    it.invoke(context, value)
                }
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
        private val contextListeners = mutableListOf<(Context, T) -> Unit>()

        /**
         * Добавление обработчиков изменения буффера
         *
         * @param action Обработчик
         */
        fun onContextListener(action: (Context, T) -> Unit) {
            contextListeners.add(action)
        }

        /**
         * Обработчик изменений состояния буффера
         *
         * @param value Новое значение
         */
        private fun handleBufferChange(value: T) {
            contextListeners.forEach {
                synchronized(lock) {
                    buffer.add(value)
                    stateBuffer++
                    it.invoke(context, value)
                }
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