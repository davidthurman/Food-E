package com.thurman.foode.add_restaurant

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.Fragment
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.thurman.foode.R
import com.thurman.foode.Utility.FirebaseUtil
import com.thurman.foode.Utility.Keys
import com.thurman.foode.models.Restaurant
import java.util.*

const val AUTOCOMPLETE_REQUEST_CODE = 1

class AddRestaurantActivity : FragmentActivity() {

    var tempRestaurant: Restaurant? = null
    var locationLat: Double = 0.0
    var locationLng: Double = 0.0
    var cityTextview: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = Bundle()
        val editing = intent.getBooleanExtra(Keys.editingFlag, false)
        bundle.putBoolean(Keys.editingFlag, editing)
        val fragment = if (editing){
            EditingRestaurantFragment()
        } else {
            AddRestaurantFragment()
        }
        fragment.arguments = bundle
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(android.R.id.content, fragment)
        fragmentTransaction.commit()
    }

    fun transitionFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(android.R.id.content, fragment)
        fragmentTransaction.commit()
    }

    fun searchRestaurantChosen(restaurant: Restaurant){
        tempRestaurant = restaurant
        val fragment = EditingRestaurantFragment()
        val bundle = Bundle()
        bundle.putBoolean(Keys.fromSearchFlag, true)
        fragment.arguments = bundle
        transitionFragment(fragment)
    }

    fun searchAutoComplete(searchEditText: TextView){
        cityTextview = searchEditText
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.my_google_api_key), Locale.US);
        }
        val fields: List<Place.Field> = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val intent: Intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == AutocompleteActivity.RESULT_OK) {
                data?.let{
                    val place = Autocomplete.getPlaceFromIntent(it)
                    locationLat = place.latLng?.latitude ?: 0.0
                    locationLng = place.latLng?.longitude ?: 0.0
                    cityTextview?.let {
                        it.text = place.name.toString()
                        FirebaseUtil.changeUserLocation(place.name ?: "", locationLat, locationLng)
                    }
                }
            }
        }
    }

}