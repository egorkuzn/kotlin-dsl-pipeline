package ru.nsu.fit.mmp.pipelinesframework

/**
 * @param name Название ноды
 * @param input Входной pipe
 * @param output Выходной pipe
 * @see Pipe
 */
class Node(
    name: String,
    input: List<Pipe<*>>,
    output: List<Pipe<*>>,
    val actions: suspend ()->Unit
)