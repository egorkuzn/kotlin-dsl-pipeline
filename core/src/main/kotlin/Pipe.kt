package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * В текущей реализации два разных [Consumer] получают одинаковые данные,
 * которые были переданы [Producer].
 */
open class Pipe<T> {

    private val _flow = MutableSharedFlow<T>()
    val flow: SharedFlow<T> get() = _flow

    inner class Consumer {
        suspend fun onListener(action: (T) -> Unit) {
            flow.collect { value ->
                action(value)
            }
        }
    }

    inner class Producer {
        suspend fun commit(value: T) {
            _flow.emit(value)
        }

        suspend fun commitAll(vararg values: T) {
            _flow.emitAll(flowOf(*values))
        }
    }
}

/**
 * Стартовый [Pipe]
 */
object Finish : Pipe<Unit>()

/**
 * Порождает финальный [Pipe]
 */
fun <T> CoroutineScope.Init(vararg values: T): Pipe<T> = Pipe<T>().apply {
    launch { Producer().commitAll(*values) }
}
