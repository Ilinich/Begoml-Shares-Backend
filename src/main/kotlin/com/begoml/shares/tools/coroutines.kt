package com.begoml.shares.tools

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

suspend fun <A, B> Iterable<A>.parallelMap(
    context: CoroutineContext = Dispatchers.IO,
    action: suspend (A) -> B,
): List<B> = coroutineScope {
    map {
        async(context) {
            action(it)
        }
    }.awaitAll()
}

fun CoroutineScope.safeLaunch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    handleException: suspend (throwable: Throwable) -> Unit = {},
    block: suspend CoroutineScope.() -> Unit,
): Job {
    return this.launch(
        context = context + CoroutineExceptionHandler { _, throwable ->
            this.launch { handleException.invoke(throwable) }
        },
        start = start,
        block = block,
    )
}