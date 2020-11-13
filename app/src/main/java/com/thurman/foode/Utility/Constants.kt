package com.thurman.foode.Utility

object Keys {
    const val friendId = "friendId"
    const val latId = "lat"
    const val lngId = "lng"
    const val searchText = "searchText"

    //FLAGS
    const val editingFlag = "editing"
    const val fromSearchFlag = "fromSearch"
}

object FireBaseKeys {
    //Restaurant
    const val restUUID = "uuid"
    const val restName = "name"
    const val restAddress = "address"
    const val restRating = "rating"
    const val restFoodItems = "foodItems"
    const val restGooglePhotoReference = "googlePhotoReference"
    const val restComments = "comments"
    const val restGoogleRating = "googleRating"
    const val sponsoredRestaurants = "sponsoredRestaurants"

    //Food Item
    const val foodName = "name"
    const val foodRating = "rating"
    const val foodUUID = "uuid"
    const val foodComments = "comments"
}

object GoogleKeys {
    const val results = "results"
    const val lat = "lat"
    const val lng = "lng"
    const val name = "name"
    const val formattedAddress = "formatted_address"
    const val photos = "photos"
    const val photoReference = "photo_reference"
    const val geometry = "geometry"
    const val placeId = "place_id"
    const val rating = "rating"
    const val location = "location"

    const val googleMapsPackageName = "com.google.android.apps.maps"
    const val googleMapsStatus = "status"
    const val googleMapsZeroResults = "ZERO_RESULTS"
}