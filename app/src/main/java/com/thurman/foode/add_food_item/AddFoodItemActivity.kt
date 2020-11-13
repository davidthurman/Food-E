package com.thurman.foode.add_restaurant

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.thurman.foode.Utility.FireBaseKeys
import com.thurman.foode.Utility.Keys


class AddFoodItemActivity : FragmentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var fragment = AddOrEditFoodItemFragment()
        var bundle = Bundle()
        bundle.putString(Keys.restUUID, intent.getStringExtra(Keys.restUUID))
        fragment.arguments = bundle
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(android.R.id.content, fragment)
        fragmentTransaction.commit()
    }

}