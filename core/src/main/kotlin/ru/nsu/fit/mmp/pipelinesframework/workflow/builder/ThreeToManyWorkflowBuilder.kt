package ru.nsu.fit.mmp.pipelinesframework.workflow.builder

import ru.nsu.fit.mmp.pipelinesframework.Node
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe
import ru.nsu.fit.mmp.pipelinesframework.workflow.WorkflowBuilder


/**
 * Конструкция DSL, создающая узел обработки [Node]
 *
 * @param name Название узла
 * @param inputs Входные каналы [Pipe] с данными типа T, Q и M
 * @param outputs Выходной канал [Pipe] с данными типа T
 * @param action Лямбда-функция, описывающая логику обработки данных узлом
 */
fun <T, Q, M> WorkflowBuilder.node(
    name: String,
    inputs: Triple<Pipe<T>, Pipe<Q>, Pipe<M>>,
    outputs: Pipe<T>,
    action: suspend (
        Pipe<T>.Consumer,
        Pipe<Q>.Consumer,
        Pipe<M>.Consumer,
        Pipe<T>.Producer,
    ) -> Unit,
) {
    node(
        name,
        inputs,
        Triple(outputs, Pipe(), Pipe()),
    ) { consumerT, consumerQ, consumerM, producerT, _, _ ->
        action.invoke(
            consumerT,
            consumerQ,
            consumerM,
            producerT
        )
    }
}


/**
 * Конструкция DSL, создающая узел обработки [Node]
 *
 * @param name Название узла
 * @param inputs Входные каналы [Pipe] с данными типа T, Q и M
 * @param outputs Выходной канал [Pipe] с данными типа T и Q
 * @param action Лямбда-функция, описывающая логику обработки данных узлом
 */
fun <T, Q, M> WorkflowBuilder.node(
    name: String,
    inputs: Triple<Pipe<T>, Pipe<Q>, Pipe<M>>,
    outputs: Pair<Pipe<T>, Pipe<Q>>,
    action: suspend (
        Pipe<T>.Consumer,
        Pipe<Q>.Consumer,
        Pipe<M>.Consumer,
        Pipe<T>.Producer,
        Pipe<Q>.Producer,
    ) -> Unit,
) {
    node(
        name,
        Triple(inputs.first, inputs.second, inputs.third),
        Triple(outputs.first, outputs.second, Pipe())
    ) { consumerT, consumerQ, consumerM, producerT, producerQ, _ ->
        action.invoke(
            consumerT,
            consumerQ,
            consumerM,
            producerT,
            producerQ
        )
    }
}
