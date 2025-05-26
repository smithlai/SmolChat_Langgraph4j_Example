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
        chatScreenViewModel.setPanelColor(r,g,b)
        return "Successfully changed panel to RGB: $r|$g|$b"
    }


}
