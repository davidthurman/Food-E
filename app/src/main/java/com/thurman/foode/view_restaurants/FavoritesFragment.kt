package com.thurman.foode.view_restaurants

import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.thurman.foode.MainActivity
import com.thurman.foode.Manifest
import com.thurman.foode.R
import com.thurman.foode.Utility.FireBaseKeys
import com.thurman.foode.Utility.FirebaseUtil
import com.thurman.foode.Utility.Keys
import com.thurman.foode.add_restaurant.AddRestaurantActivity
import com.thurman.foode.add_restaurant.ShareRestaurantsActivity
import com.thurman.foode.models.FoodItem
import com.thurman.foode.models.Restaurant
import com.thurman.foode.signin.SignInActivity
import kotlinx.android.synthetic.main.favorites_tab.*
import kotlinx.android.synthetic.main.sponsored_restaurant_view.*

class FavoritesFragment : Fragment(), OnMapReadyCallback{

    lateinit var map: GoogleMap
    var markerToIdHashmap: HashMap<Marker, String> = HashMap()
    var restaurants = ArrayList<Restaurant>()
    lateinit var recyclerAdapter: FavoriteRestaurantListAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var latLng: LatLng? = null
    var friendId: String? = null

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isMyLocationButtonEnabled = false
        map.isMyLocationEnabled = true
        map.uiSettings.isZoomControlsEnabled = true
        if (latLng != null){
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11.toFloat()))
        }
        getUserRestaurantData(recyclerAdapter)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.favorites_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            friendId = arguments?.getString(Keys.friendId)
        }

        add_icon.setOnClickListener {
            val intent = Intent(activity, AddRestaurantActivity::class.java)
            startActivityForResult(intent, 200)
        }
        logout_button.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        share_button.setOnClickListener{
            onShareClicked()
        }
        if (friendId != null){
            add_icon.hide()
            share_button.visibility = View.GONE
            logout_button.visibility = View.GONE
            setupHomeButton()
        }
        setupRecyclerView()
        onLocationEstablished()
        mapview.onCreate(savedInstanceState)
        if (!checkLocationPermission()){
            mapview.visibility = View.GONE
        }
    }

    private fun checkLocationPermission(): Boolean
    {
        val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
        val res = activity?.checkCallingOrSelfPermission(permission)
        return (res == PackageManager.PERMISSION_GRANTED)
    }

    private fun onLocationEstablished(){
        activity?.let {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(it)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    location?.let {
                        latLng = LatLng(location.latitude, location.longitude)
                    }
                    mapview.getMapAsync(this)
                }
        }
    }

    override fun onResume() {
        super.onResume()
        mapview.onResume()
        getUserRestaurantData(recyclerAdapter)
    }

    override fun onPause() {
        super.onPause()
        mapview.onPause();
    }

    override fun onDestroy() {
        super.onDestroy()
        mapview.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapview.onLowMemory()
    }

    private fun setupRecyclerView(){
        recyclerAdapter = FavoriteRestaurantListAdapter(restaurants, this.requireContext())
        favorite_restaurants_list.layoutManager = LinearLayoutManager(activity)
        favorite_restaurants_list.adapter = recyclerAdapter
        favorite_restaurants_list.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recyclerAdapter.onItemClick = {restaurant -> onRestaurantClicked(restaurant)}
    }

    private fun setupHomeButton(){
        home_button.visibility = View.VISIBLE
        home_button.setOnClickListener { (activity as MainActivity).onHomeClicked() }
    }

    private fun onRestaurantClicked(restaurant: Restaurant){
        val restaurantDetailActivity = RestaurantDetailActivity()
        val intent = Intent(activity, restaurantDetailActivity.javaClass)
        intent.putExtra(FireBaseKeys.restUUID, restaurant.uuid)
        if (friendId != null){
            intent.putExtra(Keys.friendId, friendId)
        }
        startActivity(intent)
    }

    private fun getUserRestaurantData(recyclerAdapter: FavoriteRestaurantListAdapter){

        val restaurantsListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                restaurants.clear()
                if (::map.isInitialized){
                    map.clear()
                }
                if (dataSnapshot.children.count() == 0){
                    no_restaurants_added_text.visibility = View.VISIBLE
                    favorite_restaurants_list.visibility = View.GONE
                } else {
                    no_restaurants_added_text.visibility = View.GONE
                    favorite_restaurants_list.visibility = View.VISIBLE
                    for (restaurantSnapshot in dataSnapshot.children){
                        restaurants.add(FirebaseUtil.getRestaurantFromSnapshot(restaurantSnapshot))
                    }
                    restaurants.sortBy { it.name }
                    for (restaurant in restaurants){
                        if (restaurant.googlePhotoReference == ""){
                            FirebaseUtil.getRestaurantImage(restaurant, recyclerAdapter)
                        }
                        addRestaurantToMap(restaurant)
                    }
                }
                recyclerAdapter.notifyRestaurantsHaveChanged()
                setLoading(false)

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }

        }
        friendId?.let { friendId ->
            FirebaseDatabase.getInstance().reference.child("users").child(friendId).child("restaurants").addListenerForSingleValueEvent(restaurantsListener)
        } ?: FirebaseAuth.getInstance().currentUser?.let {currentUser ->
            FirebaseDatabase.getInstance().reference.child("users").child(currentUser.uid).child("restaurants").addListenerForSingleValueEvent(restaurantsListener)
        }
    }

    private fun addRestaurantToMap(restaurant: Restaurant){
        if (::map.isInitialized) {
            if (restaurant.lat != 0.0) {
                val restaurantLatLng = LatLng(restaurant.lat, restaurant.lng)
                val marker = map.addMarker(MarkerOptions().position(restaurantLatLng).title(restaurant.name))
                map.setOnMarkerClickListener { marker ->
                    markerToIdHashmap[marker]?.let { restId ->
                        recyclerAdapter.highlightRestaurant(restId)
                        if (recyclerAdapter.getPositionForUuid(restId) != -1) {
                            favorite_restaurants_list.scrollToPosition(
                                recyclerAdapter.getPositionForUuid(restId)
                            )
                        }
                    }
                    false
                }
                markerToIdHashmap[marker] = restaurant.uuid
            }
        }
    }

    fun setLoading(loading: Boolean){
        if (!loading){
            favorite_loading_container.visibility = View.GONE
            content_container.visibility = View.VISIBLE
        }
    }

    private fun onShareClicked(){
        val shareRestaurantActivity = ShareRestaurantsActivity()
        val intent = Intent(activity, shareRestaurantActivity.javaClass)
        startActivity(intent)
    }
}