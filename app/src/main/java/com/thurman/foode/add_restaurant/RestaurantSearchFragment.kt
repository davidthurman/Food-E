package com.thurman.foode.add_restaurant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.thurman.foode.R
import com.thurman.foode.models.Restaurant
import org.json.JSONObject
import android.widget.LinearLayout
import com.thurman.foode.Utility.GoogleKeys
import com.thurman.foode.Utility.GoogleUtil
import com.thurman.foode.Utility.Keys
import kotlinx.android.synthetic.main.restaurant_search_fragment.*


class RestaurantSearchFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.restaurant_search_fragment, container, false)
        val searchString = arguments!!.getString(Keys.searchText)!!
        val searchLat = arguments!!.getDouble(Keys.latId)
        val searchLng = arguments!!.getDouble(Keys.lngId)
        getSearchResults(view, searchString, searchLat, searchLng)
        return view
    }

    private fun getSearchResults(view: View, searchText: String, lat: Double, lon: Double){
        val queue = Volley.newRequestQueue(context)
        val url = "https://maps.googleapis.com/maps/api/place/textsearch/json?input=" + searchText + "&inputtype=textquery&fields=formatted_address,photos,name,place_id,opening_hours,rating,geometry&location=" + lat.toString() + "," + lon.toString() + "&radius=1000&key=AIzaSyDCyMRUMFciuOhvLFdWp-FrxapIkPMY-JI"
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
        if (response.getString(GoogleKeys.googleMapsStatus) != GoogleKeys.googleMapsZeroResults){
            val restaurants = GoogleUtil.getRestaurantsFromSearchResults(response)
            setupRecyclerView(view, restaurants)
        } else {
            onNoResultsReturned()
        }
    }

    private fun onNoResultsReturned(){
        val noResultsLayout = no_results_layout
        noResultsLayout.visibility = View.VISIBLE
        val returnButton = return_button
        returnButton.setOnClickListener {
            val fragment = AddRestaurantFragment()
            (activity as AddRestaurantActivity).transitionFragment(fragment)
        }
    }

    private fun onRestaurantsReturnedError(error: VolleyError){
        println(error.toString())
    }

    private fun setupRecyclerView(view: View, restaurants: ArrayList<Restaurant>){
        val recyclerAdapter = SearchRestaurantListAdapter(restaurants, context!!)
        val favRestaurantsList = view.findViewById<RecyclerView>(R.id.restaurants_search_list)
        favRestaurantsList.layoutManager = LinearLayoutManager(activity)
        favRestaurantsList.adapter = recyclerAdapter
        favRestaurantsList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recyclerAdapter.onItemClick = {restaurant ->
            submit(restaurant)
        }
        removeLoading(view, favRestaurantsList)
    }

    private fun submit(restaurant: Restaurant){
        (activity as AddRestaurantActivity).searchRestaurantChosen(restaurant)
    }

    private fun removeLoading(view: View, recyclerView: RecyclerView){
        val loadingContainer = view.findViewById<LinearLayout>(R.id.searching_loader_container)
        loadingContainer.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

}