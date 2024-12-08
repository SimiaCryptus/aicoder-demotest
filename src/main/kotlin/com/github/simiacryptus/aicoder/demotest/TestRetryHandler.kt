package com.github.simiacryptus.aicoder.demotest

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler
import org.slf4j.LoggerFactory

class TestRetryHandler : TestExecutionExceptionHandler {
    private val log = LoggerFactory.getLogger(TestRetryHandler::class.java)
    private val maxRetries = 3
    private val retriesMap = mutableMapOf<String, Int>()

    override fun handleTestExecutionException(context: ExtensionContext, throwable: Throwable) {
        val testId = "${context.requiredTestClass.name}#${context.requiredTestMethod.name}"
        val retryCount = retriesMap.getOrDefault(testId, 0)

        if (retryCount < maxRetries) {
            retriesMap[testId] = retryCount + 1
            log.warn("Test failed, attempting retry ${retryCount + 1} of $maxRetries for test: $testId")
            log.warn("Failure reason: ${throwable.message}")
            // Throw the exception to trigger a retry
            throw throwable
        } else {
            log.error("Test failed after $maxRetries retries: $testId")
            // If we've exceeded max retries, throw the original exception
            throw throwable
        }
    }
}