package ru.nsu.fit.mmp.pipelinesframework.pipe

import kotlinx.coroutines.CoroutineScope

sealed class Pipe {

    class Single<T>() : Pipe() {


        inner class Consumer(private val coroutineScope: CoroutineScope) {


            fun onListener(action: (T) -> Unit) {

            }

            operator fun <U> plus(other: Single<U>.Consumer): Dual<T, U>.Consumer {
                return Dual<T,U>().Consumer(coroutineScope)
            }
        }

        inner class Producer(private val coroutineScope: CoroutineScope) {


            fun commit(value: T) {

            }

            operator fun <U> plus(other: Single<U>.Producer): Dual<T, U>.Producer {
                return Dual<T,U>().Producer()
            }
        }
    }

    class Dual<T, U>(

    ) : Pipe() {


        inner class Consumer(private val coroutineScope: CoroutineScope) {
            fun onListener(action: (T, U) -> Unit) {

            }
        }

        inner class Producer {
            suspend fun commit(value1: T, value2: U) {

            }
        }
    }
}