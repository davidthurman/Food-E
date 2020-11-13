package com.thurman.foode.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class FoodItem(val name: String,
                    val rating: Int,
                    val uuid: String) {
    var imageUri: Uri? = null
    var comments: String = ""

}