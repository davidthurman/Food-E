package com.thurman.foode

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.thurman.foode.Utility.Keys
import com.thurman.foode.view_restaurants.FavoritesFragment

class MainActivity : AppCompatActivity() {

    lateinit var favoritesFragment: FavoritesFragment
    var friendId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.hasExtra(Keys.friendId)){
            friendId = intent.getStringExtra(Keys.friendId)
        }
        favoritesFragment = FavoritesFragment()
        if (friendId != null){
            var bundle = Bundle()
            bundle.putString(Keys.friendId, friendId)
            favoritesFragment.arguments = bundle
        }
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(android.R.id.content, favoritesFragment)
        fragmentTransaction.commit()
    }

    //If user is viewing a shared link and want to return to their home screen
    fun onHomeClicked(){
        favoritesFragment = FavoritesFragment()
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(android.R.id.content, favoritesFragment)
        fragmentTransaction.commit()
    }
}
