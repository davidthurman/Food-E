package com.thurman.foode

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.thurman.foode.add_restaurant.AddNewRestaurantTab
import com.thurman.foode.view_restaurants.FavoritesTab

class TabsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    var favoritesTab = FavoritesTab()
    var addNewRestaurantTab = AddNewRestaurantTab()

    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        when (position){
            0 -> return favoritesTab
            1 -> return addNewRestaurantTab
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

    fun updateFavorites(){
        favoritesTab.updateResults()
    }

}