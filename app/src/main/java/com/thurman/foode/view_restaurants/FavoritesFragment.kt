package com.thurman.foode.view_restaurants

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
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
import com.thurman.foode.Manifest
import com.thurman.foode.R
import com.thurman.foode.Utility.FirebaseUtil
import com.thurman.foode.add_restaurant.AddRestaurantActivity
import com.thurman.foode.models.FoodItem
import com.thurman.foode.models.Restaurant

class FavoritesFragment : Fragment(), OnMapReadyCallback{

    lateinit var mapView: MapView
    lateinit var map: GoogleMap

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isMyLocationButtonEnabled = false
        map.isMyLocationEnabled = true
        map.moveCamera(CameraUpdateFactory.newLatLng(LatLng(43.1, -87.9)))
    }

//    private fun canAccessLocation(): Boolean {
//        ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS)
//    }


    var restaurants = ArrayList<Restaurant>()
    lateinit var recyclerAdapter: FavoriteRestaurantListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater!!.inflate(R.layout.favorites_tab, container, false)
        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(activity!!, permissions,0)
        mapView = view.findViewById(R.id.mapview)
        mapView.onCreate(savedInstanceState)
        setupRecyclerView(view)
        var addButton: FloatingActionButton = view.findViewById(R.id.add_icon)
        addButton.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity, AddRestaurantActivity::class.java)
            startActivityForResult(intent, 200)
        })
        return view
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mapView.getMapAsync(this)
        mapView.onResume()
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
        getUserRestaurantData(recyclerAdapter)
        var favRestaurantsList = view.findViewById<RecyclerView>(R.id.favorite_restaurants_list)
        favRestaurantsList.layoutManager = LinearLayoutManager(activity)
        favRestaurantsList.adapter = recyclerAdapter
        favRestaurantsList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recyclerAdapter.onItemClick = {restaurant -> onRestaurantClicked(restaurant)}
    }

    private fun onRestaurantClicked(restaurant: Restaurant){
        var restaurantDetailActivity = RestaurantDetailActivity()
        val intent = Intent(activity, restaurantDetailActivity.javaClass)
        intent.putExtra("restaurantUuid", restaurant.uuid)
        startActivity(intent)
    }

    private fun getUserRestaurantData(recyclerAdapter: FavoriteRestaurantListAdapter){

        val restaurantsListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                restaurants.clear()
                for (restaurantSnapshot in dataSnapshot.children){
                    restaurants.add(FirebaseUtil.getRestaurantFromSnapshot(restaurantSnapshot))
                }
                for (restaurant in restaurants){
                    FirebaseUtil.getRestaurantImage(restaurant, recyclerAdapter)
                    if (restaurant.lat != 0.0){
                        var restaurantLatLng = LatLng(restaurant.lat, restaurant.lng)
                        var latLng = LatLng(-8.064903, -34.896872)
                        var marker = map.addMarker(MarkerOptions().position(latLng).title("Test").snippet("Another test"))
                        //map.addMarker(MarkerOptions().position(restaurantLatLng).title(restaurant.name))

                    }
                }
                recyclerAdapter.notifyDataSetChanged()
                setLoading(false)
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }

        }
        FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("restaurants").addListenerForSingleValueEvent(restaurantsListener)
    }

    fun setLoading(loading: Boolean){
        var loadingContainer = view!!.findViewById<LinearLayout>(R.id.loading_container)
        var contentContainer = view!!.findViewById<FrameLayout>(R.id.content_container)
        if (!loading){
            loadingContainer.visibility = View.GONE
            contentContainer.visibility = View.VISIBLE
        }
    }

//    fun updateResults(){
//        getUserRestaurantData(recyclerAdapter)
//    }

}