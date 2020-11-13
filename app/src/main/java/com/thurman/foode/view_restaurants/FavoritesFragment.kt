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
import com.thurman.foode.add_restaurant.AddRestaurantActivity
import com.thurman.foode.add_restaurant.ShareRestaurantsActivity
import com.thurman.foode.models.FoodItem
import com.thurman.foode.models.Restaurant
import com.thurman.foode.signin.SignInActivity
import kotlinx.android.synthetic.main.favorites_tab.*
import kotlinx.android.synthetic.main.sponsored_restaurant_view.*

class FavoritesFragment : Fragment(), OnMapReadyCallback{

    lateinit var mapView: MapView
    lateinit var map: GoogleMap
    lateinit var fragment: FavoritesFragment
    lateinit var favRestaurantsList: RecyclerView
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
        var view = inflater!!.inflate(R.layout.favorites_tab, container, false)
        fragment = this
        if (arguments != null && arguments!!.containsKey("friendId")){
            friendId = arguments!!.getString("friendId")
        }

        var addButton: FloatingActionButton = view.findViewById(R.id.add_icon)

        addButton.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity, AddRestaurantActivity::class.java)
            startActivityForResult(intent, 200)
        })
        var logoutButton: ImageButton = view.findViewById(R.id.logout_button)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        var shareButton: ImageButton = view.findViewById(R.id.share_button)
        shareButton.setOnClickListener{
            onShareClicked()
        }
        if (friendId != null){
            addButton.hide()
            shareButton.visibility = View.GONE
            logoutButton.visibility = View.GONE
            setupHomeButton(view)
        }
        setupRecyclerView(view)
        onLocationEstablished()
        mapView = view.findViewById(R.id.mapview)
        mapView.onCreate(savedInstanceState)
        if (!checkLocationPermission()){
            mapView.visibility = View.GONE
        }
        return view
    }

    private fun checkLocationPermission(): Boolean
    {
        var permission = android.Manifest.permission.ACCESS_FINE_LOCATION;
        var res = activity!!.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private fun onLocationEstablished(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if (location != null){
                    latLng = LatLng(location.latitude, location.longitude)
                }
                mapView.getMapAsync(fragment)
            }

    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        getUserRestaurantData(recyclerAdapter)
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause();
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun setupRecyclerView(view: View){
        recyclerAdapter = FavoriteRestaurantListAdapter(restaurants, context!!)
        favRestaurantsList = view.findViewById<RecyclerView>(R.id.favorite_restaurants_list)
        favRestaurantsList.layoutManager = LinearLayoutManager(activity)
        favRestaurantsList.adapter = recyclerAdapter
        favRestaurantsList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recyclerAdapter.onItemClick = {restaurant -> onRestaurantClicked(restaurant)}
    }

    private fun setupHomeButton(view: View){
        var homeButton: ImageButton = view.findViewById(R.id.home_button)
        homeButton.visibility = View.VISIBLE
        homeButton.setOnClickListener { (activity as MainActivity).onHomeClicked() }
    }

    private fun onRestaurantClicked(restaurant: Restaurant){
        var restaurantDetailActivity = RestaurantDetailActivity()
        val intent = Intent(activity, restaurantDetailActivity.javaClass)
        intent.putExtra(FireBaseKeys.restUUID, restaurant.uuid)
        if (friendId != null){
            intent.putExtra("friendId", friendId)
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
                        if (restaurant.googlePhotoReference.equals("")){
                            FirebaseUtil.getRestaurantImage(restaurant, recyclerAdapter)
                        }
                        if (::map.isInitialized){
                            if (restaurant.lat != 0.0){
                                var restaurantLatLng = LatLng(restaurant.lat, restaurant.lng)
                                var marker = map.addMarker(MarkerOptions().position(restaurantLatLng).title(restaurant.name))
                                map.setOnMarkerClickListener(GoogleMap.OnMarkerClickListener {marker ->
                                    System.out.println(marker.title)
                                    var restaurantId = markerToIdHashmap.get(marker)
                                    System.out.println(restaurantId)
                                    recyclerAdapter.highlightRestaurant(restaurantId!!)
                                    if (recyclerAdapter.getPositionForUuid(restaurantId!!) != -1){
                                        favRestaurantsList.scrollToPosition(recyclerAdapter.getPositionForUuid(restaurantId!!))
                                    }
                                    false
                                })
                                markerToIdHashmap.put(marker, restaurant.uuid)
                            }
                        }
                    }
                }
                recyclerAdapter.notifyRestaurantsHaveChanged()
                setLoading(false)

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }

        }
        if (friendId == null){
            FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("restaurants").addListenerForSingleValueEvent(restaurantsListener)
        } else {
            FirebaseDatabase.getInstance().reference.child("users").child(friendId!!).child("restaurants").addListenerForSingleValueEvent(restaurantsListener)
        }
    }

    fun setLoading(loading: Boolean){
        var loadingContainer = view!!.findViewById<LinearLayout>(R.id.loading_container)
        var contentContainer = view!!.findViewById<FrameLayout>(R.id.content_container)
        if (!loading){
            loadingContainer.visibility = View.GONE
            contentContainer.visibility = View.VISIBLE
        }
    }

    private fun onShareClicked(){
        var shareRestaurantActivity = ShareRestaurantsActivity()
        val intent = Intent(activity, shareRestaurantActivity.javaClass)
        startActivity(intent)
    }
}