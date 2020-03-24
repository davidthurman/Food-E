package com.thurman.foode.Utility

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.thurman.foode.R
import com.thurman.foode.add_restaurant.AddOrEditFoodItemFragment
import com.thurman.foode.models.City
import com.thurman.foode.models.FoodItem
import com.thurman.foode.models.Location
import com.thurman.foode.models.Restaurant
import com.thurman.foode.view_restaurants.FavoriteRestaurantListAdapter
import com.thurman.foode.view_restaurants.RestaurantDetailActivity
import com.tuyenmonkey.mkloader.MKLoader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FirebaseUtil {

    companion object {

        fun getRestaurantFromSnapshot(restaurantSnapshot: DataSnapshot): Restaurant{
            var restName = restaurantSnapshot.child("name").value.toString()
            var restAddress = restaurantSnapshot.child("address").value.toString()
            var restRating = (restaurantSnapshot.child("rating").value as Long).toInt()
            var restUuid = restaurantSnapshot.child("uuid").value.toString()
            var foodItemsSnapshot = restaurantSnapshot.child("foodItems").getValue()
            var restaurant = Restaurant(restName, restAddress, restRating, restUuid)
            if (foodItemsSnapshot != null){
                restaurant.foodItems = getFoodItemsFromSnapshot(foodItemsSnapshot)
            }
            if (restaurantSnapshot.child("googlePhotoReference").getValue() != null){
                restaurant.googlePhotoReference = restaurantSnapshot.child("googlePhotoReference").getValue().toString()
            }
            if (restaurantSnapshot.child("lat").getValue() != null){
                restaurant.lat = restaurantSnapshot.child("lat").getValue() as Double
                restaurant.lng = restaurantSnapshot.child("lng").getValue() as Double
            }
            if (restaurantSnapshot.child("comments").getValue() != null){
                restaurant.comments = restaurantSnapshot.child("comments").value.toString()
            }
            if (restaurantSnapshot.child("googleRating").getValue() != null){
                var googleRating: Double? = restaurantSnapshot.child("googleRating").getValue() as? Double
                if (googleRating != null){
                    restaurant.googleRating = googleRating
                }
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
                if (foodItemHash.get("comments") != null){
                    var foodItemComments = foodItemHash.get("comments") as String
                    foodItemObject.comments = foodItemComments
                }
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

        fun getRestaurantDetailImage(restaurant: Restaurant, imageView: ImageView, loaderContainer: LinearLayout, context: Context){
            if (!restaurant.googlePhotoReference.equals("")){
                PicassoUtil.loadGoogleImageIntoImageview(context!!, restaurant.googlePhotoReference, imageView, loaderContainer)
            } else {
                val userID = FirebaseAuth.getInstance().getCurrentUser()!!.uid
                var storageReference = FirebaseStorage.getInstance().reference

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

        fun updateRestaurant(restaurant: Restaurant, restaurantUri: Uri?, activity: Activity){
            val userID = FirebaseAuth.getInstance().getCurrentUser()!!.uid
            val ref = FirebaseDatabase.getInstance().getReference("users").child(userID).child("restaurants")
            ref.child(restaurant.uuid).setValue(restaurant).addOnCompleteListener{
                if (restaurantUri != null){
                    uploadRestaurantImage(userID, false, restaurant.uuid, restaurantUri, activity, true)
                } else {
                    (activity as RestaurantDetailActivity).onEditFinished()
                }
            }

        }

        fun submitRestaurant(name: String, address: String, rating: Int, restaurantUri: Uri?, searchImage: Boolean, activity: Activity, editing: Boolean, restaurantUuid: String?){
            val userID = FirebaseAuth.getInstance().getCurrentUser()!!.uid
            val ref = FirebaseDatabase.getInstance().getReference("users").child(userID).child("restaurants")
            var resKeyId: String?
            if (editing){
                resKeyId = restaurantUuid
            } else {
                resKeyId = ref.push().key
            }
            var restaurant = Restaurant(name, address, rating, resKeyId!!)
            if (resKeyId != null){
                ref.child(resKeyId).setValue(restaurant).addOnCompleteListener{
                    if (restaurantUri != null){
                        uploadRestaurantImage(userID, searchImage, resKeyId, restaurantUri, activity, editing)
                    } else {
                        if (editing){
                            (activity as RestaurantDetailActivity).onEditFinished()
                        } else {
                            activity?.finish()
                        }
                    }
                }
            }
        }

        fun submitRestaurantWithRestaurantObject(restaurant: Restaurant, activity: Activity){
            val userID = FirebaseAuth.getInstance().getCurrentUser()!!.uid
            val ref = FirebaseDatabase.getInstance().getReference("users").child(userID).child("restaurants")
            var resKeyId = ref.push().key
            if (resKeyId != null){
                restaurant.uuid = resKeyId
                ref.child(resKeyId).setValue(restaurant).addOnCompleteListener{
                    activity?.finish()
                }
            }
        }

        private fun uploadRestaurantImage(userID: String, searchImage: Boolean, resKeyId: String, restaurantUri: Uri, activity: Activity, editing: Boolean){
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
                if (editing){
                    (activity as RestaurantDetailActivity).onEditFinished()
                } else {
                    activity?.finish()
                }
            }.addOnFailureListener{
                //TODO Handle image upload fail
                deleteUploadedImage(restUri)
                if (editing){
                    (activity as RestaurantDetailActivity).onEditFinished()
                } else {
                    activity?.finish()
                }
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

        fun submitFoodItemToRestaurant(restaurantUuid: String, name: String, rating: Int, comments: String, foodUri: Uri?, activity: Activity, fragment: AddOrEditFoodItemFragment, editing: Boolean, foodItemUuid: String?){
            val userID = FirebaseAuth.getInstance().getCurrentUser()!!.uid
            val ref = FirebaseDatabase.getInstance().getReference("users").child(userID).child("restaurants").child(restaurantUuid).child("foodItems")
            var foodKeyId: String?
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
                            (activity as RestaurantDetailActivity).onEditFinished()
                        } else {
                            activity?.finish()
                        }
                    }
                }
            }
        }

        fun setFoodItemImage(restaurant: Restaurant, foodItem: FoodItem, imageView: ImageView, imageLoader: MKLoader, context: Context){
            val userID = FirebaseAuth.getInstance().getCurrentUser()!!.uid
            var storageReference = FirebaseStorage.getInstance().reference

            storageReference.child("images/users/" + userID + "/" + restaurant.uuid + "/" + foodItem.uuid +  ".jpg").downloadUrl.addOnSuccessListener  {
                    uri ->
                run {
                    foodItem.imageUri = uri
                    PicassoUtil.loadUriIntoImageview(foodItem.imageUri, imageView, imageLoader, context)
                }
            }.addOnFailureListener{ exception -> run {
                imageView.setImageDrawable(context.resources.getDrawable(R.drawable.question_mark_icon_square))
                imageView.visibility = View.VISIBLE
                imageLoader.visibility = View.GONE
            } }
        }

        private fun uploadFoodItemImage(userID: String, restaurantUuid: String, foodKeyId: String, restaurantUri: Uri, activity: Activity, editing: Boolean){
            var storageReference = FirebaseStorage.getInstance().reference.child("images/users/" + userID + "/" + restaurantUuid + "/" + foodKeyId + ".jpg")
            storageReference.putFile(restaurantUri).addOnSuccessListener {
                if (editing){
                    (activity as RestaurantDetailActivity).onEditFinished()
                } else {
                    activity?.finish()
                }
            }.addOnFailureListener{
                if (editing){
                    (activity as RestaurantDetailActivity).onEditFinished()
                } else {
                    activity?.finish()
                }
                //TODO Handle image upload fail
            }
        }

        fun removeRestaurant(restaurantUuid: String, activity: Activity){
            val userID = FirebaseAuth.getInstance().getCurrentUser()!!.uid
            val ref = FirebaseDatabase.getInstance().getReference("users").child(userID).child("restaurants")
            ref.child(restaurantUuid).removeValue().addOnCompleteListener{
                activity?.finish()
            }.addOnFailureListener{
                //TODO
            }
        }

        fun removeFoodItem(restaurantUuid: String, foodItemUuid: String, activity: Activity){
            val userID = FirebaseAuth.getInstance().getCurrentUser()!!.uid
            val ref = FirebaseDatabase.getInstance().getReference("users").child(userID).child("restaurants").child(restaurantUuid).child("foodItems")
            ref.child(foodItemUuid).removeValue().addOnCompleteListener{
                activity?.finish()
            }.addOnFailureListener{
                //TODO
            }
        }

        fun getRestaurantFromUuid(restaurantUuid: String){

        }

        fun changeUserLocation(addressName: String, lat: Double, lng: Double){
            val userID = FirebaseAuth.getInstance().getCurrentUser()!!.uid
            var location = Location(addressName, lat, lng)
            val ref = FirebaseDatabase.getInstance().getReference("users").child(userID).child("city")
            ref.setValue(location).addOnCompleteListener{

            }
        }

        fun changeUserCity(city: City, activity: Activity){
            val userID = FirebaseAuth.getInstance().getCurrentUser()!!.uid
            val ref = FirebaseDatabase.getInstance().getReference("users").child(userID).child("city")
            ref.setValue(city).addOnCompleteListener{
                val returnIntent = Intent()
                returnIntent.putExtra("cityId", city.id)
                returnIntent.putExtra("cityTitle", city.title)
                returnIntent.putExtra("cityCountry", city.country)
                activity!!.setResult(Activity.RESULT_OK, returnIntent)
                activity!!.finish()
            }
        }

        fun getCityFromSnapshot(citySnapshot: DataSnapshot): City{
            var cityTitle = citySnapshot.child("title").getValue().toString()
            var cityCountry = citySnapshot.child("country").getValue().toString()
            var cityId = citySnapshot.child("id").getValue().toString()
            var city = City(cityTitle, cityCountry, cityId)
            return city
        }

        fun getLocationFromSnapshot(locationSnapshot: DataSnapshot): Location{
            var locName = locationSnapshot.child("addressName").getValue().toString()
            var locLat = locationSnapshot.child("lat").getValue() as Double
            var locLng = locationSnapshot.child("lng").getValue() as Double
            var location = Location(locName, locLat, locLng)
            return location
        }

        fun getSponsoredRestaurants(){

        }

    }


}