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
     * @param outputs Выходной канал [Pipe] с данными типа Q
     * @param action Лямбда-функция, описывающая логику обработки данных узлом
     */
    fun <T, Q> node(
        name: String,
        inputs: Pipe<T>,
        outputs: Pipe<Q>,
        action: suspend (Pipe<T>.Consumer, Pipe<Q>.Producer) -> Unit
    ) {
        nodes.add(Node.Input1Output1(name, inputs, outputs, action))
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
        nodes.add(Node.Output1(name, output, action))
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
        nodes.add(Node.Input1(name, input, action))
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
    fun build(countStackContext: Int, dispatcher: CoroutineDispatcher): Workflow {
        return Workflow(nodes, countStackContext, dispatcher)
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
 * @param dispatcher Диспетчер корутин [CoroutineDispatcher] для управления асинхронными задачами (по умолчанию [Dispatchers.Default])
 * @param init Контент конвейера
 * @return Новый экземпляр конвейера [Workflow]
 */
fun Workflow(
    countStackContext: Int = 100,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    init: WorkflowBuilder.() -> Unit,
): Workflow {
    return WorkflowBuilder().apply(init).build(countStackContext, dispatcher)
}