package com.thurman.foode.view_restaurants

import android.content.Intent
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
import com.squareup.picasso.Picasso
import com.thurman.foode.R
import com.thurman.foode.Utility.FirebaseUtil
import com.thurman.foode.add_restaurant.AddFoodItemActivity
import com.thurman.foode.models.FoodItem
import com.thurman.foode.models.Restaurant


class RestaurantDetailFragment : Fragment() {

    lateinit var currentView: View
    lateinit var restaurantUuid: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        currentView = inflater.inflate(R.layout.restaurant_detail_fragment,container,false)
        restaurantUuid = arguments!!.getString("restaurantUuid")!!
        getRestaurantFromUuid()
        return currentView
    }

    private fun getRestaurantFromUuid(){
        val restaurantListener = object : ValueEventListener {

            override fun onDataChange(restaurantSnapshot: DataSnapshot) {
                var restaurant = FirebaseUtil.getRestaurantFromSnapshot(restaurantSnapshot)
                setupFields(currentView, restaurant)
            }

            override fun onCancelled(databaseError: DatabaseError) {}

        }
        FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("restaurants").child(restaurantUuid).addListenerForSingleValueEvent(restaurantListener)
    }

    private fun setupFields(view: View, restaurant: Restaurant){
        setupAddFoodItemBtn(view, restaurant)
        setupRestaurantImage(view, restaurant)
        setupRestaurantTextFields(view, restaurant)
        setupRestaurantFoodItems(view, restaurant)
    }

    private fun setupAddFoodItemBtn(view: View, restaurant: Restaurant){
        var addFoodItemBtn = view.findViewById<Button>(R.id.add_food_item_btn)
        addFoodItemBtn.setOnClickListener{
            var addFoodItemActivity = AddFoodItemActivity()
            val intent = Intent(activity, addFoodItemActivity.javaClass)
            intent.putExtra("restaurantUuid", restaurant.uuid)
            startActivityForResult(intent, 200)
        }
    }

    private fun setupRestaurantImage(view: View, restaurant: Restaurant){
        var imageView = view.findViewById<ImageView>(R.id.image_view)
        FirebaseUtil.getRestaurantDetailImage(restaurant, imageView, context!!)
    }

    private fun setupRestaurantTextFields(view: View, restaurant: Restaurant){
        var nameTextField = view.findViewById<TextView>(R.id.name_textfield)
        nameTextField.text = restaurant.name
        var addressTextField = view.findViewById<TextView>(R.id.address_textfield)
        addressTextField.text = restaurant.address
    }

    private fun setupRestaurantFoodItems(view: View, restaurant: Restaurant){
        if (restaurant.foodItems != null){
            var foodRatingsList = view.findViewById<LinearLayout>(R.id.food_ratings_list)
            for (index in 0 until restaurant.foodItems!!.size) {
                addFoodItemToListview(restaurant.foodItems!!.get(index), foodRatingsList)
            }
        }
    }

    private fun addFoodItemToListview(foodItem: FoodItem, foodRatingsList: LinearLayout){
        var foodItemLayout = LayoutInflater.from(context!!).inflate(resources.getLayout(R.layout.food_item_layout), null)
        var foodItemName = foodItemLayout.findViewById<TextView>(R.id.food_item_name)
        foodItemName.text = foodItem.name
        var foodItemRatingBar = foodItemLayout.findViewById<RatingBar>(R.id.food_item_rating_bar)
        foodItemRatingBar.numStars = foodItem.rating
        foodItemLayout.setOnClickListener(View.OnClickListener {
            onFoodItemClick(foodItem)
        })
        foodRatingsList.addView(foodItemLayout)
    }

    private fun onFoodItemClick(foodItem: FoodItem){

    }

    fun refreshData(){
        getRestaurantFromUuid()
    }

}