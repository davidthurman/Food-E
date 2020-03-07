package com.thurman.foode.view_restaurants

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.thurman.foode.R
import com.thurman.foode.models.City
import com.thurman.foode.models.Restaurant
import kotlinx.android.synthetic.main.city_search_list_item.view.*
import kotlinx.android.synthetic.main.favorite_restaurant_list_item.view.*
import kotlinx.android.synthetic.main.favorite_restaurant_list_item.view.rating_bar
import kotlinx.android.synthetic.main.food_item_layout.view.*

class SearchCityListAdapter(val items: ArrayList<City>, val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClick: ((City) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CityViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.city_search_list_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var cityViewHolder = holder as CityViewHolder
        var city = items.get(position)
        cityViewHolder.cityTitleTextView.text = city.title
        cityViewHolder.cityCountryTextView.text = city.country
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class CityViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        var cityTitleTextView = view.city_title
        var cityCountryTextView = view.city_country

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(items[adapterPosition])
            }
        }
    }

}

