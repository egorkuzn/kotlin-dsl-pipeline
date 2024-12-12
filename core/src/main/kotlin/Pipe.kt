package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed class Pipe {

    class Single<T>(private val _flow: MutableSharedFlow<T> = MutableSharedFlow()) : Pipe() {
        val flow: SharedFlow<T> get() = _flow

        inner class Consumer(private val coroutineScope: CoroutineScope) {
            private val flow = _flow

            fun onListener(action: (T) -> Unit) {
                coroutineScope.launch {
                    flow.collect { value -> action(value) }
                }
            }

            operator fun <U> plus(other: Single<U>.Consumer): Dual<T, U>.Consumer {
                return Dual(_flow, other.flow).Consumer(coroutineScope)
            }
        }

        inner class Producer(private val coroutineScope: CoroutineScope) {
            private val flow = _flow

            fun commit(value: T) {
                coroutineScope.launch {
                    _flow.emit(value)
                }
            }

            operator fun <U> plus(other: Single<U>.Producer): Dual<T, U>.Producer {
                return Dual(_flow, other.flow).Producer()
            }
        }
    }

    class Dual<T, U>(
        private val _flowT: MutableSharedFlow<T>,
        private val _flowU: MutableSharedFlow<U>
    ) : Pipe() {
        val flowT: SharedFlow<T> get() = _flowT
        val flowU: SharedFlow<U> get() = _flowU

        inner class Consumer(private val coroutineScope: CoroutineScope) {
            fun onListener(action: (T, U) -> Unit) {
                coroutineScope.launch {
                    flowT.combine(flowU) { valueT, valueU ->
                        valueT to valueU
                    }.collect { (t, u) -> action(t, u) }
                }
            }
        }

        inner class Producer {
            suspend fun commit(value1: T, value2: U) {
                _flowT.emit(value1)
                _flowU.emit(value2)
            }
        }
    }
}

fun <T> pipe(): Pipe.Single<T> {
    return Pipe.Single();
}