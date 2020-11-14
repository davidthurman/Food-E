package com.thurman.foode.Utility

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.thurman.foode.R
import com.thurman.foode.add_restaurant.AddOrEditFoodItemFragment
import com.thurman.foode.models.FoodItem
import com.thurman.foode.models.Location
import com.thurman.foode.models.Restaurant
import com.thurman.foode.view_restaurants.FavoriteRestaurantListAdapter
import com.thurman.foode.view_restaurants.RestaurantDetailActivity
import com.tuyenmonkey.mkloader.MKLoader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FirebaseUtil {

    companion object {

        //val userID = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        fun getRestaurantFromSnapshot(restaurantSnapshot: DataSnapshot): Restaurant{
            val restName = restaurantSnapshot.child(FireBaseKeys.restName).value.toString()
            println(restaurantSnapshot.child(FireBaseKeys.restUUID).value.toString())
            val restAddress = restaurantSnapshot.child(FireBaseKeys.restAddress).value.toString()
            val restRating = (restaurantSnapshot.child(FireBaseKeys.restRating).value as Long).toInt()
            val restUuid = restaurantSnapshot.child(FireBaseKeys.restUUID).value.toString()
            val foodItemsSnapshot = restaurantSnapshot.child(FireBaseKeys.restFoodItems).value
            val restaurant = Restaurant(restName, restAddress, restRating, restUuid)
            if (foodItemsSnapshot != null){
                println(restUuid)
                restaurant.foodItems = getFoodItemsFromSnapshot(foodItemsSnapshot)
            }
            if (restaurantSnapshot.child(FireBaseKeys.restGooglePhotoReference).value != null){
                restaurant.googlePhotoReference = restaurantSnapshot.child(FireBaseKeys.restGooglePhotoReference).value.toString()
            }
            if (restaurantSnapshot.child(Keys.latId).value != null){
                restaurant.lat = restaurantSnapshot.child(Keys.latId).value as Double
                restaurant.lng = restaurantSnapshot.child(Keys.lngId).value as Double
            }
            if (restaurantSnapshot.child(FireBaseKeys.restComments).value != null){
                restaurant.comments = restaurantSnapshot.child(FireBaseKeys.restComments).value.toString()
            }
            if (restaurantSnapshot.child(FireBaseKeys.restGoogleRating).value != null){
                val googleRating: Double? = restaurantSnapshot.child(FireBaseKeys.restGoogleRating).value as? Double
                if (googleRating != null){
                    restaurant.googleRating = googleRating
                }
            }
            return restaurant
        }

        private fun getFoodItemsFromSnapshot(foodItemsSnapshot: Any?): ArrayList<FoodItem>{
            //TODO This is janky
            val foodItemList = ArrayList<FoodItem>()
            val foodItemsListSnapshot = foodItemsSnapshot as HashMap<String, Object>
            for (foodItem in foodItemsListSnapshot){
                val foodItemMap = foodItem as Map.Entry<String, Object>
                val foodItemHash =foodItemMap.value as HashMap<String, Object>
                val foodItemName = foodItemHash[FireBaseKeys.foodName] as String
                val foodItemRating = (foodItemHash[FireBaseKeys.foodRating] as Long).toInt()
                val foodItemUuid = foodItemHash[FireBaseKeys.foodUUID] as String
                val foodItemObject = FoodItem(foodItemName, foodItemRating, foodItemUuid)
                if (foodItemHash[FireBaseKeys.foodComments] != null){
                    val foodItemComments = foodItemHash[FireBaseKeys.foodComments] as String
                    foodItemObject.comments = foodItemComments
                }
                foodItemList.add(foodItemObject)
            }
            return foodItemList
        }

        fun getRestaurantImage(restaurant: Restaurant, recyclerAdapter: FavoriteRestaurantListAdapter){
            val userID = FirebaseAuth.getInstance().currentUser?.uid
            val storageReference = FirebaseStorage.getInstance().reference

            storageReference.child("images/users/" + userID + "/" + restaurant.uuid + ".jpg").downloadUrl.addOnSuccessListener  {
                    uri ->
                run {
                    restaurant.imageUri = uri
                    recyclerAdapter.notifyDataSetChanged()
                }
            }.addOnFailureListener{ exception -> System.out.println("DOWNLOAD FAIL: " + exception.localizedMessage) }
        }

        fun getRestaurantDetailImage(restaurant: Restaurant, imageView: ImageView, loaderContainer: LinearLayout, context: Context){
            if (!restaurant.googlePhotoReference.equals("")){
                PicassoUtil.loadGoogleImageIntoImageview(context, restaurant.googlePhotoReference, imageView, loaderContainer)
            } else {
                FirebaseAuth.getInstance().getCurrentUser()?.uid.let {userID ->
                    val storageReference = FirebaseStorage.getInstance().reference

                    storageReference.child("images/users/" + userID + "/" + restaurant.uuid + ".jpg").downloadUrl.addOnSuccessListener  {
                            uri -> run {
                        restaurant.imageUri = uri
                        PicassoUtil.loadUriIntoImageview(restaurant.imageUri, imageView, loaderContainer, context)
                        imageView.visibility = View.VISIBLE
                        loaderContainer.visibility = View.GONE
                    }
                    }.addOnFailureListener{ exception -> run {
                        imageView.setImageDrawable(context.resources.getDrawable(R.drawable.question_mark_icon_square))
                        imageView.visibility = View.VISIBLE
                        loaderContainer.visibility = View.GONE
                    } }
                }
            }
        }

        fun updateRestaurant(restaurant: Restaurant, restaurantUri: Uri?, activity: Activity){
            val userID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val ref = FirebaseDatabase.getInstance().getReference(FireBaseKeys.users).child(userID).child(FireBaseKeys.restaurants)
            restaurant.foodItems = null
            ref.child(restaurant.uuid).setValue(restaurant).addOnCompleteListener{
                if (restaurantUri != null){
                    uploadRestaurantImage(userID, false, restaurant.uuid, restaurantUri, activity, true)
                } else {
                    (activity as RestaurantDetailActivity).onEditFinished()
                }
            }

        }

        fun submitRestaurantWithRestaurantObject(restaurant: Restaurant, activity: Activity){
            val userID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val ref = FirebaseDatabase.getInstance().getReference(FireBaseKeys.users).child(userID).child(FireBaseKeys.restaurants)
            val resKeyId = ref.push().key
            resKeyId?.let {
                restaurant.uuid = resKeyId
                ref.child(resKeyId).setValue(restaurant).addOnCompleteListener{
                    activity.finish()
                }
            }
        }

        private fun uploadRestaurantImage(userID: String, searchImage: Boolean, resKeyId: String, restaurantUri: Uri, activity: Activity, editing: Boolean){
            var restUri = restaurantUri
            val storageReference = FirebaseStorage.getInstance().reference.child("images/users/" + userID + "/" + resKeyId + ".jpg")
            if (searchImage){
                val bitmap = getBitmapFromURL(restUri.toString())
                bitmap?.let {
                    restUri = getImageUri(activity.applicationContext, bitmap)
                }
            }
            storageReference.putFile(restUri).addOnSuccessListener {
                imageUploaded(editing, activity)
            }.addOnFailureListener{
                imageUploaded(editing, activity)
            }
        }

        private fun imageUploaded(editing: Boolean, activity: Activity){
            if (editing){
                (activity as RestaurantDetailActivity).onEditFinished()
            } else {
                activity.finish()
            }
        }

        private fun getBitmapFromURL(src: String): Bitmap? {
            return try {
                val url = URL(src)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream
                BitmapFactory.decodeStream(input)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

        private fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
            val bytes = ByteArrayOutputStream()
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "tempFoodEImage", null)
            return Uri.parse(path)
        }

        fun submitFoodItemToRestaurant(restaurantUuid: String, name: String, rating: Int, comments: String, foodUri: Uri?, activity: Activity, fragment: AddOrEditFoodItemFragment, editing: Boolean, foodItemUuid: String){
            FirebaseAuth.getInstance().getCurrentUser()?.uid?.let {userID ->
                val ref = FirebaseDatabase.getInstance().getReference(FireBaseKeys.users).child(userID).child(FireBaseKeys.restaurants).child(restaurantUuid).child(FireBaseKeys.foodItems)
                val foodKeyId: String?
                if (editing){
                    foodKeyId = foodItemUuid
                } else {
                    foodKeyId = ref.push().key
                }
                if (foodKeyId != null){
                    val foodItem = FoodItem(name, rating, foodKeyId)
                    foodItem.comments = comments
                    ref.child(foodKeyId).setValue(foodItem).addOnCompleteListener{
                        if (foodUri != null){
                            uploadFoodItemImage(userID, restaurantUuid, foodKeyId, foodUri, activity, editing)
                            fragment.setLoading(false)
                        } else {
                            fragment.setLoading(false)
                            if (editing){
                                activity.finish()
                                //TODO resolve this
                                //(activity as RestaurantDetailActivity).onEditFinished()
                            } else {
                                activity.finish()
                            }
                        }
                    }
                }
            }
        }

        fun setFoodItemImage(restaurant: Restaurant, foodItem: FoodItem, imageView: ImageView, imageLoader: MKLoader, context: Context){
            val userID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val storageReference = FirebaseStorage.getInstance().reference

            storageReference.child("images/users/" + userID + "/" + restaurant.uuid + "/" + foodItem.uuid +  ".jpg").downloadUrl.addOnSuccessListener  {
                    uri ->
                run {
                    foodItem.imageUri = uri
                    PicassoUtil.loadUriIntoImageview(foodItem.imageUri, imageView, imageLoader, context)
                }
            }.addOnFailureListener{ run {
                imageView.setImageDrawable(context.resources.getDrawable(R.drawable.question_mark_icon_square))
                imageView.visibility = View.VISIBLE
                imageLoader.visibility = View.GONE
            } }
        }

        private fun uploadFoodItemImage(userID: String, restaurantUuid: String, foodKeyId: String, restaurantUri: Uri, activity: Activity, editing: Boolean){
            val storageReference = FirebaseStorage.getInstance().reference.child("images/users/" + userID + "/" + restaurantUuid + "/" + foodKeyId + ".jpg")
            storageReference.putFile(restaurantUri).addOnSuccessListener {
                if (editing){
                    activity.finish()
                    //(activity as RestaurantDetailActivity).onEditFinished()
                } else {
                    activity.finish()
                }
            }.addOnFailureListener{
                if (editing){
                    activity.finish()
                    //(activity as RestaurantDetailActivity).onEditFinished()
                } else {
                    activity.finish()
                }
                //TODO Handle image upload fail
            }
        }

        fun removeRestaurant(restaurantUuid: String, activity: Activity){
            val userID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val ref = FirebaseDatabase.getInstance().getReference(FireBaseKeys.users).child(userID).child(FireBaseKeys.restaurants)
            ref.child(restaurantUuid).removeValue().addOnCompleteListener{
                activity.finish()
            }.addOnFailureListener{
                //TODO
            }
        }

        fun removeFoodItem(restaurantUuid: String, foodItemUuid: String, activity: Activity){
            val userID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val ref = FirebaseDatabase.getInstance().getReference(FireBaseKeys.users).child(userID).child(FireBaseKeys.restaurants).child(restaurantUuid).child(FireBaseKeys.foodItems)
            ref.child(foodItemUuid).removeValue().addOnCompleteListener{
                activity.finish()
            }.addOnFailureListener{
                //TODO
            }
        }

        fun changeUserLocation(addressName: String, lat: Double, lng: Double){
            val userID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            var location = Location(addressName, lat, lng)
            val ref = FirebaseDatabase.getInstance().getReference(FireBaseKeys.users).child(userID).child(FireBaseKeys.city)
            ref.setValue(location)
        }

        fun getLocationFromSnapshot(locationSnapshot: DataSnapshot): Location?{
            if (locationSnapshot.child(Keys.latId).exists()){
                val locName = locationSnapshot.child(FireBaseKeys.addressName).value.toString()
                val locLat = locationSnapshot.child(Keys.latId).value as Double
                val locLng = locationSnapshot.child(Keys.lngId).value as Double
                return Location(locName, locLat, locLng)
            } else {
                return null
            }
        }

    }


}