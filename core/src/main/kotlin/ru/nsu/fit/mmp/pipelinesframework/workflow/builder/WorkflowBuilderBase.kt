package ru.nsu.fit.mmp.pipelinesframework.workflow.builder

import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe

interface WorkflowBuilderBase {
    fun <T> node(
        name: String,
        inputs: Pipe<T>,
        outputs: Pipe<T>,
        action: suspend (Pipe<T>.Consumer, Pipe<T>.Producer) -> Unit
    )

    fun <T, Q> node(
        name: String,
        inputs: Pair<Pipe<T>, Pipe<Q>>,
        outputs: Pair<Pipe<T>, Pipe<Q>>,
        action: suspend (
            consumerT: Pipe<T>.Consumer,
            consumerQ: Pipe<Q>.Consumer,
            producerT: Pipe<T>.Producer,
            producerQ: Pipe<Q>.Producer,
        ) -> Unit
    )

    fun <T, Q, M> node(
        name: String,
        inputs: Triple<Pipe<T>, Pipe<Q>, Pipe<M>>,
        outputs: Triple<Pipe<T>, Pipe<Q>, Pipe<M>>,
        action: suspend (
            consumerT: Pipe<T>.Consumer,
            consumerQ: Pipe<Q>.Consumer,
            consumerM: Pipe<M>.Consumer,
            producerT: Pipe<T>.Producer,
            producerQ: Pipe<Q>.Producer,
            producerM: Pipe<M>.Producer
        ) -> Unit
    )
}
