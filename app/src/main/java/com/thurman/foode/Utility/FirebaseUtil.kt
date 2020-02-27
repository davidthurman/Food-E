package com.thurman.foode.Utility

import android.app.Activity
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.thurman.foode.models.FoodItem
import com.thurman.foode.models.Restaurant
import com.thurman.foode.view_restaurants.FavoriteRestaurantListAdapter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FirebaseUtil {

    companion object {

        fun getRestaurantFromSnapshot(restaurantSnapshot: DataSnapshot): Restaurant{
            var restName = restaurantSnapshot.child("name").getValue().toString()
            var restAddress = restaurantSnapshot.child("address").getValue().toString()
            var restRating = (restaurantSnapshot.child("rating").getValue() as Long).toInt()
            var restUuid = restaurantSnapshot.child("uuid").getValue().toString()
            var foodItemsSnapshot = restaurantSnapshot.child("foodItems").getValue()
            var restaurant = Restaurant(restName, restAddress, restRating, restUuid)
            if (foodItemsSnapshot != null){
                restaurant.foodItems = getFoodItemsFromSnapshot(foodItemsSnapshot)
            }
            return restaurant
        }

        fun getFoodItemsFromSnapshot(foodItemsSnapshot: Any?): ArrayList<FoodItem>{
            //TODO This is janky
            var foodItemList = ArrayList<FoodItem>()
            var foodItemsListSnapshot = foodItemsSnapshot as HashMap<String, Object>
            for (foodItem in foodItemsListSnapshot){
                var foodItemMap = foodItem as Map.Entry<String, Object>
                var foodItemHash =foodItemMap.value as HashMap<String, Object>
                var foodItemName = foodItemHash.get("name") as String
                var foodItemRating = (foodItemHash.get("rating") as Long).toInt()
                var foodItemUuid = foodItemHash.get("uuid") as String
                var foodItemObject = FoodItem(foodItemName, foodItemRating, foodItemUuid)
                foodItemList.add(foodItemObject)
            }
            return foodItemList
        }

        fun getRestaurantImage(restaurant: Restaurant, recyclerAdapter: FavoriteRestaurantListAdapter){
            val userID = FirebaseAuth.getInstance().getCurrentUser()!!.uid
            var storageReference = FirebaseStorage.getInstance().reference

            storageReference.child("images/users/" + userID + "/" + restaurant.uuid + ".jpg").downloadUrl.addOnSuccessListener  {
                    uri ->
                run {
                    restaurant.imageUri = uri
                    recyclerAdapter.notifyDataSetChanged()
                }
            }.addOnFailureListener{ exception -> System.out.println("DOWNLOAD FAIL: " + exception.localizedMessage) }
        }

        fun submitRestaurant(name: String, address: String, rating: Int, restaurantUri: Uri?, activity: Activity){
            val userID = FirebaseAuth.getInstance().getCurrentUser()!!.uid
            val ref = FirebaseDatabase.getInstance().getReference("users").child(userID).child("restaurants")
            val resKeyId = ref.push().key
            var restaurant = Restaurant(name, address, rating, resKeyId!!)
            if (resKeyId != null){
                ref.child(resKeyId).setValue(restaurant).addOnCompleteListener{
                    if (restaurantUri != null){
                        uploadRestaurantImage(userID, resKeyId, restaurantUri, activity)
                    } else {
                        activity?.finish()
                    }
                }
            }
        }

        private fun uploadRestaurantImage(userID: String, resKeyId: String, restaurantUri: Uri, activity: Activity){
            var storageReference = FirebaseStorage.getInstance().reference.child("images/users/" + userID + "/" + resKeyId + ".jpg")
            storageReference.putFile(restaurantUri).addOnSuccessListener {
                activity?.finish()
            }.addOnFailureListener{
                //TODO Handle image upload fail
                activity?.finish()
            }
        }

        fun submitFoodItemToRestaurant(restaurantUuid: String, name: String, rating: Int, foodUri: Uri?, activity: Activity){
            val userID = FirebaseAuth.getInstance().getCurrentUser()!!.uid
            val ref = FirebaseDatabase.getInstance().getReference("users").child(userID).child("restaurants").child(restaurantUuid).child("foodItems")
            val foodKeyId = ref.push().key
            if (foodKeyId != null){
                val foodItem = FoodItem(name, rating, foodKeyId)
                ref.child(foodKeyId).setValue(foodItem).addOnCompleteListener{
                    if (foodUri != null){
                        uploadFoodItemImage(userID, foodKeyId, restaurantUuid, foodUri, activity)
                    } else {
                        activity?.finish()
                    }
                }
            }
        }

        private fun uploadFoodItemImage(userID: String, restaurantUuid: String, foodKeyId: String, restaurantUri: Uri, activity: Activity){
            var storageReference = FirebaseStorage.getInstance().reference.child("images/users/" + userID + "/" + restaurantUuid + "/" + foodKeyId + ".jpg")
            storageReference.putFile(restaurantUri).addOnSuccessListener {
                activity?.finish()
            }.addOnFailureListener{
                //TODO Handle image upload fail
                activity?.finish()
            }
        }

    }


}