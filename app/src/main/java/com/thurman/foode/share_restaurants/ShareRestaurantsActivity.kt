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
import com.google.android.material.textfield.TextInputEditText
import com.thurman.foode.R
import com.thurman.foode.Utility.FirebaseUtil
import com.thurman.foode.models.Restaurant
import java.util.*


class ShareRestaurantsActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fragmentManager = supportFragmentManager
        var bundle = Bundle()
        if (intent.hasExtra("restaurantUuid")){
            var restaurantUuid = intent.getStringExtra("restaurantUuid")
            bundle.putString("restaurantUuid", restaurantUuid)
        }
        var fragment = ShareRestaurantsFragment()
        fragment.arguments = bundle
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(android.R.id.content, fragment)
        fragmentTransaction.commit()
    }

}