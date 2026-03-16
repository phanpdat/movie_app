package com.example.movie_app.ui.movielist

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
import com.example.movie_app.databinding.FragmentMovieListGenericBinding
import com.example.movie_app.util.Resource
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MovieListFragment : Fragment(R.layout.fragment_movie_list_generic) {

    private var _binding: FragmentMovieListGenericBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MovieListViewModel
    private lateinit var adapter: MovieGridAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMovieListGenericBinding.bind(view)

        val category = arguments?.getString("category") ?: "Popular"
        val genreId = arguments?.getInt("genreId") ?: -1
        val genreName = arguments?.getString("genreName")
        val personId = arguments?.getInt("personId") ?: -1
        val personName = arguments?.getString("personName")
        
        binding.tvTitle.text = personName ?: genreName ?: category

        val db = MovieDatabase.getDatabase(requireContext())
        val repository = MovieRepository(RetrofitClient.api, db.favoriteDao())
        val factory = MovieListViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MovieListViewModel::class.java]

        setupRecyclerView()

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        when {
            personId != -1 -> viewModel.loadMoviesByPerson(personId)
            genreId != -1 -> viewModel.loadMoviesByGenre(genreId)
            else -> viewModel.loadMoviesByCategory(category)
        }
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = MovieGridAdapter { movie ->
            val bundle = Bundle().apply { putInt("movieId", movie.id) }
            findNavController().navigate(R.id.action_movieListFragment_to_movieDetailFragment, bundle)
        }
        binding.rvMovies.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvMovies.adapter = adapter
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.movies.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        val results = resource.data?.results ?: emptyList()
                        adapter.submitList(results)
                        if (results.isEmpty()) {
                           binding.tvEmpty.visibility = View.VISIBLE
                        } else {
                           binding.tvEmpty.visibility = View.GONE
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        showError(resource.message ?: "Failed to load movies")
                        binding.tvEmpty.text = resource.message
                        binding.tvEmpty.visibility = View.VISIBLE
                    }
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.tvEmpty.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
