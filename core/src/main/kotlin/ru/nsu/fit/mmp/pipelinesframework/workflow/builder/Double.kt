package ru.nsu.fit.mmp.pipelinesframework.workflow.builder

import ru.nsu.fit.mmp.pipelinesframework.node.Input2Output2
import ru.nsu.fit.mmp.pipelinesframework.node.Node
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe

/**
 * Конструкция DSL, создающая узел обработки [Node]
 *
 * @param name Название узла
 * @param inputs Входные каналы [Pipe] с данными типа T и Q
 * @param outputs Выходные каналы [Pipe] с данными типа T и Q
 * @param action Лямбда-функция, описывающая логику обработки данных узлом
 */
fun <T1, T2, U1, U2>  WorkflowBuilder.node(
    name: String,
    inputs: Pair<Pipe<T1>, Pipe<T2>>,
    outputs:Pair<Pipe<U1>, Pipe<U2>>,
    action: suspend (
        consumerT1: Pipe<T1>.Consumer,
        consumerT2: Pipe<T2>.Consumer,
        producerU1: Pipe<U1>.Producer,
        producerU2: Pipe<U2>.Producer,
    ) -> Unit
) {
    nodes.add(Input2Output2(name, inputs, outputs, action))
}