package com.thurman.foode.view_restaurants

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.thurman.foode.R
import com.thurman.foode.models.Restaurant
import kotlinx.android.synthetic.main.favorite_restaurant_list_item.view.*
import kotlinx.android.synthetic.main.favorite_restaurant_list_item.view.rating_bar
import kotlinx.android.synthetic.main.food_item_layout.view.*

class FavoriteRestaurantListAdapter(val items: ArrayList<Restaurant>, val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClick: ((Restaurant) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RestaurantViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.favorite_restaurant_list_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var restaurantViewHolder = holder as RestaurantViewHolder
        var restaurant = items.get(position)
        restaurantViewHolder.restaurantNameTextView.text = restaurant.name
        restaurantViewHolder.restaurantAddressTextView.text = restaurant.address
        restaurantViewHolder.ratingBar.rating = restaurant.rating.toFloat()
        if (restaurant.imageUri != null){
            Picasso.with(context).load(restaurant.imageUri).into(restaurantViewHolder.imageView, object: com.squareup.picasso.Callback {
                override fun onSuccess() {
                    restaurantViewHolder.imageView.visibility = View.VISIBLE
                    restaurantViewHolder.imageLoader.visibility = View.GONE
                }

                override fun onError() {
                    restaurantViewHolder.imageView.setImageDrawable(context.resources.getDrawable(R.drawable.question_mark_icon))
                    restaurantViewHolder.imageView.visibility = View.VISIBLE
                    restaurantViewHolder.imageLoader.visibility = View.GONE
                }
            })
        } else {
            restaurantViewHolder.imageView.setImageDrawable(context.resources.getDrawable(R.drawable.question_mark_icon))
            restaurantViewHolder.imageView.visibility = View.VISIBLE
            restaurantViewHolder.imageLoader.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class RestaurantViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        var restaurantNameTextView = view.restaurant_name
        var restaurantAddressTextView = view.restaurant_address
        var ratingBar = view.rating_bar
        var imageView = view.restaurant_image
        var imageLoader = view.fav_restaurant_image_loader

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(items[adapterPosition])
            }
        }
    }

}

