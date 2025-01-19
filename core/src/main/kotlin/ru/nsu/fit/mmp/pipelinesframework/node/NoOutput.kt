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

/**
 * Узел 2-0
 */
class Input2<T1, T2>(
    name: String,
    private val input1: Pair<Pipe<T1>, Pipe<T2>>,
    private val actions: suspend (Pipe<T1>.Consumer, Pipe<T2>.Consumer) -> Unit
) : Node(name = name, input = input1.toList(), output = emptyList()) {

    override fun start(coroutineScope: CoroutineScope) {
        assert(isStart)
        isStart = true

        job = coroutineScope.launch {
            actions.invoke(
                consumer(input1.first, coroutineScope), consumer(input1.second, coroutineScope)
            )
        }
    }
}