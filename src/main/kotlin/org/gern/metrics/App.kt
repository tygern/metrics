package org.gern.metrics

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Slf4jReporter
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class App

fun main() = runBlocking {
    val registry = MetricRegistry()

    val metricsReporter = Slf4jReporter
        .forRegistry(registry)
        .outputTo(LoggerFactory.getLogger(App::class.java))
        .withLoggingLevel(Slf4jReporter.LoggingLevel.INFO)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build()

    val client = OkHttpClient.Builder()
        .addInterceptor(MetricsInterceptor(registry))
        .build()

    Benchmark(
        callsToAttempt = 1_000,
        metricsReporter = metricsReporter,
        client = client,
    ).run()
}
