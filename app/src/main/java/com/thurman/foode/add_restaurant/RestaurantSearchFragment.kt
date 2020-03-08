package com.thurman.foode.add_restaurant

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.thurman.foode.R
import com.thurman.foode.Utility.FirebaseUtil
import com.thurman.foode.Utility.ZomatoUtil
import com.thurman.foode.models.Restaurant
import org.json.JSONObject
import android.graphics.Bitmap
import android.content.Context
import android.graphics.BitmapFactory
import android.provider.MediaStore
import com.thurman.foode.Utility.GoogleUtil
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class RestaurantSearchFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater!!.inflate(R.layout.restaurant_search_fragment, container, false)
        var searchString = arguments!!.getString("searchText")!!
        var searchLat = arguments!!.getDouble("lat")!!
        var searchLng = arguments!!.getDouble("lon")!!
        getSearchResults(view, searchString, searchLat, searchLng)
        return view
    }

    private fun getSearchResults(view: View, searchText: String, lat: Double, lon: Double){
        val queue = Volley.newRequestQueue(context)
        var url = "https://maps.googleapis.com/maps/api/place/textsearch/json?input=" + searchText + "&inputtype=textquery&fields=formatted_address,photos,name,place_id,opening_hours,rating,geometry&location=" + lat.toString() + "," + lon.toString() + "&radius=1000&key=AIzaSyDCyMRUMFciuOhvLFdWp-FrxapIkPMY-JI"
     //   val url = "https://developers.zomato.com/api/v2.1/search?entity_id=" + searchCityEntityId + "&entity_type=city&q=" + searchText

        val req = object : JsonObjectRequest(
            Method.GET, url, null, Response.Listener { response ->
                onRestaurantsReturnedSuccess(response, view)
            }, Response.ErrorListener { error ->
                onRestaurantsReturnedError(error)
            }) {

        }
        queue.add(req)
    }

    private fun onRestaurantsReturnedSuccess(response: JSONObject, view: View){
        if (response.length() > 0){
            var restaurants = GoogleUtil.getRestaurantsFromSearchResults(response)
            setupRecyclerView(view, restaurants)
        } else {
            //TODO Empty results
        }
    }

    private fun onRestaurantsReturnedError(error: VolleyError){
        System.out.println(error.toString())
    }

    private fun setupRecyclerView(view: View, restaurants: ArrayList<Restaurant>){
        var recyclerAdapter = SearchRestaurantListAdapter(restaurants, context!!)
        var favRestaurantsList = view.findViewById<RecyclerView>(R.id.restaurants_search_list)
        favRestaurantsList.layoutManager = LinearLayoutManager(activity)
        favRestaurantsList.adapter = recyclerAdapter
        favRestaurantsList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recyclerAdapter.onItemClick = {restaurant ->
            submit(restaurant)
        }
    }

    private fun submit(restaurant: Restaurant){
        (activity as AddRestaurantActivity).SearchRestaurantChosen(restaurant)
    }

}