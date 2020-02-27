package com.thurman.foode

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.thurman.foode.add_restaurant.AddNewRestaurantTab
import com.thurman.foode.view_restaurants.FavoritesTab

class TabsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        when (position){
            0 -> return FavoritesTab()
            1 -> return AddNewRestaurantTab()
            else -> {
                return FavoritesTab()
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Favorites"
            1 -> "Add New Restaurant\""
            else -> {
                return "Add New Restaurant"
            }
        }
    }

}