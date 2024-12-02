package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class Pipe<T> {

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
    }
}