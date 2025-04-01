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
import androidx.room.Update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.Single

@Entity(tableName = "Task")
data class Task(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var name: String = "",
    var systemPrompt: String = "",
    var modelId: Long = -1,
    var shortcutId: String? = null,
    @Transient var modelName: String = "",
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM Task WHERE id = :taskId")
    suspend fun getTask(taskId: Long): Task

    @Query("SELECT * FROM Task")
    fun getTasks(): Flow<List<Task>>

    @Insert
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM Task WHERE id = :taskId")
    suspend fun deleteTask(taskId: Long)
}

@Database(entities = [Task::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}

@Single
class TasksDB(
    context: Context,
) {
    private val db =
        Room
            .databaseBuilder(
                context,
                TaskDatabase::class.java,
                "task-database",
            ).build()

    fun getTask(taskId: Long): Task? =
        runBlocking(Dispatchers.IO) {
            db.taskDao().getTask(taskId)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTasks(): Flow<List<Task>> = db.taskDao().getTasks()

    fun addTask(
        name: String,
        systemPrompt: String,
        modelId: Long,
    ) = runBlocking(Dispatchers.IO) {
        db.taskDao().insertTask(Task(name = name, systemPrompt = systemPrompt, modelId = modelId))
    }

    fun deleteTask(taskId: Long) =
        runBlocking(Dispatchers.IO) {
            db.taskDao().deleteTask(taskId)
        }

    fun updateTask(task: Task) =
        runBlocking(Dispatchers.IO) {
            db.taskDao().updateTask(task)
        }
}
