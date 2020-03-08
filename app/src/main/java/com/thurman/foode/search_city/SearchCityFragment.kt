package com.thurman.foode.add_restaurant

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.libraries.places.api.Places
import com.google.android.material.textfield.TextInputEditText
import com.thurman.foode.R
import com.thurman.foode.Utility.FirebaseUtil
import com.thurman.foode.Utility.ZomatoUtil
import com.thurman.foode.models.City
import com.thurman.foode.models.Restaurant
import com.thurman.foode.view_restaurants.SearchCityListAdapter
import org.json.JSONObject

class SearchCityFragment : Fragment() {

    lateinit var citySearchBar: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater!!.inflate(R.layout.search_city_fragment, container, false)
        setupSearchBar(view)
        setupButtons(view)
        return view
    }

    private fun setupSearchBar(view: View){
        citySearchBar = view.findViewById(R.id.city_search_bar)
        citySearchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                citySearchBar.error = null
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
    }

    private fun setupButtons(view: View){
        var searchButton = view.findViewById<Button>(R.id.search_btn)
        searchButton.setOnClickListener{
            if (citySearchBar.text!!.count() > 0){
                search(view, citySearchBar.text!!.toString())
            } else {
                citySearchBar.error = "Please enter a city"
            }
        }
    }

    private fun search(view: View, searchText: String){
        val queue = Volley.newRequestQueue(context)
        val url = "https://developers.zomato.com/api/v2.1/locations?query=" + searchText

        val req = object : JsonObjectRequest(
            Method.GET, url, null, Response.Listener { response ->
                onCitySearchReturnedSuccess(response, view)
            }, Response.ErrorListener { error ->
                onCitySearchReturnedError(error)
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

    private fun onCitySearchReturnedSuccess(response: JSONObject, view: View){
        if (response.length() > 0){
            var cities = ZomatoUtil.getCitiesFromSearchResults(response)
            setupRecyclerView(view, cities)
        } else {
            //TODO Empty results
        }
    }

    private fun onCitySearchReturnedError(error: VolleyError){
        System.out.println(error.toString())
    }

    private fun setupRecyclerView(view: View, cities: ArrayList<City>){
        var recyclerAdapter = SearchCityListAdapter(cities, context!!)
        var citiesSearchList = view.findViewById<RecyclerView>(R.id.cities_search_list)
        citiesSearchList.layoutManager = LinearLayoutManager(activity)
        citiesSearchList.adapter = recyclerAdapter
        citiesSearchList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recyclerAdapter.onItemClick = {city ->
            cityChosen(city)
        }
    }

    private fun cityChosen(city: City){
        FirebaseUtil.changeUserCity(city, activity!!)
    }

}