package com.thurman.foode.add_restaurant

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thurman.foode.R
import com.thurman.foode.Utility.FirebaseUtil
import com.thurman.foode.models.City
import com.thurman.foode.models.Location
import com.thurman.foode.models.Restaurant
import kotlinx.android.synthetic.main.add_new_restaurant_tab.*
import java.util.*

class ShareRestaurantsFragment : Fragment() {

    lateinit var thisView: View
    var restaurants = ArrayList<Restaurant>()
    lateinit var checkboxLayout: LinearLayout
    var checkboxList = ArrayList<CheckBox>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        thisView = inflater!!.inflate(R.layout.share_restaurants_fragment, container, false)
        checkboxLayout = thisView.findViewById(R.id.checkbox_layout)
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
        checkboxLayout.addView(checkBox)
        checkboxList.add(checkBox)
    }

    private fun getCheckAllBox(){
        var checkBox: CheckBox = LayoutInflater.from(context!!).inflate(resources.getLayout(R.layout.share_restaurant_checkbox_view), null) as CheckBox
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
                shareMessage += (checkbox.text.toString())
                if (index != (checkboxList.size - 1)){
                    shareMessage += "\n"
                }
            }
        }
        return shareMessage
    }

    private fun addDividerLiner(){
        var dividerView = View(context!!)
        dividerView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)
        dividerView.setBackgroundColor(R.color.black)
        checkboxLayout.addView(dividerView)
    }

}