package com.thurman.foode.add_restaurant

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thurman.foode.R
import com.thurman.foode.Utility.FireBaseKeys
import com.thurman.foode.Utility.FirebaseUtil
import com.thurman.foode.Utility.Keys
import com.thurman.foode.models.Restaurant
import kotlinx.android.synthetic.main.share_restaurants_fragment.*
import java.util.*

class ShareRestaurantsFragment : Fragment() {

    var restaurants = ArrayList<Restaurant>()
    var checkboxList = ArrayList<CheckBox>()
    var checkboxToRestaurant: HashMap<CheckBox, Restaurant> = HashMap()
    var specifiedRestaurantUuid: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.share_restaurants_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            specifiedRestaurantUuid = it.getString(FireBaseKeys.restUUID)
        }
        share_button.setOnClickListener { onShareClicked() }
        getRestaurants()
    }

    private fun getRestaurants(){
        val restaurantsListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                getCheckAllBox()
                for (restaurantSnapshot in dataSnapshot.children){
                    restaurants.add(FirebaseUtil.getRestaurantFromSnapshot(restaurantSnapshot))
                }
                var index = 1
                for (restaurant in restaurants){
                    addCheckbox(restaurant)
                    if (index != restaurants.count()){
                        addDividerLiner()
                    }
                        index += 1
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}

        }
        FirebaseDatabase.getInstance().reference.child(FireBaseKeys.users).child(FirebaseAuth.getInstance().currentUser!!.uid).child(FireBaseKeys.restaurants).addListenerForSingleValueEvent(restaurantsListener)
    }

    private fun addCheckbox(restaurant: Restaurant){
        val checkBox: CheckBox = LayoutInflater.from(context!!).inflate(resources.getLayout(R.layout.share_restaurant_checkbox_view), null) as CheckBox
        checkBox.text = restaurant.name
        if (specifiedRestaurantUuid != null && specifiedRestaurantUuid == restaurant.uuid){
            checkBox.isChecked = true
        }
        checkbox_layout.addView(checkBox)
        checkboxList.add(checkBox)
        checkboxToRestaurant[checkBox] = restaurant
    }

    private fun getCheckAllBox(){
        val checkBox: CheckBox = LayoutInflater.from(context!!).inflate(resources.getLayout(R.layout.share_restaurant_checkbox_all_view), null) as CheckBox
        checkBox.text = getString(R.string.share_restaurants_check_all)
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            checkOrUncheckAll(isChecked)
            if (isChecked){
                checkBox.text = getString(R.string.share_restaurants_uncheck_all)
            } else {
                checkBox.text = getString(R.string.share_restaurants_check_all)
            }
        }
        checkbox_layout.addView(checkBox)
        addDividerLiner()
    }

    private fun checkOrUncheckAll(checkAll: Boolean){
        for (checkbox in checkboxList){
            checkbox.isChecked = checkAll
        }
    }

    private fun onShareClicked(){
        val sendIntent = Intent(Intent.ACTION_VIEW)
        sendIntent.data = Uri.parse(Keys.smsUriKey)
        sendIntent.putExtra(Keys.smsBody, getShareMessage())
        startActivity(sendIntent)
    }

    private fun getShareMessage(): String {
        var shareMessage = ""
        var index = 0
        for (checkbox in checkboxList){
            if (checkbox.isChecked){
                if (checkboxToRestaurant.contains(checkbox)){
                    val restaurant = checkboxToRestaurant.get(checkbox)!!
                    shareMessage += (checkbox.text.toString()) + ": " + restaurant.rating + "/5"
                    if (index != (checkboxList.size - 1)){
                        shareMessage += "\n"
                        index ++
                    }
                    if (include_food_items_switch.isChecked){
                        shareMessage = getFoodItemsText(shareMessage, restaurant)
                    }
                }
            }
        }
        shareMessage += "\n"
        shareMessage += String.format(getString(R.string.share_restuarants_checkOutMyRestaurants, FirebaseAuth.getInstance().currentUser!!.uid))
        return shareMessage
    }

    private fun getFoodItemsText(_shareMessage: String, restaurant: Restaurant): String {
        var shareMessage = _shareMessage
        if (restaurant.foodItems != null && !restaurant.foodItems!!.isEmpty()){
            for (foodItem in restaurant.foodItems!!){
                shareMessage += "      -" + foodItem.name + ": " + foodItem.rating + "/5\n"
            }
        }
        return shareMessage
    }

    private fun addDividerLiner(){
        val dividerView = View(context!!)
        dividerView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)
        dividerView.setBackgroundColor(resources.getColor(R.color.black))
        checkbox_layout.addView(dividerView)
    }

}