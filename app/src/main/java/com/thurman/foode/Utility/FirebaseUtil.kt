package com.thurman.foode.Utility

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.thurman.foode.R
import com.thurman.foode.add_restaurant.AddFoodItemFragment
import com.thurman.foode.models.FoodItem
import com.thurman.foode.models.Restaurant
import com.thurman.foode.view_restaurants.FavoriteRestaurantListAdapter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
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

        fun getRestaurantDetailImage(restaurant: Restaurant, imageView: ImageView, context: Context){
            val userID = FirebaseAuth.getInstance().getCurrentUser()!!.uid
            var storageReference = FirebaseStorage.getInstance().reference

            storageReference.child("images/users/" + userID + "/" + restaurant.uuid + ".jpg").downloadUrl.addOnSuccessListener  {
                    uri ->
                run {
                    restaurant.imageUri = uri
                    Picasso.with(context).load(restaurant.imageUri).into(imageView)
                }
            }.addOnFailureListener{ exception -> imageView.setImageDrawable(context.resources.getDrawable(R.drawable.question_mark_icon)) }
        }

        fun submitRestaurant(name: String, address: String, rating: Int, restaurantUri: Uri?, searchImage: Boolean, activity: Activity){
            val userID = FirebaseAuth.getInstance().getCurrentUser()!!.uid
            val ref = FirebaseDatabase.getInstance().getReference("users").child(userID).child("restaurants")
            val resKeyId = ref.push().key
            var restaurant = Restaurant(name, address, rating, resKeyId!!)
            if (resKeyId != null){
                ref.child(resKeyId).setValue(restaurant).addOnCompleteListener{
                    if (restaurantUri != null){
                        uploadRestaurantImage(userID, searchImage, resKeyId, restaurantUri, activity)
                    } else {
                        activity?.finish()
                    }
                }
            }
        }

        private fun uploadRestaurantImage(userID: String, searchImage: Boolean, resKeyId: String, restaurantUri: Uri, activity: Activity){
            var restUri = restaurantUri
            var storageReference = FirebaseStorage.getInstance().reference.child("images/users/" + userID + "/" + resKeyId + ".jpg")
            if (searchImage){
                var bitmap = getBitmapFromURL(restUri.toString())
                if (bitmap != null) {
                    restUri = getImageUri(activity.applicationContext!!, bitmap!!)
                }
            }
            storageReference.putFile(restUri).addOnSuccessListener {
                deleteUploadedImage(restUri)
                activity?.finish()
            }.addOnFailureListener{
                //TODO Handle image upload fail
                deleteUploadedImage(restUri)
                activity?.finish()
            }
        }

        fun getBitmapFromURL(src: String): Bitmap? {
            try {
                var url = URL(src)
                var connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                var input = connection.inputStream
                var myBitmap = BitmapFactory.decodeStream(input)
                return myBitmap
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }

        fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
            val bytes = ByteArrayOutputStream()
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "tempFoodEImage", null)
            return Uri.parse(path)
        }

        fun deleteUploadedImage(uri: Uri){
            var file = File(uri.path)
            var exists = file.exists()
            var deleted = file.delete()
            System.out.println("DELETED: " + deleted)
        }

        fun submitFoodItemToRestaurant(restaurantUuid: String, name: String, rating: Int, foodUri: Uri?, activity: Activity, fragment: AddFoodItemFragment){
            val userID = FirebaseAuth.getInstance().getCurrentUser()!!.uid
            val ref = FirebaseDatabase.getInstance().getReference("users").child(userID).child("restaurants").child(restaurantUuid).child("foodItems")
            val foodKeyId = ref.push().key
            if (foodKeyId != null){
                val foodItem = FoodItem(name, rating, foodKeyId)
                ref.child(foodKeyId).setValue(foodItem).addOnCompleteListener{
                    if (foodUri != null){
                        uploadFoodItemImage(userID, restaurantUuid, foodKeyId, foodUri, activity)
                        fragment.setLoading(false)
                    } else {
                        fragment.setLoading(false)
                        activity?.finish()
                    }
                }
            }
        }

        fun setFoodItemImage(restaurant: Restaurant, foodItem: FoodItem, imageView: ImageView, context: Context){
            val userID = FirebaseAuth.getInstance().getCurrentUser()!!.uid
            var storageReference = FirebaseStorage.getInstance().reference

            storageReference.child("images/users/" + userID + "/" + restaurant.uuid + "/" + foodItem.uuid +  ".jpg").downloadUrl.addOnSuccessListener  {
                    uri ->
                run {
                    foodItem.imageUri = uri
                    Picasso.with(context).load(foodItem.imageUri).into(imageView)
                }
            }.addOnFailureListener{ exception -> imageView.setImageDrawable(context.resources.getDrawable(R.drawable.question_mark_icon)) }
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