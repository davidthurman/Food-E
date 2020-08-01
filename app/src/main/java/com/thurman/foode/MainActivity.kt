package com.thurman.foode

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.thurman.foode.view_restaurants.FavoritesFragment

class MainActivity : AppCompatActivity() {

    lateinit var favoritesFragment: FavoritesFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        favoritesFragment = FavoritesFragment()
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(android.R.id.content, favoritesFragment)
        fragmentTransaction.commit()
    }
}
