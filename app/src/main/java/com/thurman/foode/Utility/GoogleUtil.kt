package com.thurman.foode.Utility

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.LruCache
import android.view.View
import android.widget.ImageView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.thurman.foode.models.City
import com.thurman.foode.models.Restaurant
import com.tuyenmonkey.mkloader.MKLoader
import org.json.JSONObject

class GoogleUtil {

    companion object {

        fun getRestaurantsFromSearchResults(response: JSONObject): ArrayList<Restaurant>{
            val restaurants = ArrayList<Restaurant>()
            val restaurantsJson = response.getJSONArray(GoogleKeys.results)
            for (index in 0 until restaurantsJson.length()){
                val restaurant = getRestaurantFromJson(restaurantsJson.getJSONObject(index))
                restaurants.add(restaurant)
            }
            return restaurants
        }

        private fun getRestaurantFromJson(restJson: JSONObject): Restaurant{
            val restName = restJson.getString(GoogleKeys.name)
            val address = restJson.getString(GoogleKeys.formattedAddress)
            val restaurant = Restaurant(restName, address, 0, "")
            val photosArray = restJson.optJSONArray(GoogleKeys.photos)
            if (photosArray != null && photosArray.length() > 0){
                val photoJson = photosArray.getJSONObject(0)
                restaurant.googlePhotoReference = photoJson.getString(GoogleKeys.photoReference)
            }

            val geometryJson = restJson.getJSONObject(GoogleKeys.geometry)
            val locationJson = geometryJson.getJSONObject(GoogleKeys.location)
            restaurant.lat = locationJson.getDouble(GoogleKeys.lat)
            restaurant.lng = locationJson.getDouble(GoogleKeys.lng)
            restaurant.googleId = restJson.getString(GoogleKeys.placeId)
            restaurant.googleRating = restJson.getDouble(GoogleKeys.rating)
            return restaurant
        }

        fun openGoogleMaps(restaurant: Restaurant, context: Context){
            val gmmIntentUri =
                Uri.parse("https://www.google.com/maps/search/?api=1&query=" + restaurant.name + "&query_place_id=" + restaurant.googleId)
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage(GoogleKeys.googleMapsPackageName)
            context.startActivity(mapIntent)
        }

    }


}