package ru.nsu.fit.mmp.pipelinesframework.node

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe

/**
 * Узел 1-0
 */
class Input1<T>(
    name: String,
    private val input1: Pipe<T>,
    private val actions: suspend (Pipe<T>.Consumer) -> Unit
) : Node(name = name, input = listOf(input1), output = emptyList()) {

    override fun start(coroutineScope: CoroutineScope) {
        assert(isStart)
        isStart = true

        job = coroutineScope.launch {
            actions.invoke(
                consumer(input1, coroutineScope)
            )
        }
    }
}