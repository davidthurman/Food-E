package com.thurman.foode.view_restaurants

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.thurman.foode.models.Restaurant


class RestaurantDetailActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var restaurantUuid = intent.getStringExtra("restaurantUuid")
        var fragment = RestaurantDetailFragment()
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        var bundle = Bundle()
        bundle.putString("restaurantUuid", restaurantUuid)
        fragment.arguments = bundle
        fragmentTransaction.replace(android.R.id.content, fragment)
        fragmentTransaction.commit()
    }

}