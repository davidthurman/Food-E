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
        getSearchResults(view, searchString)
        return view
    }

    private fun getSearchResults(view: View, searchText: String){
        val queue = Volley.newRequestQueue(context)
        val url = "https://developers.zomato.com/api/v2.1/search?entity_id=288&q=" + searchText

        val req = object : JsonObjectRequest(
            Method.GET, url, null, Response.Listener { response ->
                onRestaurantsReturnedSuccess(response, view)
            }, Response.ErrorListener { error ->
                onRestaurantsReturnedError(error)
            }) {

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["user-key"] = "1b72cd17cc73ea8fee9ef1b1ca33d3a0"
                return headers
            }
        }
        queue.add(req)
    }

    private fun onRestaurantsReturnedSuccess(response: JSONObject, view: View){
        if (response.length() > 0){
            var restaurants = ZomatoUtil.getRestaurantsFromSearchResults(response)
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
//        FirebaseUtil.submitRestaurant(restaurant.name,
//                                      restaurant.address,
//                               5,
//                                      restaurant.imageUri,
//                                      true,
//                                      activity!!)
    }

}