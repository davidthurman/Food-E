package com.thurman.foode.Utility

import android.content.Context
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

        fun getLocationFromCity(){
            //TODO
        }

        fun getRestaurantsFromSearchResults(response: JSONObject): ArrayList<Restaurant>{
            var restaurants = ArrayList<Restaurant>()
            var restaurantsJson = response.getJSONArray("candidates")
            for (index in 0 until restaurantsJson.length()){
                var restaurant = getRestaurantFromJson(restaurantsJson.getJSONObject(index))
                restaurants.add(restaurant)
            }
            return restaurants
        }

        private fun getRestaurantFromJson(restJson: JSONObject): Restaurant{
            var restName = restJson.getString("name")
            var address = restJson.getString("formatted_address")
            //var thumbnail = restJson.get("thumb")
            var restaurant = Restaurant(restName, address, 0, "")
//            if (thumbnail != null && thumbnail is String && !thumbnail.equals("")) {
//                restaurant.imageUri = Uri.parse(thumbnail)
//            }
            var photosArray = restJson.getJSONArray("photos")
            if (photosArray != null && photosArray.length() > 0){
                var photoJson = photosArray.getJSONObject(0)
                restaurant.googlePhotoReference = photoJson.getString("photo_reference")
            }
            var geometryJson = restJson.getJSONObject("geometry")
            var locationJson = geometryJson.getJSONObject("location")
            restaurant.lat = locationJson.getDouble("lat")
            restaurant.lng = locationJson.getDouble("lng")
            restaurant.googleId = restJson.getString("place_id")
            restaurant.googleRating = restJson.getDouble("rating")
            return restaurant
        }

        fun getPhotoUrlFromReference(context: Context, photoReference: String, imageView: ImageView, imageLoader: MKLoader){
            val queue = Volley.newRequestQueue(context)
            var url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=AIzaSyDCyMRUMFciuOhvLFdWp-FrxapIkPMY-JI"

            val imageLoader: ImageLoader by lazy {
                ImageLoader(queue,
                    object : ImageLoader.ImageCache {
                        private val cache = LruCache<String, Bitmap>(20)
                        override fun getBitmap(url: String): Bitmap {
                            return cache.get(url)
                        }

                        override fun putBitmap(url: String, bitmap: Bitmap) {
                            cache.put(url, bitmap)
                            imageView.setImageBitmap(bitmap)
                            imageView.visibility = View.VISIBLE
                            imageLoader.visibility = View.GONE
                        }
                    })
            }


//            val req = object : ImageRequest(
//                Method.GET, url, null, Response.Listener { response ->
//                    onPhotoUrlReturnSuccess(response, imageView, imageLoader)
//                }, Response.ErrorListener { error ->
//                    onPhotoUrlReturnError(error)
//                }) {
//            }
//            queue.add(req)
        }

        private fun onPhotoUrlReturnSuccess(response: JSONObject, imageView: ImageView, imageLoader: MKLoader){

        }

        private fun onPhotoUrlReturnError(error: VolleyError){

        }

        fun getCitiesFromSearchResults(response: JSONObject): ArrayList<City>{
            var cities = ArrayList<City>()
            var citiesJson = response.getJSONArray("location_suggestions")
            for (index in 0 until citiesJson.length()){
                var cityJson = citiesJson.getJSONObject(index)
                var city = getCityFromJson(cityJson)
                cities.add(city)
            }
            return cities
        }

        private fun getCityFromJson(cityJson: JSONObject): City{
            var cityTitle = cityJson.getString("title")
            var cityCountry = cityJson.getString("country_name")
            var cityId = cityJson.getString("entity_id")
            var city = City(cityTitle, cityCountry, cityId)
            return city
        }

    }


}