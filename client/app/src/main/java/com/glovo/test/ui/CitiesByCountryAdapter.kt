package com.glovo.test.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.glovo.test.R

class CitiesByCountryAdapter(private val items: List<AdapterItem>, private val callback: ((CityItem) -> Unit)) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_COUNTRY = 0
        const val TYPE_CITY = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is CountryItem -> TYPE_COUNTRY
            else -> TYPE_CITY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_COUNTRY -> {
                CountryViewHolder(inflater.inflate(R.layout.item_country, parent, false))
            }
            else -> {
                CityViewHolder(inflater.inflate(R.layout.item_city, parent, false), callback)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CountryViewHolder -> holder.bind(items[position] as CountryItem)
            is CityViewHolder -> holder.bind(items[position] as CityItem)
        }
    }

    class CountryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val countryTextView = itemView.findViewById<TextView>(R.id.countryTextView)

        fun bind(countryItem: CountryItem) {
            countryTextView.text = countryItem.name
        }
    }

    class CityViewHolder(itemView: View, private val callback: ((CityItem) -> Unit)) : RecyclerView.ViewHolder(itemView) {
        private val cityTextView = itemView.findViewById<TextView>(R.id.cityTextView)

        fun bind(cityItem: CityItem) {
            cityTextView.text = cityItem.name
            itemView.setOnClickListener {
                callback.invoke(cityItem)
            }
        }
    }

    abstract class AdapterItem
    class CountryItem(val name: String) : AdapterItem()
    class CityItem(val name: String, val code: String) : AdapterItem()
}