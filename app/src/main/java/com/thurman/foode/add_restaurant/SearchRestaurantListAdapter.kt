package com.thurman.foode.add_restaurant

import com.thurman.foode.R

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thurman.foode.Utility.PicassoUtil
import com.thurman.foode.models.Restaurant
import com.tuyenmonkey.mkloader.MKLoader
import kotlinx.android.synthetic.main.favorite_restaurant_list_item.view.*

class SearchRestaurantListAdapter(val items: ArrayList<Restaurant>, val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClick: ((Restaurant) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RestaurantViewHolder(LayoutInflater.from(context).inflate(R.layout.search_restaurant_list_item, parent, false))

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val restaurantViewHolder = holder as RestaurantViewHolder
        val restaurant = items[position]
        restaurantViewHolder.restaurantNameTextView.text = restaurant.name
        restaurantViewHolder.restaurantAddressTextView.text = restaurant.address
        if (restaurant.googlePhotoReference != ""){
            PicassoUtil.loadGoogleImageIntoImageview(context, restaurant.googlePhotoReference, restaurantViewHolder.imageView, restaurantViewHolder.imageLoader)
        } else
        {
            restaurantViewHolder.imageView.setImageDrawable(context.resources.getDrawable(R.drawable.question_mark_icon_square))
            restaurantViewHolder.imageView.visibility = View.VISIBLE
            restaurantViewHolder.imageLoader.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class RestaurantViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        var restaurantNameTextView: TextView = view.restaurant_name
        var restaurantAddressTextView: TextView = view.restaurant_address
        var imageView: ImageView = view.restaurant_image
        var imageLoader: MKLoader = view.fav_restaurant_image_loader
        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(items[adapterPosition])
            }
        }
    }

}

