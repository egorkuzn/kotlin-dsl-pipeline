package ru.nsu.fit.mmp.pipelinesframework.node

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe

/**
 * Узел 0-1
 */
class Output1<U>(
    name: String,
    private val output1: Pipe<U>,
    private val actions: suspend (Pipe<U>.Producer) -> Unit
) : Node(name = name, input = emptyList(), output = listOf(output1)) {

    override fun start(coroutineScope: CoroutineScope) {
        assert(isStart)
        isStart = true

        job = coroutineScope.launch {
            actions.invoke(
                producer(output1)
            )
        }
    }
}
