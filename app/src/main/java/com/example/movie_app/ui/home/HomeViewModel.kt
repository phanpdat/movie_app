package com.example.movie_app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_app.data.model.GenreResponse
import com.example.movie_app.data.model.MovieResponse
import com.example.movie_app.data.repository.MovieRepository
import com.example.movie_app.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _popularMovies = MutableStateFlow<Resource<MovieResponse>>(Resource.Loading())
    val popularMovies: StateFlow<Resource<MovieResponse>> = _popularMovies

    private val _nowPlayingMovies = MutableStateFlow<Resource<MovieResponse>>(Resource.Loading())
    val nowPlayingMovies: StateFlow<Resource<MovieResponse>> = _nowPlayingMovies

    private val _upcomingMovies = MutableStateFlow<Resource<MovieResponse>>(Resource.Loading())
    val upcomingMovies: StateFlow<Resource<MovieResponse>> = _upcomingMovies

    private val _topRatedMovies = MutableStateFlow<Resource<MovieResponse>>(Resource.Loading())
    val topRatedMovies: StateFlow<Resource<MovieResponse>> = _topRatedMovies

    private val _genres = MutableStateFlow<Resource<GenreResponse>>(Resource.Loading())
    val genres: StateFlow<Resource<GenreResponse>> = _genres

    init {
        loadAllData()
    }

    fun loadAllData() {
        loadPopularMovies()
        loadNowPlayingMovies()
        loadUpcomingMovies()
        loadTopRatedMovies()
        loadGenres()
    }

    private fun loadPopularMovies() = viewModelScope.launch {
        _popularMovies.value = Resource.Loading()
        try {
            val response = repository.getPopularMovies()
            _popularMovies.value = Resource.Success(response)
        } catch (e: Exception) {
            _popularMovies.value = Resource.Error(e.message ?: "Unknown Error")
        }
    }

    private fun loadNowPlayingMovies() = viewModelScope.launch {
        _nowPlayingMovies.value = Resource.Loading()
        try {
            val response = repository.getNowPlayingMovies()
            _nowPlayingMovies.value = Resource.Success(response)
        } catch (e: Exception) {
            _nowPlayingMovies.value = Resource.Error(e.message ?: "Unknown Error")
        }
    }

    private fun loadUpcomingMovies() = viewModelScope.launch {
        _upcomingMovies.value = Resource.Loading()
        try {
            val response = repository.getUpcomingMovies()
            _upcomingMovies.value = Resource.Success(response)
        } catch (e: Exception) {
            _upcomingMovies.value = Resource.Error(e.message ?: "Unknown Error")
        }
    }

    private fun loadTopRatedMovies() = viewModelScope.launch {
        _topRatedMovies.value = Resource.Loading()
        try {
            val response = repository.getTopRatedMovies()
            _topRatedMovies.value = Resource.Success(response)
        } catch (e: Exception) {
            _topRatedMovies.value = Resource.Error(e.message ?: "Unknown Error")
        }
    }

    private fun loadGenres() = viewModelScope.launch {
        _genres.value = Resource.Loading()
        try {
            val response = repository.getGenres()
            _genres.value = Resource.Success(response)
        } catch (e: Exception) {
            _genres.value = Resource.Error(e.message ?: "Unknown Error")
        }
    }
}
