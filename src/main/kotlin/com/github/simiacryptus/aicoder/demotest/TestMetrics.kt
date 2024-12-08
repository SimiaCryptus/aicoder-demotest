package com.github.simiacryptus.aicoder.demotest

import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

data class TestMetric(
    val value: Number,
    val timestamp: Instant = Instant.now(),
    val metadata: Map<String, String> = emptyMap()
)

data class TestExecutionMetrics(
    var setupTime: Duration = Duration.ZERO,
    var testRunTime: Duration = Duration.ZERO,
    var teardownTime: Duration = Duration.ZERO,
    var aiResponseTime: Duration = Duration.ZERO,
    var uiInteractionTime: Duration = Duration.ZERO,
    var retryCount: Int = 0,
    var errorCount: Int = 0,
    var memoryUsageMB: Long = 0,
    var cpuUtilization: Double = 0.0,
    var networkCallCount: Int = 0,
    var fileOperationCount: Int = 0
)

class TestMetricsCollector {
    private val metrics = ConcurrentHashMap<String, MutableList<TestMetric>>()
    private val executionMetrics = ConcurrentHashMap<String, TestExecutionMetrics>()

    fun recordMetric(name: String, value: Number, metadata: Map<String, String> = emptyMap()) {
        metrics.computeIfAbsent(name) { mutableListOf() }
            .add(TestMetric(value, metadata = metadata))
    }

    fun getMetrics(name: String): List<TestMetric> = metrics[name] ?: emptyList()

    fun getExecutionMetrics(testId: String): TestExecutionMetrics =
        executionMetrics.getOrPut(testId) { TestExecutionMetrics() }

    fun clear() {
        metrics.clear()
        executionMetrics.clear()
    }
}