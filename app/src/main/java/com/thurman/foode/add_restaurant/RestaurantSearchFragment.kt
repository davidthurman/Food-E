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
import com.thurman.foode.Utility.GoogleUtil
import kotlinx.android.synthetic.main.restaurant_search_fragment.*


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
        if (response.getString("status") != "ZERO_RESULTS"){
            var restaurants = GoogleUtil.getRestaurantsFromSearchResults(response)
            setupRecyclerView(view, restaurants)
        } else {
            onNoResultsReturned()
        }
    }

    private fun onNoResultsReturned(){
        var noResultsLayout = no_results_layout
        noResultsLayout.visibility = View.VISIBLE
        var returnButton = return_button
        returnButton.setOnClickListener {
            var fragment = AddRestaurantFragment()
            (activity as AddRestaurantActivity)!!.transitionFragment(fragment)
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
        removeLoading(view, favRestaurantsList)
    }

    private fun submit(restaurant: Restaurant){
        (activity as AddRestaurantActivity).SearchRestaurantChosen(restaurant)
    }

    private fun removeLoading(view: View, recyclerView: RecyclerView){
        var loadingContainer = view.findViewById<LinearLayout>(R.id.searching_loader_container)
        loadingContainer.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

}