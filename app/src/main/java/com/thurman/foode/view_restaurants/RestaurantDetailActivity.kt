package com.thurman.foode.view_restaurants

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.thurman.foode.Utility.FireBaseKeys
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
        var restaurantUuid = intent.getStringExtra(FireBaseKeys.restUUID)
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        var bundle = Bundle()
        bundle.putString(FireBaseKeys.restUUID, restaurantUuid)
        if (intent.hasExtra("friendId")){
            bundle.putString("friendId", intent.getStringExtra("friendId"))
        }
        restaurantDetailFragment.arguments = bundle
        fragmentTransaction.replace(android.R.id.content, restaurantDetailFragment)
        fragmentTransaction.commit()
    }

    fun editRestaurant(restaurant: Restaurant){
        restaurantToEdit = restaurant
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        var editFragment = EditingRestaurantFragment()
        var bundle = Bundle()
        bundle.putBoolean("editing", true)
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
        var editFragment = AddOrEditFoodItemFragment()
        var bundle = Bundle()
        bundle.putString(FireBaseKeys.restUUID, restaurantUuid)
        bundle.putString(FireBaseKeys.foodUUID, foodItem.uuid)
        bundle.putBoolean("editing", true)
        editFragment.arguments = bundle
        fragmentTransaction.replace(android.R.id.content, editFragment)
        fragmentTransaction.addToBackStack(restaurantDetailFragment.javaClass.name)
        fragmentTransaction.commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Check if from city search
        if (data != null){
            var citySearchResults = data!!.getStringExtra("citySearch")

        }
    }
}