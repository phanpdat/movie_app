package com.example.movie_app.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.movie_app.R
import com.example.movie_app.data.api.RetrofitClient
import com.example.movie_app.data.local.MovieDatabase
import com.example.movie_app.data.model.*
import com.example.movie_app.data.repository.MovieRepository
import com.example.movie_app.databinding.FragmentSearchBinding
import com.example.movie_app.ui.movielist.MovieGridAdapter
import com.example.movie_app.util.Resource
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SearchViewModel
    private lateinit var searchAdapter: MovieGridAdapter
    private var searchJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        val db = MovieDatabase.getDatabase(requireContext())
        val repository = MovieRepository(RetrofitClient.api, db.favoriteDao())
        val factory = SearchViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[SearchViewModel::class.java]

        setupRecyclerView()
        setupSearchLogic()
        observeData()
    }

    private fun setupRecyclerView() {
        searchAdapter = MovieGridAdapter { movie ->
            val bundle = Bundle().apply { putInt("movieId", movie.id) }
            findNavController().navigate(R.id.action_searchFragment_to_movieDetailFragment, bundle)
        }
        binding.rvSearch.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = searchAdapter
        }
    }

    private fun setupSearchLogic() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Debounce logic: cancel the previous job and start a new one
                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(500) 
                    val query = p0.toString().trim()
                    viewModel.searchMovies(query)
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResult.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        val movies = resource.data?.results ?: emptyList()
                        searchAdapter.submitList(movies)
                        
                        if (movies.isEmpty()) {
                            binding.emptyState.visibility = if (binding.etSearch.text?.isNotEmpty() == true) 
                                View.VISIBLE else View.GONE
                        } else {
                            binding.emptyState.visibility = View.GONE
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        showError(resource.message ?: "Search failed")
                        binding.emptyState.visibility = View.VISIBLE
                    }
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.emptyState.visibility = View.GONE
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
