package com.example.movie_app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_app.data.model.*
import com.example.movie_app.data.repository.MovieRepository
import com.example.movie_app.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MovieDetailViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _movieDetail = MutableStateFlow<Resource<MovieDetail>>(Resource.Loading())
    val movieDetail: StateFlow<Resource<MovieDetail>> = _movieDetail

    private val _credits = MutableStateFlow<Resource<CreditsResponse>>(Resource.Loading())
    val credits: StateFlow<Resource<CreditsResponse>> = _credits

    private val _videos = MutableStateFlow<Resource<VideoResponse>>(Resource.Loading())
    val videos: StateFlow<Resource<VideoResponse>> = _videos

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    fun loadMovieData(movieId: Int) {
        loadMovieDetail(movieId)
        loadMovieCredits(movieId)
        loadMovieVideos(movieId)
        checkIsFavorite(movieId)
    }

    private fun loadMovieDetail(movieId: Int) = viewModelScope.launch {
        _movieDetail.value = Resource.Loading()
        try {
            val response = repository.getMovieDetail(movieId)
            _movieDetail.value = Resource.Success(response)
        } catch (e: Exception) {
            _movieDetail.value = Resource.Error(e.message ?: "Unknown Error")
        }
    }

    private fun loadMovieCredits(movieId: Int) = viewModelScope.launch {
        _credits.value = Resource.Loading()
        try {
            val response = repository.getMovieCredits(movieId)
            _credits.value = Resource.Success(response)
        } catch (e: Exception) {
            _credits.value = Resource.Error(e.message ?: "Unknown Error")
        }
    }

    private fun loadMovieVideos(movieId: Int) = viewModelScope.launch {
        _videos.value = Resource.Loading()
        try {
            val response = repository.getMovieVideos(movieId)
            _videos.value = Resource.Success(response)
        } catch (e: Exception) {
            _videos.value = Resource.Error(e.message ?: "Unknown Error")
        }
    }

    private fun checkIsFavorite(movieId: Int) = viewModelScope.launch {
        _isFavorite.value = repository.isFavorite(movieId)
    }

    fun toggleFavorite(movie: FavoriteMovie) = viewModelScope.launch {
        if (_isFavorite.value) {
            repository.deleteFavorite(movie)
            _isFavorite.value = false
        } else {
            repository.insertFavorite(movie)
            _isFavorite.value = true
        }
    }
}
