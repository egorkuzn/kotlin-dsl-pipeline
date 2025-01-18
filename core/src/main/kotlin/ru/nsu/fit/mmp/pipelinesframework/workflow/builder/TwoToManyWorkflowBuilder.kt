package ru.nsu.fit.mmp.pipelinesframework.workflow.builder

import ru.nsu.fit.mmp.pipelinesframework.Node
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe

interface TwoToManyWorkflowBuilder : WorkflowBuilderBase {
    /**
     * Конструкция DSL, создающая узел обработки [Node]
     *
     * @param name Название узла
     * @param inputs Входные каналы [Pipe] с данными типа T и Q
     * @param outputs Выходной канал [Pipe] с данными типа T
     * @param action Лямбда-функция, описывающая логику обработки данных узлом
     */
    fun <T, Q> node(
        name: String,
        inputs: Pair<Pipe<T>, Pipe<Q>>,
        outputs: Pipe<T>,
        action: suspend (
            Pipe<T>.Consumer,
            Pipe<Q>.Consumer,
            Pipe<T>.Producer,
        ) -> Unit,
    ) {
        node(
            name,
            inputs,
            outputs to Pipe()
        ) { consumerT, consumerQ, producerT, _ ->
            action.invoke(consumerT, consumerQ, producerT)
        }
    }

    /**
     * Конструкция DSL, создающая узел обработки [Node]
     *
     * @param name Название узла
     * @param inputs Входные каналы [Pipe] с данными типа T и Q
     * @param outputs Выходной канал [Pipe] с данными типа Q
     * @param action Лямбда-функция, описывающая логику обработки данных узлом
     */
    fun <T, Q> node(
        name: String,
        inputs: Pair<Pipe<T>, Pipe<Q>>,
        outputs: Pipe<Q>,
        action: suspend (
            Pipe<T>.Consumer,
            Pipe<Q>.Consumer,
            Pipe<Q>.Producer,
        ) -> Unit,
    ) {
        node(
            name,
            inputs,
            Pipe<T>() to outputs
        ) { consumerT, consumerQ, _, producerQ ->
            action.invoke(consumerT, consumerQ, producerQ)
        }
    }


    /**
     * Конструкция DSL, создающая узел обработки [Node]
     *
     * @param name Название узла
     * @param inputs Входные каналы [Pipe] с данными типа T и Q
     * @param outputs Выходной канал [Pipe] с данными типа M
     * @param action Лямбда-функция, описывающая логику обработки данных узлом
     */
    fun <T, Q, M> node(
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
    fun <T, Q, M> node(
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
     * @param outputs Выходной канал [Pipe] с данными типа Q и M
     * @param action Лямбда-функция, описывающая логику обработки данных узлом
     */
    fun <T, Q, M> node(
        name: String,
        inputs: Pair<Pipe<T>, Pipe<Q>>,
        outputs: Pair<Pipe<Q>, Pipe<M>>,
        action: suspend (
            Pipe<T>.Consumer,
            Pipe<Q>.Consumer,
            Pipe<Q>.Producer,
            Pipe<M>.Producer,
        ) -> Unit,
    ) {
        node(
            name,
            Triple(inputs.first, inputs.second, Pipe()),
            Triple(Pipe(), outputs.first, outputs.second)
        ) { consumerT, consumerQ, _, _, producerQ, producerM ->
            action.invoke(
                consumerT,
                consumerQ,
                producerQ,
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
    fun <T, Q, M> node(
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
}