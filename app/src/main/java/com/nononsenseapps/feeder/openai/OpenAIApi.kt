package com.nononsenseapps.feeder.openai

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.TextContent
import com.aallam.openai.api.chat.Tool
import com.aallam.openai.api.core.Parameters
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.logging.Logger
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.archmodel.Repository

class OpenAIApi(
    private val repository: Repository
) {

    data class SummaryResult(
        val id: String,
        val created: Long,
        val model: String,
        val content: String,
        val promptTokens: Int,
        val completeTokens: Int,
        val totalTokens: Int
    ) {
        companion object {
            val empty = SummaryResult(id = "", created = 0, model = "", content = "", promptTokens = 0, completeTokens = 0, totalTokens = 0)
        }
    }

    private var openAIConfig = OpenAIConfig(
        token = repository.openAIKey.value,
        logging = LoggingConfig(logLevel = LogLevel.All)
    )
    private var _openAI = OpenAI(config = openAIConfig)
    private val openAI: OpenAI
        get() = if (repository.openAIKey.value != openAIConfig.token) {
            openAIConfig = OpenAIConfig(
                token = repository.openAIKey.value,
                logging = openAIConfig.logging
            )
            OpenAI(config = openAIConfig).also { _openAI = it }
        } else _openAI

    suspend fun listModelIds(): List<String> = openAI.models().map { it.id.id }

    suspend fun summarize(content: String): SummaryResult {
        val response = openAI.chatCompletion(
            request = ChatCompletionRequest(
                model = ModelId(id = repository.openAIModelId.value),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        messageContent = TextContent("You are assistant inside RSS reader app.")
                    ),
                    ChatMessage(
                        role = ChatRole.User,
                        messageContent = TextContent("Summarize: $content")
                    )
                ),
//                tools = listOf(
//                    Tool.function(
//                        "getArticleContent",
//                        "Get content of an article for an url",
//                        parameters = Parameters.buildJsonObject {
//
//                        }
//                    )
//                )
            ),
            requestOptions = null
        )
        return SummaryResult(
            id = response.id,
            model = response.model.id,
            content = response.choices.firstOrNull()?.message?.content ?: "",
            created = response.created,
            promptTokens = response.usage?.promptTokens ?: 0,
            completeTokens = response.usage?.completionTokens ?: 0,
            totalTokens = response.usage?.completionTokens ?: 0
        )
    }
}
