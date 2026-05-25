package com.example.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

// Models of GitHub Git Data API
data class RefResponse(
    val ref: String,
    val url: String,
    val `object`: RefObject
)

data class RefObject(
    val sha: String,
    val type: String,
    val url: String
)

data class CommitDetailResponse(
    val sha: String,
    val url: String,
    val tree: TreeObject
)

data class TreeObject(
    val sha: String,
    val url: String
)

data class BlobRequest(
    val content: String,
    val encoding: String = "base64"
)

data class BlobResponse(
    val sha: String,
    val url: String
)

data class TreeRequest(
    val base_tree: String? = null,
    val tree: List<TreeEntry>
)

data class TreeEntry(
    val path: String,
    val mode: String = "100644",
    val type: String = "blob",
    val sha: String
)

data class TreeResponse(
    val sha: String,
    val url: String,
    val tree: List<TreeEntryResponse>? = null
)

data class TreeEntryResponse(
    val path: String,
    val mode: String,
    val type: String,
    val sha: String,
    val size: Int? = null,
    val url: String? = null
)

data class CommitRequest(
    val message: String,
    val tree: String,
    val parents: List<String>
)

data class CommitResponse(
    val sha: String,
    val url: String,
    val message: String,
    val tree: TreeObject
)

data class UpdateRefRequest(
    val sha: String,
    val force: Boolean = false
)

data class CreateRefRequest(
    val ref: String,
    val sha: String
)

data class BranchResponseItem(
    val name: String
)

interface GithubApiService {
    @GET("repos/{owner}/{repo}/git/ref/heads/{branch}")
    suspend fun getRef(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("branch") branch: String
    ): RefResponse

    @GET("repos/{owner}/{repo}/git/commits/{commit_sha}")
    suspend fun getCommitDetails(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("commit_sha") commitSha: String
    ): CommitDetailResponse

    @POST("repos/{owner}/{repo}/git/blobs")
    suspend fun createBlob(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body body: BlobRequest
    ): BlobResponse

    @POST("repos/{owner}/{repo}/git/trees")
    suspend fun createTree(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body body: TreeRequest
    ): TreeResponse

    @POST("repos/{owner}/{repo}/git/commits")
    suspend fun createCommit(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body body: CommitRequest
    ): CommitResponse

    @PATCH("repos/{owner}/{repo}/git/refs/heads/{branch}")
    suspend fun updateRef(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("branch") branch: String,
        @Body body: UpdateRefRequest
    ): RefResponse

    @POST("repos/{owner}/{repo}/git/refs")
    suspend fun createRef(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body body: CreateRefRequest
    ): RefResponse

    @GET("repos/{owner}/{repo}/branches")
    suspend fun getBranches(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): List<BranchResponseItem>
}

object GithubClient {
    fun createService(pat: String): GithubApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                if (pat.isNotBlank()) {
                    requestBuilder.header("Authorization", "token $pat")
                }
                requestBuilder.header("Accept", "application/vnd.github+json")
                requestBuilder.header("User-Agent", "GitPushFolderApp")
                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(logging)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GithubApiService::class.java)
    }
}
