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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.Single

@Entity(tableName = "LLMModel")
data class LLMModel(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var name: String = "",
    var url: String = "",
    var path: String = "",
    var contextSize: Int = 0,
    var chatTemplate: String = "",
)

@Dao
interface LLMModelDao {
    @Query("SELECT * FROM LLMModel")
    fun getAllModels(): Flow<List<LLMModel>>

    @Query("SELECT * FROM LLMModel")
    suspend fun getAllModelsList(): List<LLMModel>

    @Query("SELECT * FROM LLMModel WHERE id = :id")
    suspend fun getModel(id: Long): LLMModel

    @Insert
    suspend fun insertModels(vararg models: LLMModel)

    @Query("DELETE FROM LLMModel WHERE id = :id")
    suspend fun deleteModel(id: Long)
}

@Database(entities = [LLMModel::class], version = 1)
abstract class LLMModelDatabase : RoomDatabase() {
    abstract fun llmModelDao(): LLMModelDao
}

@Single
class ModelsDB(
    context: Context,
) {
    private val db =
        Room
            .databaseBuilder(
                context,
                LLMModelDatabase::class.java,
                "llm-model-database",
            ).build()

    fun addModel(
        name: String,
        url: String,
        path: String,
        contextSize: Int,
        chatTemplate: String,
    ) = runBlocking(Dispatchers.IO) {
        db.llmModelDao().insertModels(
            LLMModel(
                name = name,
                url = url,
                path = path,
                contextSize = contextSize,
                chatTemplate = chatTemplate,
            ),
        )
    }

    fun getModel(id: Long): LLMModel? =
        runBlocking(Dispatchers.IO) {
            try {
                db.llmModelDao().getModel(id)
            } catch (_: IllegalArgumentException) {
                null
            }
        }

    fun getModels(): Flow<List<LLMModel>> = runBlocking(Dispatchers.IO) { db.llmModelDao().getAllModels() }

    fun getModelsList(): List<LLMModel> = runBlocking(Dispatchers.IO) { db.llmModelDao().getAllModelsList() }

    fun deleteModel(id: Long) =
        runBlocking(Dispatchers.IO) {
            db.llmModelDao().deleteModel(id)
        }
}
