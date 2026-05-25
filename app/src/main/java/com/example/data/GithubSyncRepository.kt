package com.example.data

import kotlinx.coroutines.flow.Flow

class GithubSyncRepository(private val db: AppDatabase) {
    val configFlow: Flow<GithubConfig?> = db.configDao().getConfigFlow()
    val commitLogsFlow: Flow<List<CommitLog>> = db.commitLogDao().getAllLogsFlow()

    suspend fun getConfigDirect(): GithubConfig? {
        return db.configDao().getConfigDirect()
    }

    suspend fun saveConfig(config: GithubConfig) {
        db.configDao().insertOrUpdateConfig(config)
    }

    suspend fun updateFolder(uri: String?, path: String?) {
        val current = getConfigDirect() ?: GithubConfig()
        db.configDao().insertOrUpdateConfig(current.copy(selectedFolderUri = uri, selectedFolderPath = path))
    }

    suspend fun updateBranch(branch: String) {
        val current = getConfigDirect() ?: GithubConfig()
        db.configDao().insertOrUpdateConfig(current.copy(selectedBranch = branch))
    }

    suspend fun updateCoachMarks(shown: Boolean) {
        val current = getConfigDirect() ?: GithubConfig()
        db.configDao().insertOrUpdateConfig(current.copy(coachMarksShown = shown))
    }

    suspend fun addCommitLog(log: CommitLog) {
        db.commitLogDao().insertLog(log)
    }
}
