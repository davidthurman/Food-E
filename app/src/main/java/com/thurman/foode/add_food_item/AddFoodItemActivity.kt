package com.thurman.foode.add_restaurant

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.thurman.foode.Utility.FireBaseKeys


class AddFoodItemActivity : FragmentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var fragment = AddOrEditFoodItemFragment()
        var bundle = Bundle()
        bundle.putString(FireBaseKeys.restUUID, intent.getStringExtra(FireBaseKeys.restUUID))
        fragment.arguments = bundle
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(android.R.id.content, fragment)
        fragmentTransaction.commit()
    }

}