package ru.nsu.fit.mmp.pipelinesframework.workflow.builder

import ru.nsu.fit.mmp.pipelinesframework.Node
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe
import ru.nsu.fit.mmp.pipelinesframework.workflow.WorkflowBuilder

/**
 * Конструкция DSL, создающая узел обработки [Node]
 *
 * @param name Название узла
 * @param inputs Входные каналы [Pipe] с данными типа T и Q
 * @param outputs Выходной канал [Pipe] с данными типа M
 * @param action Лямбда-функция, описывающая логику обработки данных узлом
 */
fun <T, Q, M> WorkflowBuilder.node(
    name: String,
    inputs: Pair<Pipe<T>, Pipe<Q>>,
    outputs: Pipe<M>,
    action: suspend (
        Pipe<T>.Consumer,
        Pipe<Q>.Consumer,
        Pipe<M>.Producer,
    ) -> Unit,
) {
    node(
        name,
        Triple(inputs.first, inputs.second, Pipe()),
        Triple(Pipe(), Pipe(), outputs)
    ) { consumerT, consumerQ, _, _, _, producerM ->
        action.invoke(
            consumerT,
            consumerQ,
            producerM
        )
    }
}


/**
 * Конструкция DSL, создающая узел обработки [Node]
 *
 * @param name Название узла
 * @param inputs Входные каналы [Pipe] с данными типа T и Q
 * @param outputs Выходной канал [Pipe] с данными типа T и M
 * @param action Лямбда-функция, описывающая логику обработки данных узлом
 */
fun <T, Q, M> WorkflowBuilder.node(
    name: String,
    inputs: Pair<Pipe<T>, Pipe<Q>>,
    outputs: Pair<Pipe<T>, Pipe<M>>,
    action: suspend (
        Pipe<T>.Consumer,
        Pipe<Q>.Consumer,
        Pipe<T>.Producer,
        Pipe<M>.Producer,
    ) -> Unit,
) {
    node(
        name,
        Triple(inputs.first, inputs.second, Pipe()),
        Triple(outputs.first, Pipe(), outputs.second)
    ) { consumerT, consumerQ, _, producerT, _, producerM ->
        action.invoke(
            consumerT,
            consumerQ,
            producerT,
            producerM
        )
    }
}


/**
 * Конструкция DSL, создающая узел обработки [Node]
 *
 * @param name Название узла
 * @param inputs Входные каналы [Pipe] с данными типа T и Q
 * @param outputs Выходной канал [Pipe] с данными типа T, Q и M
 * @param action Лямбда-функция, описывающая логику обработки данных узлом
 */
fun <T, Q, M> WorkflowBuilder.node(
    name: String,
    inputs: Pair<Pipe<T>, Pipe<Q>>,
    outputs: Triple<Pipe<T>, Pipe<Q>, Pipe<M>>,
    action: suspend (
        Pipe<T>.Consumer,
        Pipe<Q>.Consumer,
        Pipe<T>.Producer,
        Pipe<Q>.Producer,
        Pipe<M>.Producer,
    ) -> Unit,
) {
    node(
        name,
        Triple(inputs.first, inputs.second, Pipe()),
        Triple(outputs.first, outputs.second, outputs.third)
    ) { consumerT, consumerQ, _, producerT, producerQ, producerM ->
        action.invoke(
            consumerT,
            consumerQ,
            producerT,
            producerQ,
            producerM
        )
    }
}
