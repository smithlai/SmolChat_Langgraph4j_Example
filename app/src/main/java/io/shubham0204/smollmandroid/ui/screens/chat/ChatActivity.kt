/*
 * Copyright (C) 2024 Shubham Panchal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.shubham0204.smollmandroid.ui.screens.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Spanned
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smith.smith_rag.ui.screens.docs.DocsScreen
import io.shubham0204.smollmandroid.R
import io.shubham0204.smollmandroid.data.Chat
import io.shubham0204.smollmandroid.data.Task
import io.shubham0204.smollmandroid.ui.components.AppBarTitleText
import io.shubham0204.smollmandroid.ui.components.MediumLabelText
import io.shubham0204.smollmandroid.ui.screens.chat.ChatScreenViewModel.ModelLoadingState
import io.shubham0204.smollmandroid.ui.screens.manage_tasks.ManageTasksActivity
import io.shubham0204.smollmandroid.ui.screens.manage_tasks.TasksList
import io.shubham0204.smollmandroid.ui.theme.SmolLMAndroidTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

private const val LOGTAG = "[ChatActivity-Kt]"
private val LOGD: (String) -> Unit = { Log.d(LOGTAG, it) }

class ChatActivity : ComponentActivity() {
    private val viewModel: ChatScreenViewModel by inject()
    private var modelUnloaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * Check if the activity was launched by an intent to share text with the app
         * If yes, then, extract the text and set its value to [viewModel.questionTextDefaultVal]
         */
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let { text ->
                val chatCount = viewModel.chatsDB.getChatsCount()
                val newChat = viewModel.chatsDB.addChat(chatName = "Untitled ${chatCount + 1}")
                viewModel.switchChat(newChat)
                viewModel.questionTextDefaultVal = text
            }
        }

        /**
         * Check if the activity was launched by an intent created by a dynamic shortcut
         * If yes, get the corresponding task (task ID is stored within the intent)
         * and create a new chat instance with the task's parameters
         */
        if (intent?.action == Intent.ACTION_VIEW && intent.getLongExtra("task_id", 0L) != 0L) {
            val taskId = intent.getLongExtra("task_id", 0L)
            viewModel.tasksDB.getTask(taskId)?.let { task ->
                createChatFromTask(viewModel, task)
            }
        }

        setContent {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = "chat",
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() },
            ) {
                composable("edit-chat") {
                    EditChatSettingsScreen(
                        viewModel,
                        onBackClicked = { navController.navigateUp() },
                    )
                }
                composable("chat") {
                    ChatActivityScreenUI(
                        viewModel,
                        onEditChatParamsClick = { navController.navigate("edit-chat") },
                        onRAGChatClick = { navController.navigate("ragdoc") }
                    )
                }

                composable("ragdoc") { DocsScreen(onBackClick = { navController.navigateUp() }) }
            }
        }
    }

    /**
     * Load the model when the activity is visible to the user and
     * unload the model when the activity is not visible to the user.
     * see https://developer.android.com/guide/components/activities/activity-lifecycle
     */
    override fun onStart() {
        super.onStart()
        if (modelUnloaded) {
            viewModel.loadModel()
            LOGD("onStart() called - model loaded")
        }
    }

    override fun onStop() {
        super.onStop()
        modelUnloaded = viewModel.unloadModel()
        LOGD("onStop() called - model unloaded result: $modelUnloaded")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatActivityScreenUI(
    viewModel: ChatScreenViewModel,
    onEditChatParamsClick: () -> Unit,
    onRAGChatClick: () -> Unit
) {
    val context = LocalContext.current
    val currChat by viewModel.currChatState.collectAsStateWithLifecycle(lifecycleOwner = LocalLifecycleOwner.current)
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    LaunchedEffect(currChat) { viewModel.loadModel() }
    SmolLMAndroidTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerUI(
                    viewModel,
                    onItemClick = { chat ->
                        viewModel.switchChat(chat)
                        scope.launch { drawerState.close() }
                    },
                    onManageTasksClick = {
                        scope.launch { drawerState.close() }
                        Intent(context, ManageTasksActivity::class.java).also {
                            context.startActivity(it)
                        }
                    },
                    onCreateTaskClick = {
                        scope.launch { drawerState.close() }
                        viewModel.showTaskListBottomList()
                    },
                )
                BackHandler(drawerState.isOpen) {
                    scope.launch { drawerState.close() }
                }
            },
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                AppBarTitleText(
                                    currChat?.name ?: stringResource(R.string.chat_select_chat),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Text(
                                    if (currChat != null && currChat?.llmModelId != -1L) {
                                        viewModel.modelsRepository
                                            .getModelFromId(currChat!!.llmModelId)
                                            ?.name ?: ""
                                    } else {
                                        ""
                                    },
                                    fontSize = 12.sp,
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = stringResource(R.string.chat_view_chats),
                                )
                            }
                        },
                        actions = {
                            if (currChat != null) {
                                Box {
                                    IconButton(
                                        onClick = { viewModel.showMoreOptionsPopup() },
                                    ) {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = "Options",
                                        )
                                    }
                                    ChatMoreOptionsPopup(viewModel, onEditChatParamsClick, onRAGChatClick)
                                }
                            }
                        },
                    )
                },
            ) { innerPadding ->
                Column(
                    modifier =
                        Modifier
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.background),
                ) {
                    if (currChat != null) {
                        ScreenUI(viewModel, currChat!!)
                    }
                }
            }
            SelectModelsList(viewModel)
            TasksListBottomSheet(viewModel)
        }
    }
}

@Composable
private fun ColumnScope.ScreenUI(
    viewModel: ChatScreenViewModel,
    currChat: Chat,
) {
    val isGeneratingResponse by viewModel.isGeneratingResponse.collectAsStateWithLifecycle()
    MessagesList(
        viewModel,
        isGeneratingResponse,
        currChat.id,
    )
    MessageInput(
        viewModel,
        isGeneratingResponse,
    )
}

@Composable
private fun ColumnScope.MessagesList(
    viewModel: ChatScreenViewModel,
    isGeneratingResponse: Boolean,
    chatId: Long,
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val messages by viewModel.getChatMessages(chatId).collectAsState(emptyList())
    val lastUserMessageIndex by remember { derivedStateOf { messages.indexOfLast { it.isUserMessage } } }
    val partialResponse by viewModel.partialResponse.collectAsStateWithLifecycle()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
        }
    }
    LazyColumn(
        state = listState,
        modifier =
            Modifier
                .fillMaxSize()
                .weight(1f),
    ) {
        itemsIndexed(messages) { i, chatMessage ->
            MessageListItem(
                viewModel.markwon.render(viewModel.markwon.parse(chatMessage.message)),
                responseGenerationSpeed = if (i == messages.size - 1) viewModel.responseGenerationsSpeed else null,
                responseGenerationTimeSecs = if (i == messages.size - 1) viewModel.responseGenerationTimeSecs else null,
                chatMessage.isUserMessage,
                onCopyClicked = {
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Copied message", chatMessage.message)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, context.getString(R.string.chat_message_copied), Toast.LENGTH_SHORT).show()
                },
                onShareClicked = {
                    context.startActivity(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, chatMessage.message)
                        },
                    )
                },
                onMessageEdited = { newMessage ->
                    // viewModel.sendUserQuery will add a new message to the chat
                    // hence we delete the old message and the corresponding LLM
                    // response if there exists one
                    // TODO: There should be no need to unload/load the model again
                    //       as only the conversation messages have changed.
                    //       Currently there's no native function to edit the conversation messages
                    //       so unload (remove all messages) and load (add all messages) the model.
                    viewModel.deleteMessage(chatMessage.id)
                    if (!messages.last().isUserMessage) {
                        viewModel.deleteMessage(messages.last().id)
                    }
                    viewModel.messagesDB.addUserMessage(chatId, newMessage)
                    viewModel.unloadModel()
                    viewModel.loadModel(onComplete = {
                        if (it == ModelLoadingState.SUCCESS) {
                            viewModel.sendUserQuery(newMessage, addMessageToDB = false)
                        }
                    })
                },
                // allow editing the message only if it is the last message in the list
                allowEditing = (i == lastUserMessageIndex),
            )
        }
        if (isGeneratingResponse) {
            item {
                if (partialResponse.isNotEmpty()) {
                    MessageListItem(
                        viewModel.markwon.render(viewModel.markwon.parse(partialResponse)),
                        responseGenerationSpeed = null,
                        responseGenerationTimeSecs = null,
                        false,
                        {},
                        {},
                        onMessageEdited = {
                            // Not applicable as allowEditing is set to False
                        },
                        allowEditing = false,
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .animateItem(),
                    ) {
                        Icon(
                            modifier = Modifier.padding(8.dp),
                            imageVector = Icons.Default.Android,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = stringResource(R.string.chat_thinking),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.MessageListItem(
    messageStr: Spanned,
    responseGenerationSpeed: Float?,
    responseGenerationTimeSecs: Int?,
    isUserMessage: Boolean,
    onCopyClicked: () -> Unit,
    onShareClicked: () -> Unit,
    onMessageEdited: (String) -> Unit,
    modifier: Modifier = Modifier,
    allowEditing: Boolean,
) {
    var isEditing by remember { mutableStateOf(false) }
    if (!isUserMessage) {
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .animateItem(),
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Icon(
                    modifier = Modifier.padding(4.dp),
                    imageVector = Icons.Default.Android,
                    contentDescription = null,
                )
            }
            Column {
                ChatMessageText(
                    // to make pointerInput work in MarkdownText use disableLinkMovementMethod
                    // https://github.com/jeziellago/compose-markdown/issues/85#issuecomment-2184040304
                    modifier =
                        Modifier
                            .padding(4.dp)
                            .background(MaterialTheme.colorScheme.background)
                            .padding(4.dp)
                            .fillMaxSize(),
                    textColor = MaterialTheme.colorScheme.onBackground.toArgb(),
                    textSize = 16f,
                    message = messageStr,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.chat_message_copy),
                        modifier = Modifier.clickable { onCopyClicked() },
                        fontSize = 6.sp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.chat_message_share),
                        modifier = Modifier.clickable { onShareClicked() },
                        fontSize = 6.sp,
                    )
                    responseGenerationSpeed?.let {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier =
                                Modifier
                                    .size(2.dp)
                                    .clip(CircleShape)
                                    .background(Color.DarkGray),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "%.2f tokens/s".format(it),
                            fontSize = 8.sp,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier =
                                Modifier
                                    .size(2.dp)
                                    .clip(CircleShape)
                                    .background(Color.DarkGray),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$responseGenerationTimeSecs s",
                            fontSize = 8.sp,
                        )
                    }
                }
            }
        }
    } else {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .animateItem(),
            horizontalArrangement = Arrangement.End,
        ) {
            Column(horizontalAlignment = Alignment.End) {
                var message by remember { mutableStateOf(messageStr.toString()) }
                if (isEditing) {
                    TextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
                                .padding(8.dp)
                                .widthIn(max = 250.dp),
                        colors =
                            TextFieldDefaults.colors(
                                errorContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                            ),
                    )
                } else {
                    ChatMessageText(
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                                .padding(8.dp)
                                .widthIn(max = 250.dp),
                        textColor = android.graphics.Color.WHITE,
                        textSize = 16f,
                        message = messageStr,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.chat_message_copy),
                        modifier = Modifier.clickable { onCopyClicked() },
                        fontSize = 6.sp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.chat_message_share),
                        modifier = Modifier.clickable { onShareClicked() },
                        fontSize = 6.sp,
                    )
                    if (allowEditing) {
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isEditing) {
                            Text(
                                text = "Done",
                                modifier =
                                    Modifier.clickable {
                                        isEditing = false
                                        onMessageEdited(message)
                                    },
                                fontSize = 6.sp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cancel",
                                modifier =
                                    Modifier.clickable {
                                        isEditing = false
                                        message = messageStr.toString()
                                    },
                                fontSize = 6.sp,
                            )
                        } else {
                            Text(
                                text = "Edit",
                                modifier = Modifier.clickable { isEditing = true },
                                fontSize = 6.sp,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
        }
    }
}

@Composable
private fun MessageInput(
    viewModel: ChatScreenViewModel,
    isGeneratingResponse: Boolean,
) {
    val currChat by viewModel.currChatState.collectAsStateWithLifecycle()
    val modelLoadingState by viewModel.modelLoadState.collectAsStateWithLifecycle()
    if ((currChat?.llmModelId ?: -1L) == -1L) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = stringResource(R.string.chat_select_model),
        )
    } else {
        var questionText by remember { mutableStateOf(viewModel.questionTextDefaultVal ?: "") }
        val keyboardController = LocalSoftwareKeyboardController.current
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
            when (modelLoadingState) {
                ChatScreenViewModel.ModelLoadingState.IN_PROGRESS -> {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = stringResource(R.string.chat_loading_model),
                    )
                }

                ChatScreenViewModel.ModelLoadingState.FAILURE -> {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = stringResource(R.string.chat_model_cannot_be_loaded),
                    )
                }

                ChatScreenViewModel.ModelLoadingState.SUCCESS -> {
                    TextField(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        value = questionText,
                        onValueChange = { questionText = it },
                        shape = RoundedCornerShape(16.dp),
                        colors =
                            TextFieldDefaults.colors(
                                disabledTextColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                            ),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.chat_ask_question),
                            )
                        },
                        keyboardOptions =
                            KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (isGeneratingResponse) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                            IconButton(onClick = { viewModel.stopGeneration() }) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop")
                            }
                        }
                    } else {
                        IconButton(
                            enabled = questionText.isNotEmpty(),
                            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            onClick = {
                                keyboardController?.hide()
                                viewModel.sendUserQuery(questionText)
                                questionText = ""
                            },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Send text",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasksListBottomSheet(viewModel: ChatScreenViewModel) {
    val context = LocalContext.current
    val showTaskListBottomList by viewModel.showTaskListBottomListState.collectAsStateWithLifecycle()
    if (showTaskListBottomList) {
        // adding bottom sheets in Compose
        // See https://developer.android.com/develop/ui/compose/components/bottom-sheets
        ModalBottomSheet(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            onDismissRequest = { viewModel.hideTaskListBottomList() },
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                val tasks by viewModel.tasksDB.getTasks().collectAsState(emptyList())
                if (tasks.isEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        stringResource(R.string.chat_no_task_created),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.showTaskListBottomList()
                            Intent(context, ManageTasksActivity::class.java).also {
                                context.startActivity(it)
                            }
                        },
                    ) {
                        MediumLabelText(stringResource(R.string.chat_create_task))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                } else {
                    AppBarTitleText(stringResource(R.string.chat_select_task))
                    TasksList(
                        tasks.map {
                            val modelName =
                                viewModel.modelsRepository.getModelFromId(it.modelId)?.name
                                    ?: return@map it
                            it.copy(modelName = modelName)
                        },
                        onTaskSelected = { task ->
                            createChatFromTask(viewModel, task)
                        },
                        onUpdateTaskClick = { // Not applicable as showTaskOptions is set to `false`
                        },
                        onEditTaskClick = { // Not applicable as showTaskOptions is set to `false`
                        },
                        onDeleteTaskClick = { // Not applicable as showTaskOptions is set to `false`
                        },
                        enableTaskClick = true,
                        showTaskOptions = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectModelsList(viewModel: ChatScreenViewModel) {
    val showSelectModelsListDialog by viewModel.showSelectModelListDialogState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    if (showSelectModelsListDialog) {
        val modelsList by
            viewModel.modelsRepository.getAvailableModels().collectAsState(emptyList())
        SelectModelsList(
            onDismissRequest = { viewModel.hideSelectModelListDialog() },
            modelsList,
            onModelListItemClick = { model ->
                viewModel.updateChatLLMParams(model.id, model.chatTemplate)
                viewModel.loadModel()
                viewModel.hideSelectModelListDialog()
            },
            onModelDeleteClick = { model ->
                viewModel.deleteModel(model.id)
                Toast
                    .makeText(
                        viewModel.context,
                        context.getString(R.string.chat_model_deleted, model.name),
                        Toast.LENGTH_LONG,
                    ).show()
            },
        )
    }
}

private fun createChatFromTask(
    viewModel: ChatScreenViewModel,
    task: Task,
) {
    // Using parameters from the `task`
    // create a `Chat` instance and switch to it
    viewModel.modelsRepository.getModelFromId(task.modelId)?.let { model ->
        val newTask =
            viewModel.chatsDB.addChat(
                chatName = task.name,
                chatTemplate = model.chatTemplate,
                systemPrompt = task.systemPrompt,
                llmModelId = task.modelId,
                isTask = true,
            )
        viewModel.switchChat(newTask)
        viewModel.hideTaskListBottomList()
    }
}
