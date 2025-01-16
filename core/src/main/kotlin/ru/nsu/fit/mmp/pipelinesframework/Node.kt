package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe


/**
 * Представляет узел (node), который работает с входными и выходными каналами (pipe).
 *
 * @param name Название ноды.
 * @param input Список входных каналов (Pipe), через которые поступают данные.
 * @param output Список выходных каналов (Pipe), через которые отправляются данные.
 * @param actions Лямбда-функция, определяющая действия, выполняемые узлом.
 * @see Pipe
 */
sealed class Node(
    val name: String,
    private val input: List<Pipe<*>>,
    private val output: List<Pipe<*>>,
    //  private val actions: suspend (coroutineScope: CoroutineScope) -> Unit,
) {
    val context: Context = Context()

    /**
     * Класс, представляющий контекст узла.
     * Управляет слушателями и реагирует на изменения в контексте связанных каналов.
     */
    inner class Context {
        private val listeners = mutableListOf<(Context) -> Unit>()

        init {
            (input + output).forEach { pipe ->
                pipe.context.onListener {
                    handlePipeContextChange(pipe)
                }
            }
        }

        /**
         * Обрабатывает изменения контекста в переданном канале.
         *
         * @param pipe Канал, в контексте которого произошли изменения.
         */
        private fun handlePipeContextChange(pipe: Pipe<*>) {
            //TODO pipe нужен для логирования
            listeners.forEach {
                it.invoke(this)
            }
        }

        /**
         * Регистрирует слушателя, который будет реагировать на изменения контекста узла.
         *
         * @param action Действие, выполняемое при изменении контекста.
         */
        fun onListener(action: (context: Context) -> Unit) {
            listeners.add(action)
        }
    }

    class Input1Output1<T, U>(
        name: String,
        private val input: Pipe<T>,
        private val output: Pipe<U>,
        private val  actions: suspend (Pipe<T>.Consumer, Pipe<U>.Producer) -> Unit
    ) : Node(name, listOf(input), listOf(output)) {

        override fun start(coroutineScope: CoroutineScope): Job {
            return coroutineScope.launch {
                actions.invoke(input.Consumer(coroutineScope), output.Producer())
            }
        }
    }

    class Input1<T>(
        name: String,
        private val input: Pipe<T>,
        private val  actions: suspend (Pipe<T>.Consumer) -> Unit
    ) : Node(name, listOf(input), emptyList()) {

        override fun start(coroutineScope: CoroutineScope): Job {
            return coroutineScope.launch {
                actions.invoke(input.Consumer(coroutineScope))
            }
        }
    }

    class Output1<U>(
        name: String,
        private val output: Pipe<U>,
        private val actions: suspend (Pipe<U>.Producer) -> Unit
    ) : Node(name, emptyList(), listOf(output)) {

        override fun start(coroutineScope: CoroutineScope): Job {
            return coroutineScope.launch {
                actions.invoke(output.Producer())
            }
        }
    }

    abstract fun start(coroutineScope: CoroutineScope): Job

    /**
     * Уничтожает узел, закрывая все связанные каналы.
     */
    fun destroy() {
        input.forEach { it.close() }
        output.forEach { it.close() }
    }
}