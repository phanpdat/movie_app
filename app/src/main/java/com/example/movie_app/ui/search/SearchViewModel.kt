package com.example.movie_app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_app.data.model.MovieResponse
import com.example.movie_app.data.repository.MovieRepository
import com.example.movie_app.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _searchResult = MutableStateFlow<Resource<MovieResponse>>(Resource.Loading())
    val searchResult: StateFlow<Resource<MovieResponse>> = _searchResult

    fun searchMovies(query: String) = viewModelScope.launch {
        if (query.isEmpty()) {
            _searchResult.value = Resource.Success(MovieResponse(1, emptyList(), 1, 0))
            return@launch
        }
        
        _searchResult.value = Resource.Loading()
        try {
            val response = repository.searchMovies(query)
            _searchResult.value = Resource.Success(response)
        } catch (e: Exception) {
            _searchResult.value = Resource.Error(e.message ?: "Unknown Error")
        }
    }
}
