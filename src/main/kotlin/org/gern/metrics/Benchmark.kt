package org.gern.metrics

import com.codahale.metrics.ScheduledReporter
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class Benchmark(
    private val callsToAttempt: Int,
    private val metricsReporter: ScheduledReporter,
    private val client: OkHttpClient,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun run() = coroutineScope {
        printHeading("Starting Benchmark")

        metricsReporter.use { reporter ->
            reporter.start(5, TimeUnit.SECONDS)
            launchRequests()
            client.shutdownWhenDone()
        }

        printHeading("Benchmark Complete")
    }

    private suspend fun launchRequests() = coroutineScope {
        repeat(callsToAttempt) {
            launch {
                client.awaitGetRequest("http://www.fillmurray.com/30/${(1..10).random() * 10}").use { response ->
                    logger.debug("Request to {} returned with {}", response.request.url, response.code)
                }
            }
        }
    }

    private fun printHeading(string: String) =
        logger.info(
            """


            ${"=".repeat(string.length + 20)}
            ${" ".repeat(10)}$string${" ".repeat(10)}
            ${"=".repeat(string.length + 20)}

        """.trimIndent()
        )
}
