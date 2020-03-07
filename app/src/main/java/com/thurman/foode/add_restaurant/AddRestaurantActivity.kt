package com.thurman.foode.add_restaurant

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.Fragment
import com.thurman.foode.models.Restaurant


class AddRestaurantActivity : FragmentActivity() {

    var tempRestaurant: Restaurant? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var bundle = Bundle()
        var editing = intent.getBooleanExtra("editing", false)
        bundle.putBoolean("editing", editing)
        var fragment: Fragment? = null
        if (editing){
            fragment = ManualEntryFragment()
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
        var fragment = ManualEntryFragment()
        var bundle = Bundle()
        bundle.putBoolean("fromSearch", true)
        fragment.arguments = bundle
        transitionFragment(fragment)
    }

}