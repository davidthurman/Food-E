package com.thurman.foode.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.PropertyName
import com.google.gson.annotations.SerializedName

data class Restaurant(
    @get:PropertyName("name")
    var name: String,
    @get:PropertyName("address")
    var address: String,
    @get:PropertyName("rating")
    var rating: Int,
    @get:PropertyName("uuid")
    var uuid: String) {
    constructor() : this("","",0,"")

    var imageUri: Uri? = null
    //TODO This needs to be a hash map <String, FoodItem>
    var foodItems: List<FoodItem>? = null
    var comments: String = ""
    var lat: Double = 0.0
    var lng: Double = 0.0
    var googleId = ""
    var googlePhotoReference = ""
    var googleRating = 0.0

}