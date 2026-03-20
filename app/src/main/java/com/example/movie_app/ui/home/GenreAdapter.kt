package com.example.movie_app.ui.home
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movie_app.data.model.Genre
import com.example.movie_app.databinding.ItemGenreBinding

class GenreAdapter(
    private val onGenreClick: (Genre) -> Unit
) : RecyclerView.Adapter<GenreAdapter.GenreViewHolder>() {

    private var genres = listOf<Genre>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newGenres: List<Genre>) {
        genres = newGenres
        notifyDataSetChanged()
    }

    inner class GenreViewHolder(private val binding: ItemGenreBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(genre: Genre) {
            binding.chipGenre.text = genre.name
            binding.chipGenre.setOnClickListener { onGenreClick(genre) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val binding = ItemGenreBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GenreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) =
        holder.bind(genres[position])

    override fun getItemCount() = genres.size
}
