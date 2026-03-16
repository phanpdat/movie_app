package com.example.movie_app.data.model

data class Cast(
    val id: Int, 
    val name: String, 
    val character: String?,
    val profile_path: String?, 
    val known_for_department: String?
)

data class Crew(
    val id: Int, 
    val name: String, 
    val job: String?,
    val department: String?, 
    val profile_path: String?
)

data class CreditsResponse(
    val id: Int, 
    val cast: List<Cast>, 
    val crew: List<Crew>
)
