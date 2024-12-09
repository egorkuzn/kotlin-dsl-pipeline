package ru.nsu.fit.mmp.pipelinesframework

import kotlinx.coroutines.CoroutineScope

class Node(
    name: String,
    input: List<Pipe.Single<*>>,
    output: List<Pipe.Single<*>>,
    val actions: suspend (coroutineScope: CoroutineScope)->Unit
)