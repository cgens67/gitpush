package com.example.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CommitLog
import com.example.data.GithubConfig
import com.example.data.GithubSyncRepository
import com.example.network.BlobRequest
import com.example.network.CommitRequest
import com.example.network.GithubClient
import com.example.network.TreeEntry
import com.example.network.TreeRequest
import com.example.network.UpdateRefRequest
import com.example.network.CreateRefRequest
import com.example.network.CreateOrUpdateFileRequest
import com.example.utils.FolderScanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object Setup : Screen()
    object Main : Screen()
    object Progress : Screen()
}

sealed class SyncState {
    object Idle : SyncState()
    data class ScanningLocalFiles(val fileCount: Int) : SyncState()
    object FetchingRef : SyncState()
    object FetchingCommitDetail : SyncState()
    data class UploadingBlobs(val total: Int, val uploaded: Int, val currentFile: String) : SyncState()
    object CreatingTree : SyncState()
    object CreatingCommit : SyncState()
    object UpdatingRef : SyncState()
    data class Success(val commitSha: String, val summary: String) : SyncState()
    data class Error(val errorMsg: String) : SyncState()
}

class GitSyncViewModel(private val repository: GithubSyncRepository) : ViewModel() {

    val currentConfig: StateFlow<GithubConfig?> = repository.configFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val commitLogs: StateFlow<List<CommitLog>> = repository.commitLogsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Setup)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _branches = MutableStateFlow<List<String>>(listOf("main", "master", "develop", "staging"))
    val branches: StateFlow<List<String>> = _branches.asStateFlow()

    private val _isFetchingBranches = MutableStateFlow(false)
    val isFetchingBranches: StateFlow<Boolean> = _isFetchingBranches.asStateFlow()

    init {
        viewModelScope.launch {
            // Check if credentials are set, and navigate directly to dashboard if they are!
            currentConfig.collect { config ->
                if (config != null && config.pat.isNotBlank() && config.repoUrl.isNotBlank()) {
                    if (_currentScreen.value == Screen.Setup) {
                        _currentScreen.value = Screen.Main
                    }
                }
            }
        }
    }

    fun parseAndSaveConfig(pat: String, repoUrl: String) {
        viewModelScope.launch {
            val trimmedUrl = repoUrl.trim().removeSuffix("/").removeSuffix(".git")
            val parts = trimmedUrl.split("/")
            var owner = ""
            var repoName = ""

            // Handle standard github urls or direct formats
            if (parts.size >= 2) {
                repoName = parts.last()
                owner = parts[parts.size - 2]
            } else {
                owner = "unknown-owner"
                repoName = trimmedUrl
            }

            val current = currentConfig.value ?: GithubConfig()
            val updated = current.copy(
                pat = pat.trim(),
                repoUrl = repoUrl.trim(),
                owner = owner,
                repoName = repoName
            )
            repository.saveConfig(updated)
            _currentScreen.value = Screen.Main
            fetchBranches()
        }
    }

    fun updateFolder(uri: Uri, context: Context) {
        viewModelScope.launch {
            val displayPath = getFriendlyPath(uri)
            repository.updateFolder(uri.toString(), displayPath)
        }
    }

    fun updateBranch(branch: String) {
        viewModelScope.launch {
            repository.updateBranch(branch)
        }
    }

    fun markCoachMarksAsShown() {
        viewModelScope.launch {
            repository.updateCoachMarks(shown = true)
        }
    }

    fun resetSetup() {
        _currentScreen.value = Screen.Setup
    }

    fun navigateToMain() {
        _currentScreen.value = Screen.Main
        _syncState.value = SyncState.Idle
    }

    fun fetchBranches() {
        val config = currentConfig.value ?: return
        if (config.pat.isBlank() || config.owner.isBlank() || config.repoName.isBlank()) return

        viewModelScope.launch {
            _isFetchingBranches.value = true
            try {
                val service = GithubClient.createService(config.pat)
                val response = service.getBranches(config.owner, config.repoName)
                if (response.isNotEmpty()) {
                    _branches.value = response.map { it.name }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isFetchingBranches.value = false
            }
        }
    }

    fun startPushAndCommit(context: Context, commitMessage: String) {
        val message = commitMessage.trim().ifBlank { "Automated commit from Git Push Folder" }
        viewModelScope.launch {
            _syncState.value = SyncState.ScanningLocalFiles(0)
            _currentScreen.value = Screen.Progress

            try {
                val config = currentConfig.value ?: throw IllegalStateException("GitHub credentials are not set up.")
                val uriString = config.selectedFolderUri ?: throw IllegalStateException("Please select a local folder first.")
                val uri = Uri.parse(uriString)

                // 1. Recursive local folder scan
                val files = FolderScanner.listAllFiles(context, uri)
                if (files.isEmpty()) {
                    throw IllegalStateException("The selected folder does not contain any file or directory.")
                }

                _syncState.value = SyncState.ScanningLocalFiles(files.size)
                delay(1200) // slight buffer for responsive transition aesthetics

                // 2. Instantiate and request refs
                val service = GithubClient.createService(config.pat)

                _syncState.value = SyncState.FetchingRef
                val refResponse = try {
                    service.getRef(config.owner, config.repoName, config.selectedBranch)
                } catch (e: Exception) {
                    null // If branch doesn't exist yet
                }

                var parentCommitSha: String? = null
                var didInitialUpload = false

                if (refResponse != null) {
                    parentCommitSha = refResponse.`object`.sha
                } else {
                    // Selected branch doesn't exist yet, or repository is empty. Let's see if any other branch exists.
                    val existingBranches = try {
                        service.getBranches(config.owner, config.repoName)
                    } catch (e: Exception) {
                        emptyList()
                    }

                    if (existingBranches.isNotEmpty()) {
                        // The repo is NOT empty! Let's base our new branch off the first existing branch of the repo.
                        val baseBranch = existingBranches.first().name
                        val baseRef = try {
                            service.getRef(config.owner, config.repoName, baseBranch)
                        } catch (e: Exception) {
                            null
                        }
                        parentCommitSha = baseRef?.`object`?.sha
                    } else {
                        // The repository is COMPLETELY empty!
                        // Let's perform an initial file creation (using the first file in our scanned files list) to initialize the repository.
                        val firstFile = files.first()
                        _syncState.value = SyncState.UploadingBlobs(
                            total = files.size,
                            uploaded = 1,
                            currentFile = firstFile.relativePath
                        )
                        val uploadMsg = "Initial commit: Add ${firstFile.relativePath}"
                        val uploadReq = CreateOrUpdateFileRequest(
                            message = uploadMsg,
                            content = firstFile.base64Content,
                            branch = config.selectedBranch
                        )
                        val initResponse = service.createOrUpdateFile(
                            owner = config.owner,
                            repo = config.repoName,
                            path = firstFile.relativePath,
                            body = uploadReq
                        )
                        parentCommitSha = initResponse.commit?.sha
                        didInitialUpload = true
                    }
                }

                val baseTreeSha = if (parentCommitSha != null) {
                    _syncState.value = SyncState.FetchingCommitDetail
                    val commitDetail = service.getCommitDetails(config.owner, config.repoName, parentCommitSha)
                    commitDetail.tree.sha
                } else {
                    null
                }

                // 3. Create blob on GitHub for each remaining scanned file
                val total = files.size
                val entries = mutableListOf<TreeEntry>()
                val startIndex = if (didInitialUpload) 1 else 0

                if (startIndex < total) {
                    for (index in startIndex until total) {
                        val scannedFile = files[index]
                        _syncState.value = SyncState.UploadingBlobs(
                            total = total,
                            uploaded = index + 1,
                            currentFile = scannedFile.relativePath
                        )

                        val blobReq = BlobRequest(content = scannedFile.base64Content)
                        val blobResp = service.createBlob(config.owner, config.repoName, blobReq)

                        entries.add(
                            TreeEntry(
                                path = scannedFile.relativePath,
                                mode = "100644",
                                type = "blob",
                                sha = blobResp.sha
                            )
                        )
                    }

                    // 4. Create new tree on GitHub
                    _syncState.value = SyncState.CreatingTree
                    delay(500)
                    val treeReq = TreeRequest(base_tree = baseTreeSha, tree = entries)
                    val treeResp = service.createTree(config.owner, config.repoName, treeReq)

                    // 5. Create new commit linking tree and parent commit
                    _syncState.value = SyncState.CreatingCommit
                    delay(500)
                    val parents = if (parentCommitSha != null) listOf(parentCommitSha) else emptyList()
                    val commitReq = CommitRequest(message = message, tree = treeResp.sha, parents = parents)
                    val commitResp = service.createCommit(config.owner, config.repoName, commitReq)

                    // 6. Push reference branch update
                    _syncState.value = SyncState.UpdatingRef
                    delay(500)
                    val finalCommitSha = commitResp.sha
                    if (refResponse != null) {
                        service.updateRef(
                            owner = config.owner,
                            repo = config.repoName,
                            branch = config.selectedBranch,
                            body = UpdateRefRequest(sha = finalCommitSha, force = true)
                        )
                    } else {
                        // Dynamically create the branch reference
                        service.createRef(
                            owner = config.owner,
                            repo = config.repoName,
                            body = CreateRefRequest(
                                ref = "refs/heads/${config.selectedBranch}",
                                sha = finalCommitSha
                            )
                        )
                    }

                    // Success! Append to log and update UI
                    repository.addCommitLog(
                        CommitLog(
                            commitMsg = message,
                            branchName = config.selectedBranch,
                            commitSha = finalCommitSha
                        )
                    )

                    _syncState.value = SyncState.Success(
                        commitSha = finalCommitSha,
                        summary = "Successfully pushed $total file(s) to branch '${config.selectedBranch}'!"
                    )
                } else {
                    // Success (Only 1 file total, and uploaded during init)
                    val finalCommitSha = parentCommitSha ?: ""
                    repository.addCommitLog(
                        CommitLog(
                            commitMsg = message,
                            branchName = config.selectedBranch,
                            commitSha = finalCommitSha
                        )
                    )

                    _syncState.value = SyncState.Success(
                        commitSha = finalCommitSha,
                        summary = "Successfully pushed $total file(s) to branch '${config.selectedBranch}'!"
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                var errorMsg = e.localizedMessage ?: e.message ?: "An unexpected error occurred during GitHub push."
                if (e is retrofit2.HttpException) {
                    try {
                        val errorBody = e.response()?.errorBody()?.string()
                        if (!errorBody.isNullOrBlank()) {
                            errorMsg += " (Details: $errorBody)"
                        }
                    } catch (err: Exception) {
                        // Ignore body parsing errors
                    }
                }
                _syncState.value = SyncState.Error(
                    errorMsg = errorMsg
                )
            }
        }
    }

    private fun getFriendlyPath(uri: Uri): String {
        val path = uri.path ?: return uri.toString()
        if (path.contains(":")) {
            val parts = path.split(":")
            if (parts.size > 1) {
                return parts[1]
            }
        }
        return uri.lastPathSegment ?: path
    }
}

class GitSyncViewModelFactory(private val repository: GithubSyncRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GitSyncViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GitSyncViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
