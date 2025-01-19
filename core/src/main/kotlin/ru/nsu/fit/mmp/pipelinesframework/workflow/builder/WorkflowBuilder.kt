package ru.nsu.fit.mmp.pipelinesframework.workflow.builder

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import ru.nsu.fit.mmp.pipelinesframework.node.*
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe
import ru.nsu.fit.mmp.pipelinesframework.workflow.SharedWorkflow
import ru.nsu.fit.mmp.pipelinesframework.workflow.Workflow

/**
 * Класс для построения DSL конвейерной обработки
 */
class WorkflowBuilder
//    OneToManyWorkflowBuilder,
//    TwoToManyWorkflowBuilder,
//    ThreeToManyWorkflowBuilder
{
    internal val nodes = mutableListOf<Node>()

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
        nodes.add(Node.Input3Output3(name, inputs, outputs, action))
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
        nodes.add(Output1(name, output, action))
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
        nodes.add(Input1(name, input, action))
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
    fun build(
        countStackContext: Int,
        enableSecurityDeadLock: Boolean,
        enabledWarringCyclePipe: Boolean,
        updateContext: (Workflow.Context)->Unit,
        dispatcher: CoroutineDispatcher
    ): Workflow {
        return Workflow(
            nodes = nodes,
            updateContext= updateContext,
            enableSecurityDeadLock = enableSecurityDeadLock,
            enabledWarringCyclePipe = enabledWarringCyclePipe,
            countStackContext = countStackContext,
            dispatcher = dispatcher
        )
    }

    /**
     * Создание экземпляра общего конвейера [SharedWorkflow]
     *
     * @return Новый экземпляр общего конвейера [SharedWorkflow]
     */
    fun buildSharedWorkflow(): SharedWorkflow {
        return SharedWorkflow(nodes = nodes)
    }

}

/**
 * Открывающая DSL конструкция [WorkflowBuilder]
 *
 * @param countStackContext Количество контекстов, хранящиеся в стеке
 * @param enableSecurityDeadLock Включение механизма обнаружения DeadLock
 * @param enabledWarringCyclePipe Включение механизма обнаружения циклов
 * @param dispatcher Диспетчер корутин [CoroutineDispatcher] для управления асинхронными задачами (по умолчанию [Dispatchers.Default])
 * @param init Контент конвейера
 * @return Новый экземпляр конвейера [Workflow]
 */
fun Workflow(
    countStackContext: Int = 100,
    enableSecurityDeadLock: Boolean = false,
    enabledWarringCyclePipe: Boolean = false,
    updateContext: (Workflow.Context)->Unit = {},
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    init: WorkflowBuilder.() -> Unit,
): Workflow {
    return WorkflowBuilder().apply(init)
        .build(countStackContext, enableSecurityDeadLock, enabledWarringCyclePipe,updateContext,  dispatcher)
}