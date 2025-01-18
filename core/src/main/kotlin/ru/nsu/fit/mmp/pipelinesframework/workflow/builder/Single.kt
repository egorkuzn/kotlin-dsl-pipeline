package ru.nsu.fit.mmp.pipelinesframework.workflow.builder

import ru.nsu.fit.mmp.pipelinesframework.node.Input1Output1
import ru.nsu.fit.mmp.pipelinesframework.node.Node
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe

/**
 * Конструкция DSL, создающая узел обработки [Node]
 *
 * @param name Название узла
 * @param input Входной канал [Pipe] с данными типа T
 * @param output Выходной канал [Pipe] с данными типа U
 * @param action Лямбда-функция, описывающая логику обработки данных узлом
 */
fun <T, U> WorkflowBuilder.node(
    name: String,
    input: Pipe<T>,
    output: Pipe<U>,
    action: suspend (Pipe<T>.Consumer, Pipe<U>.Producer) -> Unit
) {
    nodes.add(Input1Output1(name, input, output, action))
}
