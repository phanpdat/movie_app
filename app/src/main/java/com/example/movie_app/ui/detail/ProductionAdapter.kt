package com.example.movie_app.ui.detail

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movie_app.R
import com.example.movie_app.data.model.ProductionCompany
import com.example.movie_app.databinding.ItemProductionBinding
import com.example.movie_app.util.Constants

class ProductionAdapter : RecyclerView.Adapter<ProductionAdapter.ProductionViewHolder>() {

    private var companies = listOf<ProductionCompany>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<ProductionCompany>) {
        companies = list
        notifyDataSetChanged()
    }

    inner class ProductionViewHolder(private val binding: ItemProductionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(company: ProductionCompany) {
            binding.tvProductionName.text = company.name

            Glide.with(binding.root.context)
                .load(Constants.IMAGE_BASE_URL + company.logo_path)
                .placeholder(R.drawable.app_icon)
                .into(binding.ivLogo)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductionViewHolder {
        val binding = ItemProductionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductionViewHolder, position: Int) =
        holder.bind(companies[position])

    override fun getItemCount() = companies.size
}
