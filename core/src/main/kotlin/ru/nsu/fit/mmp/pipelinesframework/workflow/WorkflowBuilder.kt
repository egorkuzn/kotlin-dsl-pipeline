package ru.nsu.fit.mmp.pipelinesframework.workflow

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import ru.nsu.fit.mmp.pipelinesframework.Node
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe

/**
 * Класс для построения DSL конвейерной обработки
 */
class WorkflowBuilder {
    private val nodes = mutableListOf<Node>()

    /**
     * Конструкция DSL, создающая узел обработки [Node]
     *
     * @param name Название узла
     * @param inputs Входной канал [Pipe] с данными типа T
     * @param outputs Выходной канал [Pipe] с данными типа T
     * @param action Лямбда-функция, описывающая логику обработки данных узлом
     */
    fun <T> node(
        name: String,
        inputs: Pipe<T>,
        outputs: Pipe<T>,
        action: suspend (Pipe<T>.Consumer, Pipe<T>.Producer) -> Unit
    ) {
        nodes.add(Node(name, listOf(inputs), listOf(outputs)) {
            action.invoke(inputs.Consumer(it), outputs.Producer())
        })
    }

    /**
     * Конструкция DSL, создающая узел обработки [Node]
     *
     * @param name Название узла
     * @param inputs Входные каналы [Pipe] с данными типа T и Q
     * @param outputs Выходные каналы [Pipe] с данными типа T и Q
     * @param action Лямбда-функция, описывающая логику обработки данных узлом
     */
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
    ) {
        nodes.add(Node(
            name,
            inputs.toList(),
            outputs.toList()
        ) {
            action.invoke(
                inputs.first.Consumer(it),
                inputs.second.Consumer(it),
                outputs.first.Producer(),
                outputs.second.Producer()
            )
        })
    }

    /**
     * Конструкция DSL, создающая узел обработки [Node]
     *
     * @param name Название узла
     * @param inputs Входные каналы [Pipe] с данными типа T, Q и M
     * @param outputs Выходные каналы [Pipe] с данными типа T, Q и M
     * @param action Лямбда-функция, описывающая логику обработки данных узлом
     */
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
    ) {
        nodes.add(Node(
            name,
            inputs.toList(),
            outputs.toList()
        ) {
            action.invoke(
                inputs.first.Consumer(it),
                inputs.second.Consumer(it),
                inputs.third.Consumer(it),
                outputs.first.Producer(),
                outputs.second.Producer(),
                outputs.third.Producer()
            )
        })
    }

    /**
     * Конструкция DSL, создающая начальный узел обработки [Node] без входных каналов [Pipe]
     *
     * @param name Название узла
     * @param output Выходной канал [Pipe] с данными типа T
     * @param action Лямбда-функция, описывающая логику обработки данных узлом
     */
    fun <T> initial(
        name: String,
        output: Pipe<T>,
        action: suspend (Pipe<T>.Producer) -> Unit,
    ) {
        nodes.add(Node(name, emptyList(), listOf(output)) {
            action.invoke(output.Producer())
        })
    }

    /**
     * Конструкция DSL, создающая конечный узел обработки [Node] без выходных каналов [Pipe]
     *
     * @param name Название узла
     * @param input Входной канал [Pipe] с данными типа T
     * @param action Лямбда-функция, описывающая логику обработки данных узлом
     */
    fun <T> terminate(
        name: String,
        input: Pipe<T>,
        action: suspend (Pipe<T>.Consumer) -> Unit,
    ) {
        nodes.add(Node(name, listOf(input), emptyList()) {
            action.invoke(input.Consumer(it))
        })
    }

    /**
     * Конструкция DSL, добавляющая в конвейер узлы [Node] из общего конвейера [SharedWorkflow]
     *
     * @param sharedWorkflow Общий конвейер [SharedWorkflow]
     */
    fun sharedWorkflow(sharedWorkflow: () -> SharedWorkflow) {
        sharedWorkflow.invoke().getNodes().forEach(nodes::add)
    }


    /**
     * Создание экземпляра [Workflow] с заданным диспетчером корутин [CoroutineDispatcher]
     *
     * @param dispatcher Диспетчер корутин [CoroutineDispatcher] для управления асинхронными задачами
     * @return Новый экземпляр конвейера [Workflow]
     */
    fun build(dispatcher: CoroutineDispatcher): Workflow {
        return Workflow(nodes, dispatcher)
    }

    /**
     * Создание экземпляра общего конвейера [SharedWorkflow]
     *
     * @return Новый экземпляр общего конвейера [SharedWorkflow]
     */
    fun buildSharedWorkflow(): SharedWorkflow {
        return SharedWorkflow(nodes)
    }

}

/**
 * Открывающая DSL конструкция [WorkflowBuilder]
 *
 * @param dispatcher Диспетчер корутин [CoroutineDispatcher] для управления асинхронными задачами (по умолчанию [Dispatchers.Default])
 * @param init Контент конвейера
 * @return Новый экземпляр конвейера [Workflow]
 */
fun Workflow(
    dispatcher: CoroutineDispatcher = Dispatchers.Default, init: WorkflowBuilder.() -> Unit,
): Workflow {
    return WorkflowBuilder().apply(init).build(dispatcher)
}