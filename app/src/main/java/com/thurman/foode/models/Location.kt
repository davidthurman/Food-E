package com.thurman.foode.models

data class Location(val addressName: String,
                val lat: Double,
                val lng: Double) {
    constructor() : this("",0.0, 0.0)

}