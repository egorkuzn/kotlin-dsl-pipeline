package ru.nsu.fit.mmp.pipelinesframework

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
    private val current = mutableMapOf<Pipe<*>, Pipe.Context<*>>()
    private val contextListeners = mutableListOf<(Context) -> Unit>()
    private val context get() = Context(id, ArrayList(buffer), current.map { it.value })

    val buffer = mutableListOf<Any?>()

    internal var isStart = false
    internal var job: Job? = null

    internal fun handlePipeContextChange(pipe: Pipe<*>, context: Pipe.Context<*>) {
        current[pipe] = context
        for (listener in contextListeners) {
            listener.invoke(this.context)
        }
    }

    fun onContextListener(action: (context: Context) -> Unit) {
        contextListeners.add(action)
    }

    /**
     * Класс, представляющий контекст узла.
     * Управляет слушателями и реагирует на изменения в контексте связанных каналов.
     */
    data class Context(val id: Long, val buffer: List<Any?>, val pipesContext: List<Pipe.Context<*>>)

    class Input1Output1<T, U>(
        name: String,
        private val input1: Pipe<T>,
        private val output1: Pipe<U>,
        private val actions: suspend (Pipe<T>.Consumer, Pipe<U>.Producer) -> Unit
    ) : Node(name = name, input = listOf(input1), output = listOf(output1)) {

        override fun start(coroutineScope: CoroutineScope) {
            assert(isStart)
            isStart = true

            val costumer = input1.Consumer(coroutineScope)
            costumer.onListenerUI { c, v ->
                buffer.add(v)
                handlePipeContextChange(input1, c)
                println("$name read $v with $c")
            }

            val producer = output1.Producer()
            producer.onListenerUI { c, v ->
                buffer.remove(v)
                handlePipeContextChange(output1, c)
                println("$name write $v with $c")
            }

            job = coroutineScope.launch {
                actions.invoke(costumer, producer)
            }
        }
    }

    class Input1<T>(
        name: String,
        private val input1: Pipe<T>,
        private val actions: suspend (Pipe<T>.Consumer) -> Unit
    ) : Node(name = name, input = listOf(input1), output =  emptyList()) {

        override fun start(coroutineScope: CoroutineScope) {
            assert(isStart)
            isStart = true

            val costumer = input1.Consumer(coroutineScope)
            costumer.onListenerUI { c, v ->
                buffer.add(v)
                handlePipeContextChange(input1, c)
                println("$name read $v with $c")
            }
            job = coroutineScope.launch {
                actions.invoke(costumer)
            }
        }
    }

    class Output1<U>(
        name: String,
        private val output1: Pipe<U>,
        private val actions: suspend (Pipe<U>.Producer) -> Unit
    ) : Node(name = name, input =  emptyList(), output = listOf(output1)) {

        override fun start(coroutineScope: CoroutineScope) {
            assert(isStart)
            isStart = true

            val producer = output1.Producer()
            producer.onListenerUI { c, v ->
                buffer.remove(v)
                handlePipeContextChange(output1, c)
                println("$name write $v with $c")
            }

            job = coroutineScope.launch {
                actions.invoke(producer)
            }
        }
    }

    abstract fun start(coroutineScope: CoroutineScope)

    fun stop() {
        assert(!isStart)
        job?.cancel()
        destroy()
    }

    /**
     * Уничтожает узел, закрывая все связанные каналы.
     */
    private fun destroy() {
        input.forEach { it.close() }
        output.forEach { it.close() }
    }
}