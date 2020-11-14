package com.thurman.foode.add_restaurant

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import kotlinx.android.synthetic.main.add_new_restaurant_tab.*
import java.util.*

class AddRestaurantFragment : Fragment() {

    lateinit var restaurantSearchBar: EditText
    private lateinit var currentView: View
    private lateinit var thisActivity: AddRestaurantActivity
    private lateinit var sponsoredRestaurantsLayout: LinearLayout
    var sponsoredRestaurants = ArrayList<Restaurant>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        currentView = inflater.inflate(R.layout.add_new_restaurant_tab, container, false)

        return currentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thisActivity = activity as AddRestaurantActivity
        sponsoredRestaurantsLayout = currentView.findViewById(R.id.sponsored_restaurants_layout)
        setupSearchBar(currentView)
        setupButtons(currentView)
        addSponsoredRestaurants()
    }

    override fun onResume() {
        super.onResume()
        setupCity()
    }

    private fun setupSearchBar(view: View){
        restaurantSearchBar = view.findViewById(R.id.restaurant_search_bar)
        restaurantSearchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                restaurantSearchBar.error = null
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
    }

    private fun setupCity(){
        val cityListener = object : ValueEventListener {
            override fun onDataChange(citySnapshot: DataSnapshot) {
                var location = FirebaseUtil.getLocationFromSnapshot(citySnapshot)
                if (location != null){
                    city_title.text = location.addressName
                    thisActivity.locationLng = location.lng
                    thisActivity.locationLat = location.lat
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        FirebaseAuth.getInstance().currentUser?.uid?.let {userID ->
            FirebaseDatabase.getInstance().reference.child("users").child(userID).child("city").addListenerForSingleValueEvent(cityListener)
        }
    }

    private fun setupButtons(view: View){
        var searchButton = view.findViewById<ImageButton>(R.id.search_btn)
        searchButton.setOnClickListener{
            city_title.setTextColor(resources.getColor(R.color.black, null))
            if (restaurantSearchBar.text.count() == 0){
                restaurantSearchBar.error = "Please enter a restaurant name"
            } else {
                transitionScreen()
            }
        }
        setupChangeCityButton(view)
    }

    private fun setupChangeCityButton(view: View){
        var changeCityButton = view.findViewById<LinearLayout>(R.id.change_city_btn)
        changeCityButton.setOnClickListener{ thisActivity.searchAutoComplete(city_title) }
    }

    private fun transitionScreen(){
        var fragment = RestaurantSearchFragment()
        var bundle = Bundle()
        bundle.putString(Keys.searchText, restaurantSearchBar.text.toString())
        bundle.putDouble(Keys.latId, thisActivity.locationLat)
        bundle.putDouble(Keys.lngId, thisActivity.locationLng)
        fragment.arguments = bundle
        (activity as AddRestaurantActivity).transitionFragment(fragment)
    }

    private fun addSponsoredRestaurants(){
        val restaurantsListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (restaurantSnapshot in dataSnapshot.children){
                    sponsoredRestaurants.add(FirebaseUtil.getRestaurantFromSnapshot(restaurantSnapshot))
                }
                var index = 1
                for (restaurant in sponsoredRestaurants){
                    addSponsorView(restaurant, (index != sponsoredRestaurants.size))
                    index += 1
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}

        }
        FirebaseDatabase.getInstance().reference.child(FireBaseKeys.sponsoredRestaurants).addListenerForSingleValueEvent(restaurantsListener)
    }

    private fun addSponsorView(restaurant: Restaurant, addDivider: Boolean){
        val sponsoredRestaurantView = LayoutInflater.from(context).inflate(resources.getLayout(R.layout.sponsored_restaurant_view), null)
        val restName = sponsoredRestaurantView.findViewById<TextView>(R.id.name)
        restName.text = restaurant.name
        val restRatingBar = sponsoredRestaurantView.findViewById<RatingBar>(R.id.rating_bar)
        restRatingBar.rating = restaurant.googleRating.toFloat()

        sponsoredRestaurantsLayout.addView(sponsoredRestaurantView)
        val loadingContainer = sponsoredRestaurantView.findViewById<LinearLayout>(R.id.loading_container)
        val imageView = sponsoredRestaurantView.findViewById<ImageView>(R.id.image)
        FirebaseUtil.getRestaurantDetailImage(restaurant, imageView, loadingContainer, requireContext())
        if (addDivider){
            val dividerView = View(context)
            dividerView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5)
            sponsoredRestaurantsLayout.addView(dividerView)
        }
    }
}