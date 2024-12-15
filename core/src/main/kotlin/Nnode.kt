package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.experimental.ExperimentalTypeInference

fun <T, Q> CoroutineScope.nnode(
    name: String,
    input: ReceiveChannel<T>,
    output: SendChannel<Q>,
    action: (T) -> Q,
) {
    launch { for (msg in input) output.send(action(msg)) }
}

fun <T, Q> CoroutineScope.nnode(
    name: String,
    input: ReceiveChannel<T>,
    action: (T) -> Q
) {
    launch { for (msg in input) action(msg) }
}

val logger = Logger.getLogger("NNode")

/**
 * Остановка конвеера
 */
fun stop() {
    producersPool.replaceAll{ producer ->
        producer.second.cancel()
        logger.info("Stoped ${producer.first}")
        producer
    }

    producersPool.size
}

/**
 * Каналы, которых убьют. Имя канала и канал.
 */
var producersPool = CopyOnWriteArrayList<Pair<String, ReceiveChannel<*>>>()

/**
 * Конвеер
 */
fun <T> Wworkflow(context: CoroutineContext, block: suspend CoroutineScope.() -> T) = runBlocking(context, block)

/**
 * Канал с наполнением
 */
@OptIn(ExperimentalTypeInference::class, ExperimentalCoroutinesApi::class)
fun <E> CoroutineScope.pproduce(
    context: CoroutineContext = EmptyCoroutineContext,
    capacity: Int = Channel.RENDEZVOUS,
    @BuilderInference block: suspend ProducerScope<E>.() -> Unit,
): ReceiveChannel<E> = produce(context, capacity, block).apply {
    producersPool.add("produce" to this)
}

/**
 * Просто канал
 */
fun <E> cchanel() = Channel<E>(Channel.UNLIMITED).apply {
    producersPool.add("cchanel" to this)
}
