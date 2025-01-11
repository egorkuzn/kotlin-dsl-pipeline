package ru.nsu.fit.mmp.pipelinesframework.channel

import kotlinx.coroutines.channels.SendChannel

interface SendPipe<in E>: SendChannel<E>
