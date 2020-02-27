package com.thurman.foode.add_restaurant

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.thurman.foode.R

class AddNewRestaurantTab : Fragment() {

    lateinit var restaurantSearchBar: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater!!.inflate(R.layout.add_new_restaurant_tab, container, false)
        setupSearchBar(view)
        setupButtons(view)
        return view
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

    private fun setupButtons(view: View){
        var manualEntryButton = view.findViewById<Button>(R.id.manual_entry_btn)
        manualEntryButton?.setOnClickListener { transitionScreen("manual") }

        var searchButton = view.findViewById<Button>(R.id.search_btn)
        searchButton.setOnClickListener{
            if (restaurantSearchBar.text!!.count() > 0){
                transitionScreen("search")
            } else {
                restaurantSearchBar.error = "Please enter a restaurant name"
            }
        }
    }

    private fun transitionScreen(type: String){
        val intent = Intent(activity, AddRestaurantActivity::class.java)
        intent.putExtra("type", type)
        intent.putExtra("searchText", restaurantSearchBar.text.toString())
        startActivity(intent)
    }
}