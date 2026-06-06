package com.starweave.android.api

import com.starweave.android.model.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiService {

    // ── User ──
    @POST("user/login")
    suspend fun loginAnonymous(@Body body: Map<String, String>): ApiResponse<User>

    @POST("user/register")
    suspend fun register(@Body body: Map<String, String>): ApiResponse<User>

    @POST("user/login/password")
    suspend fun loginWithPassword(@Body body: Map<String, String>): ApiResponse<User>

    @GET("user/{id}")
    suspend fun getUser(@Path("id") id: Long): ApiResponse<User>

    @PUT("user/{id}")
    suspend fun updateProfile(@Path("id") id: Long, @Body body: Map<String, String>): ApiResponse<User>

    @POST("user/{id}/avatar")
    suspend fun setAvatar(@Path("id") id: Long, @Body body: Map<String, String>): ApiResponse<User>

    @Multipart
    @POST("user/{id}/avatar/upload")
    suspend fun uploadAvatar(@Path("id") id: Long, @Part file: MultipartBody.Part): ApiResponse<User>

    @POST("user/{id}/password")
    suspend fun changePassword(@Path("id") id: Long, @Body body: Map<String, String>): ApiResponse<User>

    @GET("user/{id}/stats")
    suspend fun getUserStats(@Path("id") id: Long): ApiResponse<UserStats>

    // ── Meteors ──
    @POST("meteors")
    suspend fun publishMeteor(@Body body: Map<String, String>): ApiResponse<Message>

    @GET("meteors/random")
    suspend fun getRandomMeteor(@Query("userId") userId: Long): ApiResponse<Message>

    @GET("meteors/{id}")
    suspend fun getMeteor(@Path("id") id: Long): ApiResponse<Message>

    @POST("meteors/{id}/catch")
    suspend fun catchMeteor(@Path("id") id: Long, @Body body: Map<String, String>): ApiResponse<Message>

    @POST("meteors/{meteorId}/wish")
    suspend fun makeWish(@Path("meteorId") meteorId: Long, @Body body: Map<String, String>): ApiResponse<Message>

    @HTTP(method = "DELETE", path = "meteors/{id}", hasBody = true)
    suspend fun deleteMeteor(@Path("id") id: Long, @Body body: Map<String, String>): ApiResponse<Message>

    @GET("meteors/{meteorId}/wishes")
    suspend fun getWishes(@Path("meteorId") meteorId: Long): ApiResponse<List<Wish>>

    @GET("meteors/user/{userId}")
    suspend fun getUserMeteors(@Path("userId") userId: Long): ApiResponse<List<Message>>

    @GET("meteors/caught/{userId}")
    suspend fun getCaughtMeteors(@Path("userId") userId: Long): ApiResponse<List<Message>>

    @GET("meteors/wishes/user/{userId}")
    suspend fun getUserWishes(@Path("userId") userId: Long): ApiResponse<List<Map<String, Any>>>

    @HTTP(method = "DELETE", path = "meteors/wishes/{wishId}", hasBody = true)
    suspend fun deleteWish(@Path("wishId") wishId: Long, @Body body: Map<String, String>): ApiResponse<Message>

    // ── Star Map ──
    @GET("star-map/{id}")
    suspend fun getStarMap(@Path("id") id: Long): ApiResponse<StarMap>

    @GET("star-map/user/{userId}")
    suspend fun getUserStarMaps(@Path("userId") userId: Long): ApiResponse<List<StarMap>>

    @POST("star-map/unlock")
    suspend fun unlockStarMap(@Body body: Map<String, Long>): ApiResponse<StarMap>

    // ── Sponsor ──
    @GET("sponsor/guardians")
    suspend fun getGuardians(): ApiResponse<List<Sponsor>>

    @GET("sponsor/count")
    suspend fun getGuardianCount(): ApiResponse<Long>

    // ── Admin ──
    @GET("admin/pending")
    suspend fun getPendingReviews(@Query("adminId") adminId: Long): ApiResponse<List<Message>>

    @POST("admin/review/{messageId}")
    suspend fun reviewMessage(
        @Path("messageId") messageId: Long,
        @Query("adminId") adminId: Long,
        @Body body: Map<String, String>
    ): ApiResponse<Message>

    @GET("admin/stats")
    suspend fun getAdminStats(@Query("adminId") adminId: Long): ApiResponse<Map<String, Int>>

    @GET("admin/messages")
    suspend fun getAllMessages(
        @Query("adminId") adminId: Long,
        @Query("status") status: String? = null
    ): ApiResponse<List<Message>>

    @DELETE("admin/meteors/{messageId}")
    suspend fun deleteMeteorAdmin(
        @Path("messageId") messageId: Long,
        @Query("adminId") adminId: Long
    ): ApiResponse<Message>

    @GET("admin/wishes/pending")
    suspend fun getPendingWishes(@Query("adminId") adminId: Long): ApiResponse<List<Wish>>

    @GET("admin/wishes")
    suspend fun getAllWishes(
        @Query("adminId") adminId: Long,
        @Query("status") status: String? = null
    ): ApiResponse<List<Wish>>

    @POST("admin/wishes/{wishId}/review")
    suspend fun reviewWish(
        @Path("wishId") wishId: Long,
        @Query("adminId") adminId: Long,
        @Body body: Map<String, String>
    ): ApiResponse<Wish>

    @DELETE("admin/wishes/{wishId}")
    suspend fun deleteWishAdmin(
        @Path("wishId") wishId: Long,
        @Query("adminId") adminId: Long
    ): ApiResponse<Message>

    @GET("admin/wishes/stats")
    suspend fun getWishStats(@Query("adminId") adminId: Long): ApiResponse<Map<String, Int>>

    @GET("admin/users")
    suspend fun getAdminUsers(@Query("adminId") adminId: Long): ApiResponse<List<User>>

    @DELETE("admin/users/{userId}")
    suspend fun deleteUserAdmin(
        @Path("userId") userId: Long,
        @Query("adminId") adminId: Long
    ): ApiResponse<Message>
}
