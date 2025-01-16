package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.CoroutineScope
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
class Node(
    val name: String,
    private val input: List<Pipe<*>>,
    private val output: List<Pipe<*>>,
    val actions: suspend (coroutineScope: CoroutineScope) -> Unit,
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

    /**
     * Уничтожает узел, закрывая все связанные каналы.
     */
    fun destroy() {
        input.forEach { it.close() }
        output.forEach { it.close() }
    }
}