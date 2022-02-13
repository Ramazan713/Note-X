package com.masterplus.notex.models.coroutinescopes

import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

class IOCoroutineScope :CoroutineScope, Observable() {
    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.IO + handler

    private val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        setChanged()
        notifyObservers(throwable)
    }

}