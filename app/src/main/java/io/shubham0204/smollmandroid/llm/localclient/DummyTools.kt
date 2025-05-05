package io.shubham0204.smollmandroid.llm.localclient

import dev.langchain4j.agent.tool.P
import dev.langchain4j.agent.tool.Tool

class DummyTools {

    @Tool("translate a string into cat language, returns string")
    fun cat_language(@P("Original string") text: String): String {
        val catted = text.toList().joinToString(" Miao ")
        return "$catted"
    }
    @Tool("get the weather of a city")
    fun weather(@P("City name") city: String): String {
        return "The weather of $city is raining"
    }
}
