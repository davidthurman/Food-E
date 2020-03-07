package com.thurman.foode.view_restaurants

import android.app.AlertDialog
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
import com.thurman.foode.R
import com.thurman.foode.Utility.FirebaseUtil
import com.thurman.foode.add_restaurant.AddFoodItemActivity
import com.thurman.foode.add_restaurant.AddRestaurantActivity
import com.thurman.foode.add_restaurant.SearchCityActivity
import com.thurman.foode.models.FoodItem
import com.thurman.foode.models.Restaurant
import com.tuyenmonkey.mkloader.MKLoader


class RestaurantDetailFragment : Fragment() {

    lateinit var currentView: View
    lateinit var restaurantUuid: String
    var restaurant: Restaurant? = null
    lateinit var contentScroll: ScrollView
    lateinit var loadingContainer: LinearLayout
    var optionsMenuOpen = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        currentView = inflater.inflate(R.layout.restaurant_detail_fragment,container,false)
        contentScroll = currentView.findViewById(R.id.content_scroll)
        loadingContainer = currentView.findViewById(R.id.loading_container)
        restaurantUuid = arguments!!.getString("restaurantUuid")!!

        getRestaurantFromUuid()
        return currentView
    }

    override fun onResume() {
        super.onResume()
        getRestaurantFromUuid()
    }

    private fun getRestaurantFromUuid(){
        val restaurantListener = object : ValueEventListener {

            override fun onDataChange(restaurantSnapshot: DataSnapshot) {
                restaurant = FirebaseUtil.getRestaurantFromSnapshot(restaurantSnapshot)
                setupFields(currentView, restaurant!!)
                setLoading(false)
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
        setupRatingBar(view, restaurant)
        setupMoreOptions()
    }

    private fun setupMoreOptions(){
        var moreIcon = currentView.findViewById<ImageButton>(R.id.more_icon)
        val dialogBuilder = AlertDialog.Builder(context!!).create()
        var dialogView = layoutInflater.inflate(R.layout.restaurant_detail_more_options_layout, null)
        var editBtn = dialogView.findViewById<Button>(R.id.edit_button)
        editBtn.setOnClickListener(View.OnClickListener {
            dialogBuilder.dismiss()
            onEditClicked()
        })
        var deleteBtn = dialogView.findViewById<Button>(R.id.remove_button)
        deleteBtn.setOnClickListener(View.OnClickListener {
            FirebaseUtil.removeRestaurant(restaurantUuid, activity!!)
        })
        dialogBuilder.setView(dialogView)
        moreIcon.setOnClickListener{
            dialogBuilder.show()
        }
    }

    private fun onEditClicked(){
        if (restaurant != null){
            (activity as RestaurantDetailActivity).editRestaurant(restaurant!!)
        }
    }

    private fun onRemoveClicked(){
        FirebaseUtil.removeRestaurant(restaurantUuid, activity!!)
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
        var loaderContainer = view.findViewById<LinearLayout>(R.id.res_detail_loader_container)
        FirebaseUtil.getRestaurantDetailImage(restaurant, imageView, loaderContainer, context!!)
    }

    private fun setupRestaurantTextFields(view: View, restaurant: Restaurant){
        var nameTextField = view.findViewById<TextView>(R.id.name_textfield)
        nameTextField.text = restaurant.name
        var addressTextField = view.findViewById<TextView>(R.id.address_textfield)
        addressTextField.text = restaurant.address
    }

    private fun setupRatingBar(view: View, restaurant: Restaurant){
        var ratingBar = view.findViewById<RatingBar>(R.id.restaurant_rating_bar)
        ratingBar.rating = restaurant.rating.toFloat()
    }

    private fun setupRestaurantFoodItems(view: View, restaurant: Restaurant){
        if (restaurant.foodItems != null){
            var foodRatingsList = view.findViewById<LinearLayout>(R.id.food_ratings_list)
            foodRatingsList.removeAllViews()
            for (index in 0 until restaurant.foodItems!!.size) {
                var dividerBar = true
                if (index == (restaurant.foodItems!!.size - 1)){
                    dividerBar = false
                }
                addFoodItemToListview(restaurant.foodItems!!.get(index), restaurant, foodRatingsList, dividerBar)
            }
        }
    }

    private fun addFoodItemToListview(foodItem: FoodItem, restaurant: Restaurant, foodRatingsList: LinearLayout, dividerBar: Boolean){
        var foodItemLayout = LayoutInflater.from(context!!).inflate(resources.getLayout(R.layout.food_item_layout), null)
        var foodItemName = foodItemLayout.findViewById<TextView>(R.id.food_item_name)
        foodItemName.text = foodItem.name
        var foodItemRatingBar = foodItemLayout.findViewById<RatingBar>(R.id.rating_bar)
        foodItemRatingBar.rating = foodItem.rating.toFloat()
        if (foodItem.comments != ""){
            var foodItemComments = foodItemLayout.findViewById<TextView>(R.id.comments_textfield)
            foodItemComments.visibility = View.VISIBLE
            foodItemComments.text = "Comments: " + foodItem.comments
        }

        foodItemLayout.setOnClickListener(View.OnClickListener {
            onFoodItemClick(foodItem)
        })
        var foodItemImage = foodItemLayout.findViewById<ImageView>(R.id.food_item_image)
        var foodItemLoader = foodItemLayout.findViewById<MKLoader>(R.id.image_loader)
        FirebaseUtil.setFoodItemImage(restaurant, foodItem, foodItemImage, foodItemLoader, context!!)
        foodRatingsList.addView(foodItemLayout)
        if (dividerBar){
            addDividerBar(foodRatingsList)
        }
    }

    private fun addDividerBar(foodRatingsList: LinearLayout){
        var dividerBar = LayoutInflater.from(context!!).inflate(resources.getLayout(R.layout.divider_bar), null)
        foodRatingsList.addView(dividerBar)
    }

    private fun onFoodItemClick(foodItem: FoodItem){
        (activity as RestaurantDetailActivity).editFoodItem(foodItem, restaurantUuid)
    }

    fun refreshData(){
        getRestaurantFromUuid()
    }

    private fun setLoading(loading: Boolean){
        if (loading){
            contentScroll.visibility = View.GONE
            loadingContainer.visibility = View.VISIBLE
        } else {
            contentScroll.visibility = View.VISIBLE
            loadingContainer.visibility = View.GONE
        }
    }

}