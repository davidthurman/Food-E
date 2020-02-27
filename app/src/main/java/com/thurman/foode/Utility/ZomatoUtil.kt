package com.thurman.foode.Utility

import android.content.Context
import android.net.Uri
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.thurman.foode.models.Restaurant
import org.json.JSONObject

class ZomatoUtil {

    companion object {

        fun getLocationFromCity(){
            //TODO
        }

        fun getRestaurantsFromSearchResults(response: JSONObject): ArrayList<Restaurant>{
            var restaurants = ArrayList<Restaurant>()
            var restaurantsJson = response.getJSONArray("restaurants")
            for (index in 0 until restaurantsJson.length()){
                var restaurantJson = restaurantsJson.getJSONObject(index).getJSONObject("restaurant")
                var restaurant = getRestaurantFromJson(restaurantJson)
                restaurants.add(restaurant)
            }
            return restaurants
        }

        private fun getRestaurantFromJson(restJson: JSONObject): Restaurant{
            var restName = restJson.getString("name")
            var locationData = restJson.getJSONObject("location")
            var address = locationData.getString("address")
            var thumbnail = restJson.get("thumb")
            var restaurant = Restaurant(restName, address, 0, "")
            if (thumbnail != null && thumbnail is String && !thumbnail.equals("")) {
                restaurant.imageUri = Uri.parse(thumbnail)
            }
            return restaurant
        }

    }


}