package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "github_config")
data class GithubConfig(
    @PrimaryKey val id: Int = 1,
    val pat: String = "",
    val repoUrl: String = "",
    val owner: String = "",
    val repoName: String = "",
    val selectedBranch: String = "main",
    val selectedFolderUri: String? = null,
    val selectedFolderPath: String? = null,
    val coachMarksShown: Boolean = false
)

@Entity(tableName = "commit_logs")
data class CommitLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val commitMsg: String,
    val branchName: String,
    val commitSha: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface GithubConfigDao {
    @Query("SELECT * FROM github_config WHERE id = 1 LIMIT 1")
    fun getConfigFlow(): Flow<GithubConfig?>

    @Query("SELECT * FROM github_config WHERE id = 1 LIMIT 1")
    suspend fun getConfigDirect(): GithubConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(config: GithubConfig)

    @Query("UPDATE github_config SET selectedFolderUri = :uri, selectedFolderPath = :path WHERE id = 1")
    suspend fun updateFolder(uri: String?, path: String?)

    @Query("UPDATE github_config SET selectedBranch = :branch WHERE id = 1")
    suspend fun updateBranch(branch: String)

    @Query("UPDATE github_config SET coachMarksShown = :shown WHERE id = 1")
    suspend fun updateCoachMarks(shown: Boolean)
}

@Dao
interface CommitLogDao {
    @Query("SELECT * FROM commit_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<CommitLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CommitLog)
}

@Database(entities = [GithubConfig::class, CommitLog::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun configDao(): GithubConfigDao
    abstract fun commitLogDao(): CommitLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "github_sync_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
