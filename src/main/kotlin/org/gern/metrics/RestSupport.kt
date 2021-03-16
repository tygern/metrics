package org.gern.metrics

import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import java.io.IOException
import kotlin.coroutines.resumeWithException
import kotlin.time.milliseconds


suspend fun OkHttpClient.awaitGetRequest(url: String): Response {
    val request = Request.Builder()
        .url(url)
        .build()

    return newCall(request).await()
}

suspend fun Call.await(): Response =
    suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response) {
                    try {
                        cancel()
                    } catch (_: Throwable) {
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                if (!continuation.isCancelled) {
                    continuation.resumeWithException(e)
                }
            }
        })

        continuation.invokeOnCancellation {
            try {
                cancel()
            } catch (_: Throwable) {
            }
        }
    }

suspend fun OkHttpClient.shutdownWhenDone() {
    do {
        delay(100.milliseconds)
    } while (dispatcher.queuedCallsCount() + dispatcher.runningCallsCount() > 0)

    dispatcher.executorService.shutdown()
}
