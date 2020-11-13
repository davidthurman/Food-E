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
import com.thurman.foode.models.Restaurant
import java.util.*

class ShareRestaurantsFragment : Fragment() {

    lateinit var thisView: View
    var restaurants = ArrayList<Restaurant>()
    lateinit var checkboxLayout: LinearLayout
    var checkboxList = ArrayList<CheckBox>()
    var checkboxToRestaurant: HashMap<CheckBox, Restaurant> = HashMap()
    lateinit var includeFoodItemsSwitch: Switch
    var specifiedRestaurantUuid: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        thisView = inflater!!.inflate(R.layout.share_restaurants_fragment, container, false)
        if (arguments != null && arguments!!.getString(FireBaseKeys.restUUID) != null){
            specifiedRestaurantUuid = arguments!!.getString(FireBaseKeys.restUUID)!!

        }
        checkboxLayout = thisView.findViewById<LinearLayout>(R.id.checkbox_layout)
        includeFoodItemsSwitch = thisView.findViewById(R.id.include_food_items_switch)
        var shareButton = thisView.findViewById<Button>(R.id.share_button)
        shareButton.setOnClickListener { onShareClicked() }
        getRestaurants()
        return thisView
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
        FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("restaurants").addListenerForSingleValueEvent(restaurantsListener)
    }

    private fun addCheckbox(restaurant: Restaurant){
        var checkBox: CheckBox = LayoutInflater.from(context!!).inflate(resources.getLayout(R.layout.share_restaurant_checkbox_view), null) as CheckBox
        checkBox.text = restaurant.name
        if (specifiedRestaurantUuid != null && specifiedRestaurantUuid == restaurant.uuid){
            checkBox.isChecked = true
        }
        checkboxLayout.addView(checkBox)
        checkboxList.add(checkBox)
        checkboxToRestaurant.put(checkBox, restaurant)
    }

    private fun getCheckAllBox(){
        var checkBox: CheckBox = LayoutInflater.from(context!!).inflate(resources.getLayout(R.layout.share_restaurant_checkbox_all_view), null) as CheckBox
        checkBox.text = "Check all"
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            checkOrUncheckAll(isChecked)
            if (isChecked){
                checkBox.text = "Uncheck all"
            } else {
                checkBox.text = "Check all"
            }
        }
        checkboxLayout.addView(checkBox)
        addDividerLiner()
    }

    private fun checkOrUncheckAll(checkAll: Boolean){
        for (checkbox in checkboxList){
            checkbox.isChecked = checkAll
        }
    }

    private fun onShareClicked(){
        var sendIntent = Intent(Intent.ACTION_VIEW)
        sendIntent.data = Uri.parse("sms:")
        sendIntent.putExtra("sms_body", getShareMessage())
        startActivity(sendIntent)
    }

    private fun getShareMessage(): String {
        var shareMessage = ""
        var index = 0
        for (checkbox in checkboxList){
            if (checkbox.isChecked){
                if (checkboxToRestaurant.contains(checkbox)){
                    var restaurant = checkboxToRestaurant.get(checkbox)!!
                    shareMessage += (checkbox.text.toString()) + ": " + restaurant.rating + "/5"
                    if (index != (checkboxList.size - 1)){
                        shareMessage += "\n"
                        index ++
                    }
                    if (includeFoodItemsSwitch.isChecked){
                        shareMessage = getFoodItemsText(shareMessage, restaurant)
                    }
                }
            }
        }
        shareMessage += "\n"
        shareMessage += "Check out all of my favorite restaurants from the Savor app: http://www.savor.com/id=" + FirebaseAuth.getInstance().currentUser!!.uid
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
        var dividerView = View(context!!)
        dividerView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)
        dividerView.setBackgroundColor(resources.getColor(R.color.black))
        checkboxLayout.addView(dividerView)
    }

}