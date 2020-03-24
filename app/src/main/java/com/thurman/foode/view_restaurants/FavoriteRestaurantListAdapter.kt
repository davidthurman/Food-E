package com.thurman.foode.view_restaurants

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.thurman.foode.R
import com.thurman.foode.Utility.GoogleUtil
import com.thurman.foode.Utility.PicassoUtil
import com.thurman.foode.models.Restaurant
import kotlinx.android.synthetic.main.favorite_restaurant_list_item.view.*
import kotlinx.android.synthetic.main.favorite_restaurant_list_item.view.rating_bar
import kotlinx.android.synthetic.main.food_item_layout.view.*

class FavoriteRestaurantListAdapter(val items: ArrayList<Restaurant>, val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClick: ((Restaurant) -> Unit)? = null
    private var selectedPos = RecyclerView.NO_POSITION
    private var previouslySelectedPosition = -1
    var restUuidToPosHash: HashMap<String, Int> = HashMap()

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
        holder.itemView.isSelected = (position == selectedPos)
        restaurantViewHolder.restaurantNameTextView.text = restaurant.name
        restaurantViewHolder.restaurantAddressTextView.text = restaurant.address
        restaurantViewHolder.ratingBar.rating = restaurant.rating.toFloat()
        restaurantViewHolder.mapIcon.setOnClickListener {
            GoogleUtil.openGoogleMaps(restaurant, context)
        }
        if (!restaurant.googlePhotoReference.equals("")){
            PicassoUtil.loadGoogleImageIntoImageview(context, restaurant.googlePhotoReference, restaurantViewHolder.imageView, restaurantViewHolder.imageLoader)
        } else if (restaurant.imageUri != null){
            Picasso.with(context).load(restaurant.imageUri).into(restaurantViewHolder.imageView, object: com.squareup.picasso.Callback {
                override fun onSuccess() {
                    restaurantViewHolder.imageView.visibility = View.VISIBLE
                    restaurantViewHolder.imageLoader.visibility = View.GONE
                }

                override fun onError() {
                    restaurantViewHolder.imageView.setImageDrawable(context.resources.getDrawable(R.drawable.question_mark_icon_square))
                    restaurantViewHolder.imageView.visibility = View.VISIBLE
                    restaurantViewHolder.imageLoader.visibility = View.GONE
                }
            })
        } else {
            restaurantViewHolder.imageView.setImageDrawable(context.resources.getDrawable(R.drawable.question_mark_icon_square))
            restaurantViewHolder.imageView.visibility = View.VISIBLE
            restaurantViewHolder.imageLoader.visibility = View.GONE
        }
    }

    fun notifyRestaurantsHaveChanged(){
        notifyDataSetChanged()
        loadInAllRestaurantPositions()
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
        var mapIcon = view.map_icon

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(items[adapterPosition])
            }
        }
    }

    private fun loadInAllRestaurantPositions(){
        restUuidToPosHash.clear()
        for (index in 0 until items.size){
            restUuidToPosHash.put(items[index].uuid, index)
        }
    }

    fun highlightRestaurant(restaurantUuid: String){
        if (restUuidToPosHash.get(restaurantUuid) != null){
            selectedPos = restUuidToPosHash.get(restaurantUuid)!!
            notifyItemChanged(selectedPos)
            if (previouslySelectedPosition != -1){
                notifyItemChanged(previouslySelectedPosition)
            }
            previouslySelectedPosition = selectedPos
        }
    }

    fun getPositionForUuid(restaurantUuid: String): Int{
        if (restUuidToPosHash.get(restaurantUuid) != null){
            return restUuidToPosHash.get(restaurantUuid)!!
        }
        return -1
    }

}

