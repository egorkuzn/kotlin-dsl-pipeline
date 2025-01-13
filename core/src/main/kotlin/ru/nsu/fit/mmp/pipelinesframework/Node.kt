package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.CoroutineScope
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe

/**
 * @param name Название ноды
 * @param input Входной pipe
 * @param output Выходной pipe
 * @see Pipe
 */
class Node(
    val name: String,
    private val input: List<Pipe<*>>,
    private val output: List<Pipe<*>>,
    val actions: suspend (coroutineScope: CoroutineScope) -> Unit,
) {
    val context: Context = Context()

    inner class Context {
        private val listeners = mutableListOf<(Context) -> Unit>()

        init {
            (input + output).forEach { pipe ->
                pipe.context.onListener {
                    handlePipeContextChange(pipe)
                }
            }
        }

        private fun handlePipeContextChange(pipe: Pipe<*>) {
            //TODO pipe нужен для логирования
            listeners.forEach {
                it.invoke(this)
            }
        }

        fun onListener(action: (context: Context) -> Unit) {
            listeners.add(action)
        }
    }

    fun destroy() {
        input.forEach { it.close() }
        output.forEach { it.close() }
    }
}