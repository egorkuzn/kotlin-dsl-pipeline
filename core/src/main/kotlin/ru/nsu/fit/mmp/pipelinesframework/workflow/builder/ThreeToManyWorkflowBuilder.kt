package ru.nsu.fit.mmp.pipelinesframework.workflow.builder

import ru.nsu.fit.mmp.pipelinesframework.Node
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe

interface ThreeToManyWorkflowBuilder: WorkflowBuilderBase {
    /**
     * Конструкция DSL, создающая узел обработки [Node]
     *
     * @param name Название узла
     * @param inputs Входные каналы [Pipe] с данными типа T, Q и M
     * @param outputs Выходной канал [Pipe] с данными типа T
     * @param action Лямбда-функция, описывающая логику обработки данных узлом
     */
    fun <T, Q, M> node(
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
     * @param outputs Выходной канал [Pipe] с данными типа Q
     * @param action Лямбда-функция, описывающая логику обработки данных узлом
     */
    fun <T, Q, M> node(
        name: String,
        inputs: Triple<Pipe<T>, Pipe<Q>, Pipe<M>>,
        outputs: Pipe<Q>,
        action: suspend (
            Pipe<T>.Consumer,
            Pipe<Q>.Consumer,
            Pipe<M>.Consumer,
            Pipe<Q>.Producer,
        ) -> Unit,
    ) {
        node(
            name,
            inputs,
            Triple(Pipe(), outputs, Pipe())
        ) { consumerT, consumerQ, consumerM, _, producerQ, _ ->
            action.invoke(
                consumerT,
                consumerQ,
                consumerM,
                producerQ
            )
        }
    }


    /**
     * Конструкция DSL, создающая узел обработки [Node]
     *
     * @param name Название узла
     * @param inputs Входные каналы [Pipe] с данными типа T, Q и M
     * @param outputs Выходной канал [Pipe] с данными типа M
     * @param action Лямбда-функция, описывающая логику обработки данных узлом
     */
    fun <T, Q, M> node(
        name: String,
        inputs: Triple<Pipe<T>, Pipe<Q>, Pipe<M>>,
        outputs: Pipe<M>,
        action: suspend (
            Pipe<T>.Consumer,
            Pipe<Q>.Consumer,
            Pipe<M>.Consumer,
            Pipe<M>.Producer,
        ) -> Unit,
    ) {
        node(
            name,
            Triple(inputs.first, inputs.second, Pipe()),
            Triple(Pipe(), Pipe(), outputs)
        ) { consumerT, consumerQ, consumerM, _, _, producerM ->
            action.invoke(
                consumerT,
                consumerQ,
                consumerM,
                producerM
            )
        }
    }


    /**
     * Конструкция DSL, создающая узел обработки [Node]
     *
     * @param name Название узла
     * @param inputs Входные каналы [Pipe] с данными типа T, Q и M
     * @param outputs Выходной канал [Pipe] с данными типа T и M
     * @param action Лямбда-функция, описывающая логику обработки данных узлом
     */
    fun <T, Q, M> node(
        name: String,
        inputs: Triple<Pipe<T>, Pipe<Q>, Pipe<M>>,
        outputs: Pair<Pipe<T>, Pipe<M>>,
        action: suspend (
            Pipe<T>.Consumer,
            Pipe<Q>.Consumer,
            Pipe<M>.Consumer,
            Pipe<T>.Producer,
            Pipe<M>.Producer,
        ) -> Unit,
    ) {
        node(
            name,
            Triple(inputs.first, inputs.second, Pipe()),
            Triple(outputs.first, Pipe(), outputs.second)
        ) { consumerT, consumerQ, consumerM, producerT, _, producerM ->
            action.invoke(
                consumerT,
                consumerQ,
                consumerM,
                producerT,
                producerM
            )
        }
    }


    /**
     * Конструкция DSL, создающая узел обработки [Node]
     *
     * @param name Название узла
     * @param inputs Входные каналы [Pipe] с данными типа T, Q и M
     * @param outputs Выходной канал [Pipe] с данными типа Q и M
     * @param action Лямбда-функция, описывающая логику обработки данных узлом
     */
    fun <T, Q, M> node(
        name: String,
        inputs: Triple<Pipe<T>, Pipe<Q>, Pipe<M>>,
        outputs: Pair<Pipe<Q>, Pipe<M>>,
        action: suspend (
            Pipe<T>.Consumer,
            Pipe<Q>.Consumer,
            Pipe<M>.Consumer,
            Pipe<Q>.Producer,
            Pipe<M>.Producer,
        ) -> Unit,
    ) {
        node(
            name,
            Triple(inputs.first, inputs.second, Pipe()),
            Triple(Pipe(), outputs.first, outputs.second)
        ) { consumerT, consumerQ, consumerM, _, producerQ, producerM ->
            action.invoke(
                consumerT,
                consumerQ,
                consumerM,
                producerQ,
                producerM
            )
        }
    }
}
