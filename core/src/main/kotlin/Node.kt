package ru.nsu.fit.mmp.pipelinesframework

class Node(
    name: String,
    input: List<Pipe<*>>,
    output: List<Pipe<*>>,
    val actions: suspend ()->Unit
)