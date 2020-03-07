package com.thurman.foode.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class Restaurant(val name: String, val address: String, var rating: Int, var uuid: String) {
    constructor() : this("","",0,"")

    var imageUri: Uri? = null
    var foodItems: List<FoodItem>? = null
    var comments: String = ""
    var lat: Double = 0.0
    var lng: Double = 0.0
    var googleId = ""
    var googlePhotoReference = ""
    var googleRating = 0.0

}