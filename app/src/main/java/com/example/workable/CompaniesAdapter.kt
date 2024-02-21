package com.example.workable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy


class CompaniesAdapter(private var companies: List<Company>) :
    RecyclerView.Adapter<CompaniesAdapter.CompanyViewHolder>() {

    fun updateData(newCompanies: List<Company>) {
        companies = newCompanies
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_company, parent, false)
        return CompanyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompanyViewHolder, position: Int) {
        val company = companies[position]
        holder.bind(company)
    }

    override fun getItemCount(): Int = companies.size

    class CompanyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val companyLogo: ImageView = itemView.findViewById(R.id.companyLogo)

        fun bind(company: Company) {
            Glide.with(itemView.context)
                .load(company.logo)
                .placeholder(com.google.firebase.database.R.drawable.common_google_signin_btn_icon_dark) // placeholder if not found
                .diskCacheStrategy(DiskCacheStrategy.ALL) // caching
                .into(companyLogo)
        }
    }
}
