package com.example.movie_app.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_app.data.model.FavoriteMovie
import com.example.movie_app.data.repository.MovieRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoriteViewModel(private val repository: MovieRepository) : ViewModel() {

    // Get favorites from Room as Flow and convert it to StateFlow
    val favoriteMovies: StateFlow<List<FavoriteMovie>> = repository.getAllFavorites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteFavorite(movie: FavoriteMovie) = viewModelScope.launch {
        repository.deleteFavorite(movie)
    }
}
