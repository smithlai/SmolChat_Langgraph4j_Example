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

package io.shubham0204.smollmandroid.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.Single

@Entity(tableName = "ChatMessage")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var chatId: Long = 0,
    var message: String = "",
    var isUserMessage: Boolean = false,
)

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM ChatMessage WHERE chatId = :chatId")
    fun getMessages(chatId: Long): Flow<List<ChatMessage>>

    @Query("SELECT * FROM ChatMessage WHERE chatId = :chatId")
    suspend fun getMessagesForModel(chatId: Long): List<ChatMessage>

    @Insert
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM ChatMessage WHERE chatId = :chatId")
    suspend fun deleteMessages(chatId: Long)
}

@Database(entities = [ChatMessage::class], version = 1)
abstract class ChatMessagesDatabase : RoomDatabase() {
    abstract fun chatMessagesDao(): ChatMessageDao
}

@Single
class MessagesDB(
    context: Context,
) {
    private val db =
        Room
            .databaseBuilder(
                context,
                ChatMessagesDatabase::class.java,
                "chat-messages-database",
            ).build()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMessages(chatId: Long): Flow<List<ChatMessage>> = db.chatMessagesDao().getMessages(chatId)

    fun getMessagesForModel(chatId: Long): List<ChatMessage> =
        runBlocking(Dispatchers.IO) {
            db.chatMessagesDao().getMessagesForModel(chatId)
        }

    fun addUserMessage(
        chatId: Long,
        message: String,
    ) = runBlocking(Dispatchers.IO) {
        db.chatMessagesDao().insertMessage(ChatMessage(chatId = chatId, message = message, isUserMessage = true))
    }

    fun addAssistantMessage(
        chatId: Long,
        message: String,
    ) = runBlocking(Dispatchers.IO) {
        db.chatMessagesDao().insertMessage(ChatMessage(chatId = chatId, message = message, isUserMessage = false))
    }

    fun deleteMessages(chatId: Long) =
        runBlocking(Dispatchers.IO) {
            db.chatMessagesDao().deleteMessages(chatId)
        }
}
