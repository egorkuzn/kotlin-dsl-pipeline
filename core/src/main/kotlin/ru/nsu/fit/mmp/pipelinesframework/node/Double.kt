package ru.nsu.fit.mmp.pipelinesframework.node

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe

/**
 * Узел 2-2
 */
class Input2Output2<T1, T2, U1, U2>(
    name: String,
    private val input1: Pair<Pipe<T1>, Pipe<T2>>,
    private val output1: Pair<Pipe<U1>, Pipe<U2>>,
    private val actions: suspend (
        consumerT1: Pipe<T1>.Consumer,
        consumerT2: Pipe<T2>.Consumer,
        producerU1: Pipe<U1>.Producer,
        producerU2: Pipe<U2>.Producer,
    ) -> Unit
) : Node(name = name, input = input1.toList(), output = output1.toList()) {

    override fun start(coroutineScope: CoroutineScope) {
        assert(isStart)
        isStart = true

        job = coroutineScope.launch {
            actions.invoke(
                consumer(input1.first, coroutineScope),
                consumer(input1.second, coroutineScope),
                producer(output1.first),
                producer(output1.second),
            )
        }
    }
}