package ru.nsu.fit.mmp.pipelinesframework.node

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe

/**
 * Узел 1-1
 */
class Input1Output1<T, U>(
    name: String,
    private val input1: Pipe<T>,
    private val output1: Pipe<U>,
    private val actions: suspend (Pipe<T>.Consumer, Pipe<U>.Producer) -> Unit
) : Node(name = name, input = listOf(input1), output = listOf(output1)) {

    override fun start(coroutineScope: CoroutineScope) {
        assert(isStart)
        isStart = true

        job = coroutineScope.launch {
            actions.invoke(
                consumer(input1, coroutineScope),
                producer(output1),
            )
        }
    }
}

/**
 * Узел 1-2
 */
class Input1Output2<T, U1, U2>(
    name: String,
    private val input1: Pipe<T>,
    private val output1: Pair<Pipe<U1>, Pipe<U2>>,
    private val actions: suspend (
        Pipe<T>.Consumer,
        Pipe<U1>.Producer,
        Pipe<U2>.Producer,
    ) -> Unit
) : Node(name = name, input = listOf(input1), output = output1.toList()) {

    override fun start(coroutineScope: CoroutineScope) {
        assert(isStart)
        isStart = true

        job = coroutineScope.launch {
            actions.invoke(
                consumer(input1, coroutineScope),
                producer(output1.first),
                producer(output1.second)
            )
        }
    }
}

/**
 * Узел 1-2
 */
class Input2Output1<T1, T2,  U>(
    name: String,
    private val input1: Pair<Pipe<T1>, Pipe<T2>>,
    private val output1: Pipe<U>,
    private val actions: suspend (
        Pipe<T1>.Consumer,
        Pipe<T2>.Consumer,
        Pipe<U>.Producer

    ) -> Unit
) : Node(name = name, input = input1.toList(), output = listOf(output1)) {

    override fun start(coroutineScope: CoroutineScope) {
        assert(isStart)
        isStart = true

        job = coroutineScope.launch {
            actions.invoke(
                consumer(input1.first, coroutineScope),
                consumer(input1.second, coroutineScope),
                producer(output1)
            )
        }
    }
}