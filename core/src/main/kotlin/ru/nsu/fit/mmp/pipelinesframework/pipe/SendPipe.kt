package ru.nsu.fit.mmp.pipelinesframework.pipe

import kotlinx.coroutines.channels.SendChannel

interface SendPipe<in E>: SendChannel<E>
