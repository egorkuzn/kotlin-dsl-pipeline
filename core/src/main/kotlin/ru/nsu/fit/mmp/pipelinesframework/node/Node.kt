package ru.nsu.fit.mmp.pipelinesframework.node

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe
import java.util.*

/**
 * Представляет узел (node), который работает с входными и выходными каналами (pipe).
 *
 * @param name Название ноды.
 * @param input Список входных каналов (Pipe), через которые поступают данные.
 * @param output Список выходных каналов (Pipe), через которые отправляются данные.
 * @see Pipe
 */
sealed class Node(
    private val id: Long = Random().nextLong(),
    val name: String,
    val input: List<Pipe<*>>,
    val output: List<Pipe<*>>,
) {
    private val currentContext = mutableMapOf<Long, Pipe.Context>()
    private val contextListeners = mutableListOf<(Context) -> Unit>()
    private val context get() = Context(id, buffer.map { it.toString() }, currentContext.map { it.value })

    // Элементы, которые обрабатывает узел
    val buffer = mutableListOf<Any?>()

    internal var isStart = false
    internal var job: Job? = null

    /**
     * Обработчик обновления контекстов
     * @param context Обновленный контекст канала [Pipe.Context]
     */
    internal fun handlePipeContextChange(context: Pipe.Context) {
        currentContext[context.id] = context
        for (listener in contextListeners) {
            listener.invoke(this.context)
        }
    }

    /**
     * Регистрация обработчиков обновлений контекста
     * @param action Обработчик
     */
    fun onContextListener(action: (context: Context) -> Unit) {
        contextListeners.add(action)
    }

    /**
     * Класс, представляющий контекст узла.
     * Управляет слушателями и реагирует на изменения в контексте связанных каналов.
     *
     * @param id Индефикатор узла
     * @param buffer Элементы, которые обрабатывает узел [Node]
     * @param pipesContext Список контекстов каналов [Pipe.Context]
     */
    data class Context(val id: Long, val buffer: List<String>, val pipesContext: List<Pipe.Context>)


    /**
     * Узел 3-3
     */
    class Input3Output3<T1, T2, T3, U1, U2, U3>(
        name: String,
        private val input1: Triple<Pipe<T1>, Pipe<T2>, Pipe<T3>>,
        private val output1: Triple<Pipe<U1>, Pipe<U2>, Pipe<U3>>,
        private val actions: suspend (
            consumerT1: Pipe<T1>.Consumer,
            consumerT2: Pipe<T2>.Consumer,
            consumerT3: Pipe<T3>.Consumer,
            producerU1: Pipe<U1>.Producer,
            producerU2: Pipe<U2>.Producer,
            producerU3: Pipe<U3>.Producer,
        ) -> Unit
    ) : Node(name = name, input = input1.toList(), output = output1.toList()) {

        override fun start(coroutineScope: CoroutineScope) {
            assert(isStart)
            isStart = true

            job = coroutineScope.launch {
                actions.invoke(
                    consumer(input1.first, coroutineScope),
                    consumer(input1.second, coroutineScope),
                    consumer(input1.third, coroutineScope),
                    producer(output1.first),
                    producer(output1.second),
                    producer(output1.third),
                )
            }
        }
    }

    /**
     * Запуск конвейера
     *
     * @param coroutineScope Контекст корутины [CoroutineScope], в котором запускается узел [Node]
     */
    abstract fun start(coroutineScope: CoroutineScope)

    fun stop() {
        assert(!isStart)
        job?.cancel()
        destroy()
    }

    internal fun <T> consumer(pipe: Pipe<T>, coroutineScope: CoroutineScope): Pipe<T>.Consumer {
        val consumer = pipe.Consumer(coroutineScope)
        consumer.onContextListener { c, v ->
            buffer.add(v)
            handlePipeContextChange(c)
        }
        return consumer;
    }

    internal fun <T> producer(pipe: Pipe<T>): Pipe<T>.Producer {
        val producer = pipe.Producer()
        producer.onContextListener { c, v ->
            buffer.remove(v)
            handlePipeContextChange(c)
        }
        return producer;
    }

    /**
     * Уничтожает узел, закрывая все связанные каналы.
     */
    private fun destroy() {
        input.forEach { it.close() }
        output.forEach { it.close() }
    }
}