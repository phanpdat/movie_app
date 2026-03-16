package com.example.movie_app.data.model

data class PersonMovieCreditsResponse(
    val cast: List<Movie>, 
    val crew: List<Movie>
)
