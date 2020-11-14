package com.thurman.foode.view_restaurants

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.thurman.foode.Utility.FireBaseKeys
import com.thurman.foode.Utility.Keys
import com.thurman.foode.add_restaurant.AddFoodItemActivity
import com.thurman.foode.add_restaurant.AddOrEditFoodItemFragment
import com.thurman.foode.add_restaurant.EditingRestaurantFragment
import com.thurman.foode.models.FoodItem
import com.thurman.foode.models.Restaurant


class RestaurantDetailActivity : FragmentActivity() {

    var restaurantDetailFragment = RestaurantDetailFragment()
    var restaurantToEdit: Restaurant? = null
    var foodItemToEdit: FoodItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val restaurantUuid = intent.getStringExtra(FireBaseKeys.restUUID)
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putString(FireBaseKeys.restUUID, restaurantUuid)
        if (intent.hasExtra(Keys.friendId)){
            bundle.putString(Keys.friendId, intent.getStringExtra(Keys.friendId))
        }
        restaurantDetailFragment.arguments = bundle
        fragmentTransaction.replace(android.R.id.content, restaurantDetailFragment)
        fragmentTransaction.commit()
    }

    fun editRestaurant(restaurant: Restaurant){
        restaurantToEdit = restaurant
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val editFragment = EditingRestaurantFragment()
        val bundle = Bundle()
        bundle.putBoolean(Keys.editingFlag, true)
        bundle.putString(FireBaseKeys.restUUID, restaurant.uuid)
        editFragment.arguments = bundle
        fragmentTransaction.replace(android.R.id.content, editFragment)
        fragmentTransaction.commit()
    }

    fun onEditFinished(){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(android.R.id.content, restaurantDetailFragment)
        fragmentTransaction.commit()
    }

    fun editFoodItem(foodItem: FoodItem, restaurantUuid: String){
        foodItemToEdit = foodItem
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val editFragment = AddOrEditFoodItemFragment()
        val bundle = Bundle()
        bundle.putString(Keys.restUUID, restaurantUuid)
        bundle.putString(Keys.foodUUID, foodItem.uuid)
        bundle.putBoolean(Keys.editingFlag, true)
        editFragment.arguments = bundle
        fragmentTransaction.replace(android.R.id.content, editFragment)
        fragmentTransaction.addToBackStack(restaurantDetailFragment.javaClass.name)
        fragmentTransaction.commit()
    }

}