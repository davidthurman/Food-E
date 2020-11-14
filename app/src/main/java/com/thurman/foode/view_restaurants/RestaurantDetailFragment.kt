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
import com.thurman.foode.Utility.FireBaseKeys
import com.thurman.foode.Utility.FirebaseUtil
import com.thurman.foode.Utility.GoogleUtil
import com.thurman.foode.Utility.Keys
import com.thurman.foode.add_restaurant.AddFoodItemActivity
import com.thurman.foode.add_restaurant.ShareRestaurantsActivity
import com.thurman.foode.models.FoodItem
import com.thurman.foode.models.Restaurant
import com.tuyenmonkey.mkloader.MKLoader
import kotlinx.android.synthetic.main.food_item_layout.*
import kotlinx.android.synthetic.main.restaurant_detail_fragment.*
import kotlinx.android.synthetic.main.restaurant_detail_fragment.comments_textfield
import java.util.*


class RestaurantDetailFragment : Fragment() {

    lateinit var restaurantUuid: String
    var restaurant: Restaurant? = null
    var friendId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val currentView = inflater.inflate(R.layout.restaurant_detail_fragment,container,false)

        return currentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restaurantUuid = arguments?.getString(FireBaseKeys.restUUID) ?: ""
        arguments?.containsKey(Keys.friendId).let {
            friendId = arguments?.getString(Keys.friendId)
            setupFriendLink()
        }
        getRestaurantFromUuid()
    }

    override fun onResume() {
        super.onResume()
        getRestaurantFromUuid()
    }

    private fun getRestaurantFromUuid(){
        val restaurantListener = object : ValueEventListener {

            override fun onDataChange(restaurantSnapshot: DataSnapshot) {
                restaurant = FirebaseUtil.getRestaurantFromSnapshot(restaurantSnapshot)
                setupFields(restaurant!!)
                setLoading(false)
            }

            override fun onCancelled(databaseError: DatabaseError) {}

        }
        if (friendId == null){
            FirebaseDatabase.getInstance().reference.child(FireBaseKeys.users).child(FirebaseAuth.getInstance().currentUser!!.uid).child(FireBaseKeys.restaurants).child(restaurantUuid).addListenerForSingleValueEvent(restaurantListener)
        } else {
            FirebaseDatabase.getInstance().reference.child(FireBaseKeys.users).child(friendId!!).child(FireBaseKeys.restaurants).child(restaurantUuid).addListenerForSingleValueEvent(restaurantListener)
        }
    }

    private fun setupFields(restaurant: Restaurant){
        setupAddFoodItemBtn(restaurant)
        setupRestaurantImage(restaurant)
        setupRestaurantTextFields(restaurant)
        setupRestaurantFoodItems(restaurant)
        setupRatingBar(restaurant)
        setupMoreOptions()
        setupToolbar(restaurant.name)
    }

    private fun setupToolbar(restaurantName: String){
        var isShow = false
        var scrollRange = -1
        collapsing_toolbar_layout.setCollapsedTitleTextColor(resources.getColor(R.color.white))
        appbar_layout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { barLayout, verticalOffset ->
            if (scrollRange == -1){
                scrollRange = barLayout?.totalScrollRange!!
            }
            if (scrollRange + verticalOffset == 0){
                collapsing_toolbar_layout.title = restaurantName
                isShow = true
            } else if (isShow){
                collapsing_toolbar_layout.title = " "
                isShow = false
            }
        })
    }

    private fun setupMoreOptions(){
        edit_icon.setOnClickListener{ onEditClicked() }
        trash_icon.setOnClickListener{ onRemoveClicked() }
        map_icon.setOnClickListener{ onMapClicked() }
        share_btn.setOnClickListener { onShareClicked() }
    }

    private fun onShareClicked(){
        val shareRestaurantActivity = ShareRestaurantsActivity()
        val intent = Intent(activity, shareRestaurantActivity.javaClass)
        intent.putExtra(FireBaseKeys.restUUID, restaurantUuid)
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
        dialogBuilder.setMessage(getString(R.string.restaurant_detail_delete_confirmation_message))
            .setCancelable(true)
            .setPositiveButton(getString(R.string.restaurant_detail_delete_confirmation_confirm)) { _, _ -> removeRestaurant() }
            .setNegativeButton(getString(R.string.restaurant_detail_delete_confirmation_cancel)) { dialog, _ -> dialog.cancel() }
        val alert = dialogBuilder.create()
        alert.show()

    }

    private fun removeRestaurant(){
        FirebaseUtil.removeRestaurant(restaurantUuid, activity!!)
    }

    private fun setupAddFoodItemBtn(restaurant: Restaurant){
        add_food_item_btn.setOnClickListener{
            val addFoodItemActivity = AddFoodItemActivity()
            val intent = Intent(activity, addFoodItemActivity.javaClass)
            intent.putExtra(Keys.restUUID, restaurant.uuid)
            startActivityForResult(intent, 200)
        }
    }

    private fun setupRestaurantImage(restaurant: Restaurant){
        FirebaseUtil.getRestaurantDetailImage(restaurant, image_view, res_detail_loader_container, context!!)
    }

    private fun setupRestaurantTextFields(restaurant: Restaurant){
        name_textfield.text = restaurant.name
        address_textfield.text = restaurant.address
        if (restaurant.comments != ""){
            comments_textfield.text = restaurant.comments
            comments_textfield.visibility = View.VISIBLE
        }
    }

    private fun setupRatingBar(restaurant: Restaurant){
        restaurant_rating_bar.rating = restaurant.rating.toFloat()
    }

    private fun setupRestaurantFoodItems(restaurant: Restaurant){
        restaurant.foodItems?.let {foodItems ->
            food_ratings_list.removeAllViews()
            for (index in foodItems.indices) {
                var dividerBar = true
                if (index == (foodItems.size - 1)){
                    dividerBar = false
                }
                addFoodItemToListview(foodItems[index], restaurant, food_ratings_list, dividerBar)
            }
        }
    }

    private fun addFoodItemToListview(foodItem: FoodItem, restaurant: Restaurant, foodRatingsList: LinearLayout, dividerBar: Boolean){
        val foodItemLayout = LayoutInflater.from(context!!).inflate(resources.getLayout(R.layout.food_item_layout), null)
        val foodItemName = foodItemLayout.findViewById<TextView>(R.id.food_item_name)
        val foodItemRatingBar = foodItemLayout.findViewById<RatingBar>(R.id.rating_bar)
        foodItemName.text = foodItem.name
        foodItemRatingBar.rating = foodItem.rating.toFloat()
        if (foodItem.comments != ""){
            val foodItemComments = foodItemLayout.findViewById<TextView>(R.id.comments_textfield)
            foodItemComments.visibility = View.VISIBLE
            foodItemComments.text = "Comments: " + foodItem.comments
        }
        foodItemLayout.setOnClickListener { onFoodItemClick(foodItem) }
        val foodItemImage = foodItemLayout.findViewById<ImageView>(R.id.food_item_image)
        val foodItemLoader = foodItemLayout.findViewById<MKLoader>(R.id.image_loader)
        FirebaseUtil.setFoodItemImage(restaurant, foodItem, foodItemImage, foodItemLoader, context!!)
        foodRatingsList.addView(foodItemLayout)
        if (dividerBar){
            addDividerBar(foodRatingsList)
        }
    }

    private fun addDividerBar(foodRatingsList: LinearLayout){
        val dividerBar = LayoutInflater.from(context!!).inflate(resources.getLayout(R.layout.divider_bar), null)
        foodRatingsList.addView(dividerBar)
    }

    private fun onFoodItemClick(foodItem: FoodItem){
        if (friendId == null){
            (activity as RestaurantDetailActivity).editFoodItem(foodItem, restaurantUuid)
        }
    }

    private fun setLoading(loading: Boolean){
        if (loading){
            content_scroll.visibility = View.GONE
            loading_container.visibility = View.VISIBLE
        } else {
            content_scroll.visibility = View.VISIBLE
            loading_container.visibility = View.GONE
        }
    }

    private fun setupFriendLink(){
        edit_icon.visibility = View.GONE
        trash_icon.visibility = View.GONE
        share_btn.visibility = View.GONE
        add_food_item_btn.visibility = View.GONE
    }

}