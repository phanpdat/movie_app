package com.example.movie_app.ui.favorite

import android.R.drawable.ic_menu_report_image
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.movie_app.R
import com.example.movie_app.data.model.FavoriteMovie
import com.example.movie_app.databinding.ItemMovieGridBinding
import com.example.movie_app.util.Constants

class FavoriteAdapter(
    private val onMovieClick: (FavoriteMovie) -> Unit
) : ListAdapter<FavoriteMovie, FavoriteAdapter.FavoriteViewHolder>(FavoriteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemMovieGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FavoriteViewHolder(private val binding: ItemMovieGridBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("DefaultLocale")
        fun bind(movie: FavoriteMovie) {
            binding.tvTitle.text = movie.title
            binding.tvRating.text = String.format("%.1f", movie.voteAverage)
            
            Glide.with(binding.root.context)
                .load(Constants.IMAGE_BASE_URL + movie.posterPath)
                .placeholder(R.drawable.shimmer_placeholder)
                .error(ic_menu_report_image)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.ivPoster)

            binding.root.setOnClickListener { onMovieClick(movie) }
        }
    }

    class FavoriteDiffCallback : DiffUtil.ItemCallback<FavoriteMovie>() {
        override fun areItemsTheSame(oldItem: FavoriteMovie, newItem: FavoriteMovie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FavoriteMovie, newItem: FavoriteMovie): Boolean {
            return oldItem == newItem
        }
    }
}
