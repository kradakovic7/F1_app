package com.example.f1fantasyapp

object UserManager {
    var userId: Int = -1
    var username: String? = null
    var budget: Double = 0.0

    var isAdmin: Boolean = false

    fun isLoggedIn(): Boolean {
        return userId != -1
    }

    fun logout() {
        userId = -1
        username = null
        budget = 0.0
        isAdmin = false
    }
}