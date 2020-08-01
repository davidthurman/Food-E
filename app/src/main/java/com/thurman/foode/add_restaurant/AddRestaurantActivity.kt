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
import com.thurman.foode.models.Restaurant
import java.util.*


class AddRestaurantActivity : FragmentActivity() {

    var tempRestaurant: Restaurant? = null
    val AUTOCOMPLETE_REQUEST_CODE = 1
    var locationLat: Double? = null
    var locationLng: Double? = null
    var cityTextview: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var bundle = Bundle()
        var editing = intent.getBooleanExtra("editing", false)
        bundle.putBoolean("editing", editing)
        var fragment: Fragment? = null
        if (editing){
            fragment = EditingRestaurantFragment()
        } else {
            fragment = AddRestaurantFragment()
        }
        fragment.arguments = bundle
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(android.R.id.content, fragment!!)
        fragmentTransaction.commit()
    }

    fun transitionFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(android.R.id.content, fragment)
        fragmentTransaction.commit()
    }

    fun SearchRestaurantChosen(restaurant: Restaurant){
        tempRestaurant = restaurant
        var fragment = EditingRestaurantFragment()
        var bundle = Bundle()
        bundle.putBoolean("fromSearch", true)
        fragment.arguments = bundle
        transitionFragment(fragment)
    }

    fun searchAutoComplete(searchEditText: TextView){
        cityTextview = searchEditText
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.my_google_api_key), Locale.US);
        }
        var fields: List<Place.Field> = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        var intent: Intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == AutocompleteActivity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                locationLat = place.latLng!!.latitude
                locationLng = place.latLng!!.longitude
                if (cityTextview != null){
                    cityTextview!!.text = place.name.toString()
                    FirebaseUtil.changeUserLocation(place.name!!, locationLat!!, locationLng!!)
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                val status = Autocomplete.getStatusFromIntent(data!!)
            } else if (resultCode == AutocompleteActivity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

}