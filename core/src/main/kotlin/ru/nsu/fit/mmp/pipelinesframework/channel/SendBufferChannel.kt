package ru.nsu.fit.mmp.pipelinesframework.channel

import kotlinx.coroutines.channels.SendChannel

interface SendBufferChannel<in E>: SendChannel<E>
