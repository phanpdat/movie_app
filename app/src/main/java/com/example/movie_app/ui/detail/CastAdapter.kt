package com.example.movie_app.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movie_app.R
import com.example.movie_app.data.model.Cast
import com.example.movie_app.databinding.ItemCastBinding
import com.example.movie_app.util.Constants

class CastAdapter(
    private val onCastClick: (Cast) -> Unit
) : RecyclerView.Adapter<CastAdapter.CastViewHolder>() {

    private var castList = listOf<Cast>()

    fun submitList(list: List<Cast>) {
        castList = list
        notifyDataSetChanged()
    }

    inner class CastViewHolder(private val binding: ItemCastBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(cast: Cast) {
            binding.tvName.text = cast.name
            binding.tvCharacter.text = cast.character

            Glide.with(binding.root.context)
                .load(Constants.IMAGE_BASE_URL + cast.profile_path)
                .placeholder(android.R.drawable.progress_horizontal)
                .error(android.R.drawable.ic_menu_report_image)
                .into(binding.ivCast)

            binding.root.setOnClickListener { onCastClick(cast) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val binding = ItemCastBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) =
        holder.bind(castList[position])

    override fun getItemCount() = castList.size
}
