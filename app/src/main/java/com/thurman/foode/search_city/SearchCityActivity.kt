package com.thurman.foode.add_restaurant

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.Fragment
import com.thurman.foode.models.Restaurant


class SearchCityActivity : FragmentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var fragment = SearchCityFragment()
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(android.R.id.content, fragment!!)
        fragmentTransaction.commit()
    }

}