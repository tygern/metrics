package org.gern.metrics

import com.codahale.metrics.MetricRegistry
import okhttp3.Interceptor
import okhttp3.Response

class MetricsInterceptor(
    private val registry: MetricRegistry,
) : Interceptor {
    private val attempted = registry.counter("attempted-requests")
    private val completed = registry.counter("completed-requests")
    private val success = registry.counter("successful-requests")
    private val failed = registry.counter("failed-requests")

    override fun intercept(chain: Interceptor.Chain): Response {
        attempted.inc()

        return chain.proceed(chain.request()).also { response ->
            completed.inc()

            if (response.isSuccessful) {
                success.inc()
            } else {
                failed.inc()
            }

            val path = response.request.url.encodedPath
            registry.histogram(path).update(response.receivedResponseAtMillis - response.sentRequestAtMillis)
        }
    }
}
