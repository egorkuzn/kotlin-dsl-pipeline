package ru.nsu.fit.mmp.pipelinesframework.workflow.builder

import ru.nsu.fit.mmp.pipelinesframework.Node
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe

interface OneToManyWorkflowBuilder: WorkflowBuilderBase {
    /**
     * Конструкция DSL, создающая узел обработки [Node]
     *
     * @param name Название узла
     * @param inputs Входной канал [Pipe] с данными типа T
     * @param outputs Выходной канал [Pipe] с данными типа Q
     * @param action Лямбда-функция, описывающая логику обработки данных узлом
     */
    fun <T, Q> node(
        name: String,
        inputs: Pipe<T>,
        outputs: Pipe<Q>,
        action: suspend (Pipe<T>.Consumer, Pipe<Q>.Producer) -> Unit
    ) {
        node(
            name,
            inputs to Pipe(),
            Pipe<T>() to outputs
        ) { consumerT, _, _, producerQ -> action.invoke(consumerT, producerQ) }
    }


    /**
     * Конструкция DSL, создающая узел обработки [Node]
     *
     * @param name Название узла
     * @param inputs Входной канал [Pipe] с данными типа T
     * @param outputs Выходные каналы [Pipe] с данными типа T и Q
     * @param action Лямбда-функция, описывающая логику обработки данных узлом
     */
    fun <T, Q> node(
        name: String,
        inputs: Pipe<T>,
        outputs: Pair<Pipe<T>, Pipe<Q>>,
        action: suspend (
            Pipe<T>.Consumer,
            Pipe<T>.Producer,
            Pipe<Q>.Producer,
        ) -> Unit
    ) {
        node(
            name,
            inputs to Pipe(),
            outputs
        ) { consumerT, _, producerT, producerQ ->
            action.invoke(
                consumerT,
                producerT,
                producerQ
            )
        }
    }


    /**
     * Конструкция DSL, создающая узел обработки [Node]
     *
     * @param name Название узла
     * @param inputs Входной канал [Pipe] с данными типа T
     * @param outputs Выходные каналы [Pipe] с данными типа Q и M
     * @param action Лямбда-функция, описывающая логику обработки данных узлом
     */
    fun <T, Q, M> node(
        name: String,
        inputs: Pipe<T>,
        outputs: Pair<Pipe<Q>, Pipe<M>>,
        action: suspend (
            Pipe<T>.Consumer,
            Pipe<Q>.Producer,
            Pipe<M>.Producer,
        ) -> Unit
    ) {
        node(
            name,
            Triple(inputs, Pipe(), Pipe() ),
            Triple(Pipe(), outputs.first, outputs.second)
        ) { consumerT,
            _,
            _,
            _,
            producerQ,
            producerM ->
            action(
                consumerT,
                producerQ,
                producerM
            )
        }
    }

    /**
     * Конструкция DSL, создающая узел обработки [Node]
     *
     * @param name Название узла
     * @param inputs Входной канал [Pipe] с данными типа T
     * @param outputs Выходные каналы [Pipe] с данными типа T, Q и M
     * @param action Лямбда-функция, описывающая логику обработки данных узлом
     */
    fun <T, Q, M> node(
        name: String,
        inputs: Pipe<T>,
        outputs: Triple<Pipe<T>, Pipe<Q>, Pipe<M>>,
        action: suspend (
            Pipe<T>.Consumer,
            Pipe<T>.Producer,
            Pipe<Q>.Producer,
            Pipe<M>.Producer,
        ) -> Unit
    ) {
        node(
            name,
            Triple(inputs, Pipe(), Pipe() ),
            Triple(outputs.first, outputs.second, outputs.third)
        ) { consumerT,
            _,
            _,
            producerT,
            producerQ,
            producerM ->
            action(
                consumerT,
                producerT,
                producerQ,
                producerM
            )
        }
    }
}
