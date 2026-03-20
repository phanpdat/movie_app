package com.example.movie_app.data.repository

import com.example.movie_app.BuildConfig
import com.example.movie_app.data.api.MovieApi
import com.example.movie_app.data.local.FavoriteDao
import com.example.movie_app.data.model.FavoriteMovie

class MovieRepository(
    private val api: MovieApi,
    private val favoriteDao: FavoriteDao
) {
    suspend fun getPopularMovies(page: Int = 1) = api.getPopularMovies(BuildConfig.TMDB_API_KEY, page)
    suspend fun getNowPlayingMovies(page: Int = 1) = api.getNowPlayingMovies(BuildConfig.TMDB_API_KEY, page)
    suspend fun getUpcomingMovies(page: Int = 1) = api.getUpcomingMovies(BuildConfig.TMDB_API_KEY, page)
    suspend fun getTopRatedMovies(page: Int = 1) = api.getTopRatedMovies(BuildConfig.TMDB_API_KEY, page)
    
    suspend fun getGenres() = api.getGenres(BuildConfig.TMDB_API_KEY)
    
    suspend fun searchMovies(query: String, page: Int = 1) = api.searchMovies(BuildConfig.TMDB_API_KEY, query, page)
    
    suspend fun getMovieDetail(movieId: Int) = api.getMovieDetail(movieId, BuildConfig.TMDB_API_KEY)
    suspend fun getMovieVideos(movieId: Int) = api.getMovieVideos(movieId, BuildConfig.TMDB_API_KEY)
    suspend fun getMovieCredits(movieId: Int) = api.getMovieCredits(movieId, BuildConfig.TMDB_API_KEY)
    
    suspend fun getMoviesByGenre(genreId: Int, page: Int = 1) = api.getMoviesByGenre(BuildConfig.TMDB_API_KEY, genreId, page)
    
    suspend fun getPersonMovies(personId: Int) = api.getPersonMovieCredits(personId, BuildConfig.TMDB_API_KEY)

    // Room operations
    fun getAllFavorites() = favoriteDao.getAllFavorites()
    
    suspend fun insertFavorite(movie: FavoriteMovie) = favoriteDao.insertFavorite(movie)
    suspend fun deleteFavorite(movie: FavoriteMovie) = favoriteDao.deleteFavorite(movie)
    suspend fun isFavorite(movieId: Int) = favoriteDao.isFavorite(movieId)
}
