package com.thurman.foode.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class City(val title: String,
                    val country: String,
                    val id: String) {
    constructor() : this("","","")

}