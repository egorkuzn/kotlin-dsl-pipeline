package ru.nsu.fit.mmp.pipelinesframework.workflow

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import ru.nsu.fit.mmp.pipelinesframework.Node
import ru.nsu.fit.mmp.pipelinesframework.pipe.Pipe

class WorkflowBuilder {
    private val nodes = mutableListOf<Node>()

    companion object {
        const val ERROR_MESSAGE = """
            Количество элементов, которые возвращает action, не соответсвует количеству каналов-получателей
        """
    }

    /**
     * Многие-ко-многим
     * А -> x
     * B -> x
     * C -> xxxx
     */
    fun <T, Q> node(
        name: String,
        inputs: Pipe<T>,
        outputs: Pipe<Q>,
        action: suspend (Pipe<T>.Consumer, Pipe<Q>.Producer) -> Unit
    ) {
        nodes.add(Node(name, listOf(inputs), listOf(outputs)) {
            action.invoke(inputs.Consumer(it), outputs.Producer())
        })
    }

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
     * Вывод
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

    fun sharedWorkflow(sharedWorkflow: () -> SharedWorkflow) {
        sharedWorkflow.invoke().getNodes().forEach(nodes::add)
    }

    fun build(dispatcher: CoroutineDispatcher): Workflow {
        return Workflow(nodes, dispatcher)
    }

    fun buildSharedWorkflow(): SharedWorkflow {
        return SharedWorkflow(nodes)
    }

}

fun Workflow(
    dispatcher: CoroutineDispatcher = Dispatchers.Default, init: WorkflowBuilder.() -> Unit,
): Workflow {
    return WorkflowBuilder().apply(init).build(dispatcher)
}