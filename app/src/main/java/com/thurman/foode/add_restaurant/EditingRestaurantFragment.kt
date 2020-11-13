package com.thurman.foode.add_restaurant

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import android.widget.ImageView
import android.widget.Toast
import com.github.dhaval2404.imagepicker.ImagePicker
import com.squareup.picasso.Picasso
import java.io.File
import com.thurman.foode.R
import com.thurman.foode.Utility.FireBaseKeys
import com.thurman.foode.Utility.FirebaseUtil
import com.thurman.foode.Utility.Keys
import com.thurman.foode.Utility.PicassoUtil
import com.thurman.foode.models.Restaurant
import com.thurman.foode.view_restaurants.RestaurantDetailActivity
import com.tuyenmonkey.mkloader.MKLoader


class EditingRestaurantFragment : Fragment() {

    private lateinit var nameTextfield: TextInputEditText
    private lateinit var addressTextfield: TextInputEditText
    private lateinit var commentTextfield: TextInputEditText
    private lateinit var ratingBar: RatingBar
    private lateinit var imageView: ImageView
    private var currentUri: Uri? = null
    private var imageFromSearchResults = false
    private var editing = false
    private var editingRestaurantUuid: String? = null
    private var imageLoader: MKLoader? = null
    private var fromSearch = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.manual_entry,container,false)
        setupFields(view)
        fromSearch = arguments?.getBoolean(Keys.fromSearchFlag) ?: false
        if (fromSearch){
            imageFromSearchResults = true
            imageLoader = view.findViewById(R.id.image_loader)
            setupLayoutFromSearchResults()
        }
        val editing = arguments?.getBoolean(Keys.editingFlag) ?: false
        if (editing){
            editingRestaurantUuid = arguments?.getString(FireBaseKeys.restUUID) ?: ""
            imageLoader = view.findViewById(R.id.image_loader)
            this.editing = true
            setupEditing()
        }

        setupButtons(view)
        return view
    }

    private fun setupFields(view: View){
        nameTextfield = view.findViewById(R.id.name_textfield)
        addressTextfield = view.findViewById(R.id.address_textfield)
        ratingBar = view.findViewById(R.id.rating_bar)
        imageView = view.findViewById(R.id.image_view)
        commentTextfield = view.findViewById(R.id.comments_textfield)
    }

    private fun setupButtons(view: View){
        val submitBtn = view.findViewById<Button>(R.id.submit_button)
        submitBtn.setOnClickListener{ checkIfFieldsAreValid() }
        if (editing){
            submitBtn.text = "Update"
        }
        val uploadImageBtn = view.findViewById<Button>(R.id.upload_image_btn)
        uploadImageBtn.setOnClickListener{ uploadImageClicked() }
    }

    private fun checkIfFieldsAreValid(){
        //TODO Add validation
        submit()
    }

    private fun setupLayoutFromSearchResults(){
        val restaurant = (activity as AddRestaurantActivity).tempRestaurant!!
        nameTextfield.setText(restaurant.name)
        addressTextfield.setText(restaurant.address)
        ratingBar.rating = restaurant.rating.toFloat()
        if (restaurant.googlePhotoReference != ""){
            PicassoUtil.loadGoogleImageIntoImageview(context!!, restaurant.googlePhotoReference, imageView, imageLoader!!)
        }
    }

    private fun setupEditing(){
        val restaurant = (activity as RestaurantDetailActivity).restaurantToEdit!!
        nameTextfield.setText(restaurant.name)
        addressTextfield.setText(restaurant.address)
        ratingBar.rating = restaurant.rating.toFloat()
        commentTextfield.setText(restaurant.comments)

        if (restaurant.googlePhotoReference != ""){
            PicassoUtil.loadGoogleImageIntoImageview(context!!, restaurant.googlePhotoReference, imageView, imageLoader!!)
        } else if (restaurant.imageUri != null){
            Picasso.with(context).load(restaurant.imageUri).into(imageView)
            currentUri = restaurant.imageUri
        } else {
            imageView.setImageDrawable(resources.getDrawable(R.drawable.question_mark_icon_square))
        }
    }

    private fun getRestaurantFromFields(restaurant: Restaurant): Restaurant{
        restaurant.name = nameTextfield.text.toString()
        restaurant.address = addressTextfield.text.toString()
        restaurant.rating = ratingBar.rating.toInt()
        restaurant.comments = commentTextfield.text.toString()
        return restaurant
    }

    private fun submit(){
        if (fromSearch){
            val restaurantToSend = getRestaurantFromFields((activity as AddRestaurantActivity).tempRestaurant!!)
            FirebaseUtil.submitRestaurantWithRestaurantObject(restaurantToSend, activity!!)
        } else {
            val restaurant = getRestaurantFromFields((activity as RestaurantDetailActivity).restaurantToEdit!!)
            FirebaseUtil.updateRestaurant(restaurant,
                currentUri,
                activity!!
            )
        }
    }

    private fun uploadImageClicked(){
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .start {resultCode, data ->
                if (resultCode == Activity.RESULT_OK) {
                    val fileUri = data?.data
                    imageView.setImageURI(fileUri)
                    imageFromSearchResults = false
                    val file: File = ImagePicker.getFile(data)!!
                    currentUri = Uri.fromFile(file)
                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }

    }


}