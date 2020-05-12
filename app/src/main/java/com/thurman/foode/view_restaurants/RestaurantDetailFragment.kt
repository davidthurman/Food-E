package com.thurman.foode.view_restaurants

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.view.marginBottom
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thurman.foode.R
import com.thurman.foode.Utility.FirebaseUtil
import com.thurman.foode.Utility.GoogleUtil
import com.thurman.foode.add_restaurant.AddFoodItemActivity
import com.thurman.foode.add_restaurant.ShareRestaurantsActivity
import com.thurman.foode.models.FoodItem
import com.thurman.foode.models.Restaurant
import com.tuyenmonkey.mkloader.MKLoader
import java.util.*


class RestaurantDetailFragment : Fragment() {

    lateinit var currentView: View
    lateinit var restaurantUuid: String
    var restaurant: Restaurant? = null
    lateinit var contentScroll: NestedScrollView
    lateinit var loadingContainer: LinearLayout
    lateinit var toolbar: Toolbar
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
        toolbar = currentView.findViewById(R.id.toolbar)
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
        setupToolbar(restaurant.name)
    }

    private fun setupToolbar(restaurantName: String){
        var isShow = false
        var scrollRange = -1
        var appBarLayout = currentView.findViewById<AppBarLayout>(R.id.appbar_layout)
        var collapsingToolbarLayout = currentView.findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar_layout)
        collapsingToolbarLayout.setCollapsedTitleTextColor(resources.getColor(R.color.white))
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { barLayout, verticalOffset ->
            if (scrollRange == -1){
                scrollRange = barLayout?.totalScrollRange!!
            }
            if (scrollRange + verticalOffset == 0){
                collapsingToolbarLayout.title = restaurantName
                isShow = true
            } else if (isShow){
                collapsingToolbarLayout.title = " " //careful there should a space between double quote otherwise it wont work
                isShow = false
            }
        })
    }

    private fun setupMoreOptions(){
        var editBtn = currentView.findViewById<ImageButton>(R.id.edit_icon)
        editBtn.setOnClickListener{
            onEditClicked()
        }
        var trashBtn = currentView.findViewById<ImageButton>(R.id.trash_icon)
        trashBtn.setOnClickListener{
            onRemoveClicked()
        }
        var mapBtn = currentView.findViewById<ImageButton>(R.id.map_icon)
        mapBtn.setOnClickListener{
            onMapClicked()
        }
        var shareBtn = currentView.findViewById<ImageButton>(R.id.share_btn)
        shareBtn.setOnClickListener { onShareClicked() }
    }

    private fun onShareClicked(){
        var shareRestaurantActivity = ShareRestaurantsActivity()
        val intent = Intent(activity, shareRestaurantActivity.javaClass)
        intent.putExtra("restaurantUuid", restaurantUuid)
        startActivity(intent)
    }

    private fun onEditClicked(){
        if (restaurant != null){
            (activity as RestaurantDetailActivity).editRestaurant(restaurant!!)
        }
    }

    private fun onMapClicked(){
        if (restaurant != null && context != null){
            GoogleUtil.openGoogleMaps(restaurant!!, context!!)
        }
    }

    private fun onRemoveClicked(){
        val dialogBuilder = AlertDialog.Builder(context!!)
        dialogBuilder.setMessage("Are you sure you want to delete this restaurant?")
            .setCancelable(true)
            .setPositiveButton("Yes", {
                    dialog, id -> removeRestaurant()
            })
            .setNegativeButton("Cancel", {
                    dialog, id -> dialog.cancel()
            })
        val alert = dialogBuilder.create()
        alert.show()

    }

    private fun removeRestaurant(){
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
        if (restaurant.comments != ""){
            var commentsTextField = view.findViewById<TextView>(R.id.comments_textfield)
            commentsTextField.setText(restaurant.comments)
            commentsTextField.visibility = View.VISIBLE
        }
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