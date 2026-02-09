package com.example.f1fantasyapp

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// ==========================================
// 1. DATA MODELI
// ==========================================

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val user_id: Int,
    val username: String,
    val is_admin: Boolean
)

data class RegisterRequest(
    val username: String,
    val password: String
)

data class SimpleResponse(
    val message: String,
    val user_id: Int?
)

data class Constructor(
    val id: Int,
    val name: String,
    val price: Double,
    val points: Double
)

data class MyTeamResponse(
    val id: Int,
    val username: String,
    val budget: Double,
    @SerializedName("total_points")
    val totalPoints: Double,
    val drivers: List<Driver>,
    val constructors: List<Constructor>
)

data class SaveTeamRequest(
    val user_id: Int,
    val driver_ids: List<Int>,
    val constructor_ids: List<Int>
)

data class UserRanking(
    val id: Int,
    val username: String,
    val points: Double,
    @SerializedName("team_value")
    val teamValue: Double,
    val rank: Int
)

data class Race(
    val round: Int,
    val name: String,
    val location: String,
    val date: String
)

data class RaceResult(
    val position: Int,
    val driver: String,
    val team: String,
    val time: String,
    val points: Double
)

// ================
// 2. API INTERFACE
// ================

interface ApiService {

    // --- AUTH ---
    @POST("/api/login") // To je že delalo
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("/api/register") // To je že delalo
    fun registerUser(@Body request: RegisterRequest): Call<SimpleResponse>

    // --- FANTASY ---
    @POST("/api/fantasy/create-team") // <--- DODANO /api
    fun saveTeam(@Body request: SaveTeamRequest): Call<SimpleResponse>

    @GET("/api/fantasy/my-team/{userId}") // <--- DODANO /api
    fun getMyTeam(@Path("userId") userId: Int): Call<MyTeamResponse>

    @GET("/api/drivers") // <--- DODANO /api (To bo popravilo drivers!)
    fun getDrivers(): Call<List<Driver>>

    @GET("/api/leaderboard") // <--- DODANO /api
    fun getLeaderboard(): Call<List<UserRanking>>

    // --- DATA / STATS ---
    @GET("/api/calendar") // <--- DODANO /api
    fun getCalendar(): Call<List<Race>>

    @GET("/api/results/{round}") // <--- DODANO /api
    fun getRaceResults(@Path("round") round: Int): Call<List<RaceResult>>

    @GET("/api/constructors")
    fun getConstructors(): Call<List<Constructor>>

    // --- USER MANAGEMENT ---
    @retrofit2.http.DELETE("/api/delete-user/{userId}")
    fun deleteUser(@Path("userId") userId: Int): Call<SimpleResponse>

}