package com.example.movie_app.data.model

data class MovieDetail(
    val id: Int, 
    val title: String, 
    val overview: String,
    val poster_path: String?, 
    val backdrop_path: String?,
    val vote_average: Double, 
    val release_date: String?,
    val runtime: Int?, 
    val genres: List<Genre>,
    val budget: Long?, 
    val revenue: Long?,
    val status: String?, val tagline: String?,
    val production_companies: List<ProductionCompany>?
)
