package com.thurman.foode.add_restaurant

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thurman.foode.R
import com.thurman.foode.Utility.FirebaseUtil
import com.thurman.foode.models.City
import com.thurman.foode.models.Location
import kotlinx.android.synthetic.main.add_new_restaurant_tab.*
import java.util.*

class AddRestaurantFragment : Fragment() {

    lateinit var restaurantSearchBar: TextInputEditText
    lateinit var currentView: View
    var searchLocation: Location? = null
    lateinit var thisActivity: AddRestaurantActivity
    lateinit var cityTextview: TextView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        currentView = inflater!!.inflate(R.layout.add_new_restaurant_tab, container, false)
        thisActivity = activity!! as AddRestaurantActivity
        cityTextview = currentView.findViewById(R.id.city_title)
        setupSearchBar(currentView)
        //setupCity(currentView)
        setupButtons(currentView)
        return currentView
    }

    override fun onResume() {
        super.onResume()
        setupCity(currentView)
    }

    private fun setupSearchBar(view: View){
        restaurantSearchBar = view.findViewById(R.id.restaurant_search_bar)
        restaurantSearchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                restaurantSearchBar.error = null
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
    }

    private fun setupCity(view: View){
        val cityListener = object : ValueEventListener {
            override fun onDataChange(citySnapshot: DataSnapshot) {
                var location = FirebaseUtil.getLocationFromSnapshot(citySnapshot)
                cityTextview.text = location.addressName
                searchLocation = location
                thisActivity.locationLng = location.lng
                thisActivity.locationLat = location.lat
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        val userID = FirebaseAuth.getInstance().getCurrentUser()!!.uid
        FirebaseDatabase.getInstance().reference.child("users").child(userID).child("city").addListenerForSingleValueEvent(cityListener)
    }

    private fun setupButtons(view: View){
        var manualEntryButton = view.findViewById<Button>(R.id.manual_entry_btn)
        manualEntryButton?.setOnClickListener { transitionScreen("manual") }

        var searchButton = view.findViewById<Button>(R.id.search_btn)
        searchButton.setOnClickListener{
            if (thisActivity.locationLat != null && thisActivity.locationLng != null){
                if (restaurantSearchBar.text!!.count() > 0){
                    transitionScreen("search")
                } else {
                    restaurantSearchBar.error = "Please enter a restaurant name"
                }
            } else {
                //TODO Handle no location chosen
            }
        }
        setupChangeCityButton(view)
    }

    private fun setupChangeCityButton(view: View){
        var changeCityButton = view.findViewById<Button>(R.id.change_city_btn)
        changeCityButton.setOnClickListener(View.OnClickListener {
            //val intent = Intent(activity, SearchCityActivity::class.java)
            //startActivityForResult(intent, 200)
            thisActivity.searchAutoComplete(cityTextview)
        })
    }

    private fun transitionScreen(type: String){
        var fragment: Fragment? = null
        if (type == "manual"){
            fragment = ManualEntryFragment()
        } else {
            fragment = RestaurantSearchFragment()
            var bundle = Bundle()
            bundle.putString("searchText", restaurantSearchBar.text.toString())
            bundle.putDouble("lat", thisActivity.locationLat!!)
            bundle.putDouble("lon", thisActivity.locationLng!!)
            fragment.arguments = bundle
        }
        (activity as AddRestaurantActivity).transitionFragment(fragment)
    }
}