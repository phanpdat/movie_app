package com.example.movie_app.ui.movielist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_app.data.model.MovieResponse
import com.example.movie_app.data.repository.MovieRepository
import com.example.movie_app.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MovieListViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _movies = MutableStateFlow<Resource<MovieResponse>>(Resource.Loading())
    val movies: StateFlow<Resource<MovieResponse>> = _movies

    fun loadMoviesByCategory(category: String) = viewModelScope.launch {
        _movies.value = Resource.Loading()
        try {
            val response = when (category.lowercase()) {
                "popular" -> repository.getPopularMovies()
                "now playing" -> repository.getNowPlayingMovies()
                "upcoming" -> repository.getUpcomingMovies()
                "top rated" -> repository.getTopRatedMovies()
                else -> repository.getPopularMovies()
            }
            _movies.value = Resource.Success(response)
        } catch (e: Exception) {
            _movies.value = Resource.Error(e.message ?: "Unknown Error")
        }
    }

    fun loadMoviesByGenre(genreId: Int) = viewModelScope.launch {
        _movies.value = Resource.Loading()
        try {
            val response = repository.getMoviesByGenre(genreId)
            _movies.value = Resource.Success(response)
        } catch (e: Exception) {
            _movies.value = Resource.Error(e.message ?: "Unknown Error")
        }
    }

    fun loadMoviesByPerson(personId: Int) = viewModelScope.launch {
        _movies.value = Resource.Loading()
        try {
            val response = repository.getPersonMovies(personId)
            // Combine cast and crew, then distinct by movie ID
            val allMovies = (response.cast + response.crew).distinctBy { it.id }
            val movieResponse = MovieResponse(1, allMovies, 1, allMovies.size)
            _movies.value = Resource.Success(movieResponse)
        } catch (e: Exception) {
            _movies.value = Resource.Error(e.message ?: "Unknown Error")
        }
    }
}
