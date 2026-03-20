package com.example.movie_app.ui.favorite

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.movie_app.R
import com.example.movie_app.data.api.RetrofitClient
import com.example.movie_app.data.local.MovieDatabase
import com.example.movie_app.data.repository.MovieRepository
import com.example.movie_app.databinding.FragmentFavoriteBinding
import kotlinx.coroutines.launch
import com.example.movie_app.util.showPremiumDialog

class FavoriteFragment : Fragment(R.layout.fragment_favorite) {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FavoriteViewModel
    private lateinit var adapter: FavoriteAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFavoriteBinding.bind(view)

        val db = MovieDatabase.getDatabase(requireContext())
        val repository = MovieRepository(RetrofitClient.api, db.favoriteDao())
        val factory = FavoriteViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[FavoriteViewModel::class.java]

        setupRecyclerView()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = FavoriteAdapter { movie ->
            navigateToDetail(movie.id)
        }
        binding.rvFavorites.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@FavoriteFragment.adapter
        }
    }

    private fun navigateToDetail(movieId: Int) {
        if (com.example.movie_app.ads.AdManager.isUnlocked()) {
            val bundle = Bundle().apply { putInt("movieId", movieId) }
            findNavController().navigate(R.id.action_favoriteFragment_to_movieDetailFragment, bundle)
        } else {
            showPremiumDialog {
                val bundle = Bundle().apply { putInt("movieId", movieId) }
                findNavController().navigate(R.id.action_favoriteFragment_to_movieDetailFragment, bundle)
            }
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favoriteMovies.collect { favorites ->
                adapter.submitList(favorites)
                binding.emptyState.visibility = if (favorites.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
