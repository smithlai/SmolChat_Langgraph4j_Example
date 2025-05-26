package com.example.llama.localclient

import LLMToolAdapter
import com.smith.lai.langgraph4j_android_adapter.localclient.LocalLLMInferenceEngine
import com.smith.lai.langgraph4j_android_adapter.localclient.adaptor.Llama3_2_ToolAdapter
import dev.langchain4j.agent.tool.ToolSpecification
import dev.langchain4j.agent.tool.ToolSpecifications
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.model.chat.response.ChatResponse
import io.shubham0204.smollm.SmolLM
import kotlinx.coroutines.flow.Flow


class SmolLMInferenceEngine(
    private val smolLM: SmolLM,
    toolSpecifications: List<ToolSpecification> = emptyList(),
    mTemplate:String? = null
) : LocalLLMInferenceEngine(toolSpecifications, mTemplate) {

    val toolAdapter: LLMToolAdapter = Llama3_2_ToolAdapter()

    override fun toolPrompt(): String {
        return toolAdapter.createToolPrompt(toolSpecifications)
    }

    override fun addUserMessage(message: String) {
        smolLM.addUserMessage(message)
    }

    override fun addSystemPrompt(systemPrompt: String) {
        smolLM.addSystemPrompt(systemPrompt)
    }

    override fun addAssistantMessage(message: String) {
        smolLM.addAssistantMessage(message)
    }

    override fun generate(prompt: String): Flow<String> {
        return smolLM.getResponse(prompt)
    }

    // Note: The chat method is inherited from LocalLLMInferenceEngine and uses toolAdapter
}