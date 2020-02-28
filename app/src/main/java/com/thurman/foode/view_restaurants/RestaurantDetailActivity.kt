package com.thurman.foode.view_restaurants

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.thurman.foode.models.Restaurant


class RestaurantDetailActivity : FragmentActivity() {

    var restaurantDetailFragment = RestaurantDetailFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var restaurantUuid = intent.getStringExtra("restaurantUuid")
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        var bundle = Bundle()
        bundle.putString("restaurantUuid", restaurantUuid)
        restaurantDetailFragment.arguments = bundle
        fragmentTransaction.replace(android.R.id.content, restaurantDetailFragment)
        fragmentTransaction.commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        restaurantDetailFragment.refreshData()
    }
}