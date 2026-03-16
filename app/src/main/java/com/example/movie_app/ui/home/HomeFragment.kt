package com.example.movie_app.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.movie_app.R
import com.example.movie_app.data.api.RetrofitClient
import com.example.movie_app.data.local.MovieDatabase
import com.example.movie_app.data.model.*
import com.example.movie_app.data.repository.MovieRepository
import com.example.movie_app.databinding.FragmentHomeBinding
import com.example.movie_app.ui.movielist.MovieAdapter
import com.example.movie_app.util.Constants
import com.example.movie_app.util.Resource
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel

    private lateinit var popularAdapter: MovieAdapter
    private lateinit var nowPlayingAdapter: MovieAdapter
    private lateinit var upcomingAdapter: MovieAdapter
    private lateinit var topRatedAdapter: MovieAdapter
    private lateinit var genreAdapter: GenreAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        val db = MovieDatabase.getDatabase(requireContext())
        val repository = MovieRepository(RetrofitClient.api, db.favoriteDao())
        val factory = HomeViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        setupRecyclerViews()
        observeData()
    }



    private fun setupRecyclerViews() {
        val navigateToSeeAll = { category: String ->
            val bundle = Bundle().apply { putString("category", category) }
            findNavController().navigate(R.id.action_homeFragment_to_movieListFragment, bundle)
        }

        // Popular
        popularAdapter = MovieAdapter { movie -> navigateToDetail(movie.id) }
        binding.sectionPopular.rvMovies.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = popularAdapter
        }
        binding.sectionPopular.tvSectionTitle.text = "Popular Movies"
        binding.sectionPopular.tvSeeAll.setOnClickListener { navigateToSeeAll("Popular") }

        // Now Playing
        nowPlayingAdapter = MovieAdapter { movie -> navigateToDetail(movie.id) }
        binding.sectionNowPlaying.rvMovies.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = nowPlayingAdapter
        }
        binding.sectionNowPlaying.tvSectionTitle.text = "Continue Watching"
        binding.sectionNowPlaying.tvSeeAll.setOnClickListener { navigateToSeeAll("Now Playing") }

        // Upcoming
        upcomingAdapter = MovieAdapter { movie -> navigateToDetail(movie.id) }
        binding.sectionUpcoming.rvMovies.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = upcomingAdapter
        }
        binding.sectionUpcoming.tvSectionTitle.text = "Coming Soon"
        binding.sectionUpcoming.tvSeeAll.setOnClickListener { navigateToSeeAll("Upcoming") }

        // Top Rated
        topRatedAdapter = MovieAdapter { movie -> navigateToDetail(movie.id) }
        binding.sectionTopRated.rvMovies.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = topRatedAdapter
        }
        binding.sectionTopRated.tvSectionTitle.text = "Top Rated"
        binding.sectionTopRated.tvSeeAll.setOnClickListener { navigateToSeeAll("Top Rated") }

        // Genres
        genreAdapter = GenreAdapter { genre ->
            val bundle = Bundle().apply {
                putInt("genreId", genre.id)
                putString("genreName", genre.name)
            }
            findNavController().navigate(R.id.action_homeFragment_to_movieListFragment, bundle)
        }
        binding.rvGenres.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = genreAdapter
        }
    }
    
    private fun navigateToDetail(movieId: Int) {
        if (com.example.movie_app.ads.AdManager.isUnlocked()) {
            val bundle = Bundle().apply { putInt("movieId", movieId) }
            findNavController().navigate(R.id.action_homeFragment_to_movieDetailFragment, bundle)
        } else {
            showPremiumDialog {
                val bundle = Bundle().apply { putInt("movieId", movieId) }
                findNavController().navigate(R.id.action_homeFragment_to_movieDetailFragment, bundle)
            }
        }
    }

    private fun showPremiumDialog(onUnlocked: () -> Unit) {
        val dialog = android.app.Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_unlock_premium)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val btnWatchAds = dialog.findViewById<View>(R.id.btnWatchAds)
        val btnUpgrade = dialog.findViewById<View>(R.id.btnUpgrade)
        val ivClose = dialog.findViewById<View>(R.id.ivClose)

        ivClose.setOnClickListener { dialog.dismiss() }
        btnUpgrade.setOnClickListener {
            dialog.dismiss()
            findNavController().navigate(R.id.action_homeFragment_to_subscriptionFragment)
        }

        btnWatchAds.setOnClickListener {
            dialog.dismiss()
            com.example.movie_app.ads.AdManager.showRewardedAd(requireActivity()) {
                onUnlocked()
            }
        }

        dialog.show()
    }

    private fun observeData() {
        // Genres
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.genres.collect { resource ->
                if (resource is Resource.Success) {
                    genreAdapter.submitList(resource.data?.genres ?: emptyList())
                }
            }
        }

        // Popular
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.popularMovies.collect { resource ->
                handleMovieResource(resource, popularAdapter)
                if (resource is Resource.Success) {
                    val firstMovie = resource.data?.results?.firstOrNull()
                    firstMovie?.let { setupFeaturedMovie(it) }
                }
            }
        }

        // Now Playing
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.nowPlayingMovies.collect { resource ->
                handleMovieResource(resource, nowPlayingAdapter)
            }
        }

        // Upcoming
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.upcomingMovies.collect { resource ->
                handleMovieResource(resource, upcomingAdapter)
            }
        }

        // Top Rated
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.topRatedMovies.collect { resource ->
                handleMovieResource(resource, topRatedAdapter)
            }
        }
    }

    private fun setupFeaturedMovie(movie: Movie) {
        binding.layoutFeatured.tvFeaturedTitle.text = movie.title
        binding.layoutFeatured.tvFeaturedDesc.text = movie.overview
        
        Glide.with(this)
            .load(Constants.BACKDROP_BASE_URL + movie.backdrop_path)
            .placeholder(R.drawable.shimmer_placeholder)
            .into(binding.layoutFeatured.ivFeaturedBackdrop)
            
        binding.layoutFeatured.root.setOnClickListener {
            navigateToDetail(movie.id)
        }
        
        binding.layoutFeatured.btnFeaturedPlay.setOnClickListener {
            navigateToDetail(movie.id)
        }
    }

    private fun handleMovieResource(resource: Resource<MovieResponse>, adapter: MovieAdapter) {
        when (resource) {
            is Resource.Success -> {
                adapter.submitList(resource.data?.results ?: emptyList())
            }
            is Resource.Error -> {
                showError(resource.message ?: "Network Error")
            }
            is Resource.Loading -> {}
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Retry") {
                viewModel.loadAllData()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
