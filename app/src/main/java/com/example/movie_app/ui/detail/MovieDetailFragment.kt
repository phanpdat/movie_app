package com.example.movie_app.ui.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.movie_app.R
import com.example.movie_app.data.api.RetrofitClient
import com.example.movie_app.data.local.MovieDatabase
import com.example.movie_app.data.model.*
import com.example.movie_app.data.repository.MovieRepository
import com.example.movie_app.databinding.FragmentMovieDetailBinding
import com.example.movie_app.util.Constants
import com.example.movie_app.util.Resource
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MovieDetailFragment : Fragment(R.layout.fragment_movie_detail) {

    private var _binding: FragmentMovieDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MovieDetailViewModel
    private lateinit var castAdapter: CastAdapter
    private lateinit var productionAdapter: ProductionAdapter
    
    private var currentMovie: MovieDetail? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMovieDetailBinding.bind(view)

        val movieId = arguments?.getInt("movieId") ?: return

        val db = MovieDatabase.getDatabase(requireContext())
        val repository = MovieRepository(RetrofitClient.api, db.favoriteDao())
        val factory = MovieDetailViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MovieDetailViewModel::class.java]

        setupRecyclerViews()
        setupListeners()
        
        viewModel.loadMovieData(movieId)
        observeData()
    }

    private fun setupRecyclerViews() {
        castAdapter = CastAdapter { cast ->
            val bundle = Bundle().apply {
                putInt("personId", cast.id)
                putString("personName", cast.name)
            }
            findNavController().navigate(R.id.action_movieDetailFragment_to_movieListFragment, bundle)
        }
        binding.rvCast.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = castAdapter
        }

        productionAdapter = ProductionAdapter()
        binding.rvProduction.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = productionAdapter
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.fabFavorite.setOnClickListener {
            currentMovie?.let { detail ->
                val fav = FavoriteMovie(
                    detail.id, detail.title, detail.poster_path,
                    detail.vote_average, detail.overview, detail.release_date
                )
                viewModel.toggleFavorite(fav)
            }
        }
    }

    private fun observeData() {
        // Detail
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.movieDetail.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.detailContent.visibility = View.VISIBLE
                        resource.data?.let { setupUI(it) }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        showError(resource.message ?: "Failed to load movie details")
                    }
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.detailContent.visibility = View.GONE
                    }
                }
            }
        }

        // Cast
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.credits.collect { resource ->
                if (resource is Resource.Success) {
                    val list = resource.data?.cast ?: emptyList()
                    castAdapter.submitList(list)
                    binding.tvCastLabel.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                    binding.rvCast.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }

        // Production
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.movieDetail.collect { resource ->
                if (resource is Resource.Success) {
                    val list = resource.data?.production_companies ?: emptyList()
                    productionAdapter.submitList(list)
                    binding.tvProductionLabel.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                    binding.rvProduction.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }

        // Videos (Trailer)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.videos.collect { resource ->
                if (resource is Resource.Success) {
                    val trailer = resource.data?.results?.firstOrNull { 
                        it.site == "YouTube" && it.type == "Trailer" 
                    }
                    binding.btnTrailer.visibility = if (trailer != null) View.VISIBLE else View.GONE
                    binding.btnTrailer.setOnClickListener {
                        trailer?.let { playTrailer(it.key) }
                    }
                }
            }
        }

        // Favorite State
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isFavorite.collect { isFav ->
                binding.fabFavorite.setImageResource(
                    if (isFav) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
                )
                binding.fabFavorite.imageTintList = android.content.res.ColorStateList.valueOf(
                    if (isFav) ContextCompat.getColor(requireContext(), R.color.red_accent) 
                    else ContextCompat.getColor(requireContext(), R.color.white)
                )
            }
        }
    }

    private fun setupUI(movie: MovieDetail) {
        currentMovie = movie
        binding.tvTitle.text = movie.title
        binding.tvRating.text = String.format("%.1f", movie.vote_average)
        binding.tvReleaseDate.text = movie.release_date
        binding.tvOverview.text = movie.overview

        Glide.with(this)
            .load(Constants.BACKDROP_BASE_URL + movie.backdrop_path)
            .placeholder(R.drawable.shimmer_placeholder)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.ivBackdrop)

        Glide.with(this)
            .load(Constants.IMAGE_BASE_URL + movie.poster_path)
            .placeholder(R.drawable.shimmer_placeholder)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.ivPoster)

        setupGenreChips(movie)
    }

    private fun setupGenreChips(movie: MovieDetail) {
        binding.chipGroupGenres.removeAllViews()
        movie.genres.forEach { genre ->
            val chip = Chip(context)
            chip.text = genre.name
            chip.setOnClickListener {
                val bundle = Bundle().apply {
                    putInt("genreId", genre.id)
                    putString("genreName", genre.name)
                }
                findNavController().navigate(R.id.action_movieDetailFragment_to_movieListFragment, bundle)
            }
            binding.chipGroupGenres.addView(chip)
        }
    }

    private fun playTrailer(videoKey: String) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoKey"))
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.YOUTUBE_BASE_URL + videoKey))
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(webIntent)
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
