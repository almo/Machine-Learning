package com.catharsis.ai4media.ai4mediaserver

import com.google.cloud.vertexai.VertexAI
import com.google.cloud.vertexai.api.GenerateContentResponse
import com.google.cloud.vertexai.api.GenerateContentResponse.UsageMetadata
import com.google.cloud.vertexai.api.GenerationConfig
import com.google.cloud.vertexai.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

// 1. Configuration Data Classes (Clean separation of environments)
data class VertexAiConfig(
    val projectId: String = AppConfig.projectId,
    val location: String = AppConfig.cloudLocationId,
    val modelName: String = "gemini-2.5-flash-lite"
)

data class PricingConfig(
    val inputCostPerMillionTokens: Double = 0.10,
    val outputCostPerMillionTokens: Double = 0.40
)

data class TokenMetrics(
    val promptTokenCount: Int,
    val candidatesTokenCount: Int,
    val totalTokenCount: Int,
    val approximateCost: Double
)

// 2. Extension Functions for Readability & Reusability
/** Safely extracts text content from the deeply nested response hierarchy. */
private fun GenerateContentResponse.extractText(): String? =
    candidatesList.firstOrNull()?.content?.partsList?.firstOrNull()?.text

/** Calculates approximate cost based on token usage. */
private fun UsageMetadata.calculateCost(pricing: PricingConfig): Double {
    val inputCost = (promptTokenCount / 1_000_000.0) * pricing.inputCostPerMillionTokens
    val outputCost = (candidatesTokenCount / 1_000_000.0) * pricing.outputCostPerMillionTokens
    return inputCost + outputCost
}

// 3. Encapsulate core logic in a testable Client class
class GeminiClient(
    private val config: VertexAiConfig = VertexAiConfig(),
    private val pricing: PricingConfig = PricingConfig()
) : AutoCloseable {

    private val vertexAI = VertexAI(config.projectId, config.location)

    // Use `apply` to clean up the Java Builder pattern
    private val generationConfig = GenerationConfig.newBuilder().apply {
        setTemperature(1.2f)
        setMaxOutputTokens(2048)
        setTopK(40f)
        setTopP(0.95f)
    }.build()

    private val generativeModel = GenerativeModel(config.modelName, vertexAI)
        .withGenerationConfig(generationConfig)

    /**
     * Generates content. Executes on the IO dispatcher to ensure 
     * the underlying network call doesn't block the calling thread.
     */
    suspend fun generateContent(prompt: String): GenerateContentResponse =
        withContext(Dispatchers.IO) {
            generativeModel.generateContent(prompt)
        }

    fun printMetrics(response: GenerateContentResponse): TokenMetrics? {
        val usage = response.usageMetadata ?: return null
        val cost = usage.calculateCost(pricing)

        return TokenMetrics(
            promptTokenCount = usage.promptTokenCount,
            candidatesTokenCount = usage.candidatesTokenCount,
            totalTokenCount = usage.totalTokenCount,
            approximateCost = cost
        )
    }

    override fun close() {
        vertexAI.close()
    }
}