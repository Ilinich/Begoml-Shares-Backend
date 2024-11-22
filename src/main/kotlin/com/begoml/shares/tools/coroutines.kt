package com.begoml.shares.tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

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