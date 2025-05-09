package io.shubham0204.smollmandroid.llm.localclient

import android.util.Log
import com.smith.smith_rag.api.RagApi
import dev.langchain4j.agent.tool.P
import dev.langchain4j.agent.tool.Tool
import io.shubham0204.smollmandroid.ui.screens.chat.ChatScreenViewModel
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext

class DummyTools (val chatScreenViewModel: ChatScreenViewModel){

    @Tool("Change the panel color with RGBA")
    fun panel_color(@P("Red(0~1)") r: Float,@P("Green(0~1)") g: Float,@P("Blue(0~1)") b: Float): String {
//        val viewmodel: ChatScreenViewModel = GlobalContext.get().get()
//        Log.i("ViewModelCheck", "ViewModel instance: $viewmodel")
        chatScreenViewModel.setPanelColor(r,g,b)
        Log.i("aaaax", "$r|$g|$b")
        return "Successfully changed panel to RGB: $r|$g|$b"
    }

//    @Tool("translate a string into cat language, returns string")
//    fun cat_language(@P("Original string") text: String): String {
//        val catted = text.toList().joinToString(" Miao ")
//        return "$catted"
//    }
//    @Tool("get the weather of a city")
//    fun weather(@P("City name") city: String): String {
//        return "The weather of $city is raining"
//    }
//    @Tool("search anything about the novel Alice in the Wonderland using RAG.")
//    fun alice_serach(@P("keyword to query") keyword: String): String {
//        var data = ""
//        runBlocking {
//            val ragapi: RagApi = GlobalContext.get().get()
//            data = ragapi.search(keyword)
//        }
//        return data;
//    }

}
