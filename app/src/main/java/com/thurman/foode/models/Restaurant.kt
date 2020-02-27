package com.thurman.foode.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class Restaurant(val name: String, val address: String, val rating: Int, val uuid: String) : Parcelable {
    constructor() : this("","",0,"")

    var imageUri: Uri? = null
    var foodItems: List<FoodItem>? = null

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!
    ) {
        imageUri = parcel.readParcelable(Uri::class.java.classLoader)
        foodItems = parcel.createTypedArrayList(FoodItem)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(address)
        parcel.writeInt(rating)
        parcel.writeString(uuid)
        parcel.writeParcelable(imageUri, flags)
        parcel.writeList(foodItems)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Restaurant> {
        override fun createFromParcel(parcel: Parcel): Restaurant {
            return Restaurant(parcel)
        }

        override fun newArray(size: Int): Array<Restaurant?> {
            return arrayOfNulls(size)
        }
    }


}