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
import com.thurman.foode.Utility.FirebaseUtil
import com.thurman.foode.Utility.PicassoUtil
import com.thurman.foode.view_restaurants.RestaurantDetailActivity
import com.tuyenmonkey.mkloader.MKLoader


class ManualEntryFragment : Fragment() {

    lateinit var nameTextfield: TextInputEditText
    lateinit var addressTextfield: TextInputEditText
    lateinit var ratingBar: RatingBar
    lateinit var imageView: ImageView
    var currentUri: Uri? = null
    var imageFromSearchResults = false
    var editing = false
    var editingRestaurantUuid: String? = null
    var imageLoader: MKLoader? = null
    var fromSearch = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.manual_entry,container,false)
        setupFields(view)
        setupButtons(view)
        if (arguments != null){
            fromSearch = arguments!!.getBoolean("fromSearch")
            if (fromSearch != null && fromSearch){
                imageFromSearchResults = true
                imageLoader = view!!.findViewById(R.id.image_loader)
                setupLayoutFromSearchResults()
            }
            var editing = arguments!!.getBoolean("editing")
            if (editing != null && editing){
                editingRestaurantUuid= arguments!!.getString("restaurantUuid")
                imageLoader = view!!.findViewById(R.id.image_loader)
                this.editing = true
                setupEditing()
            }
        }
        return view
    }

    private fun setupFields(view: View){
        nameTextfield = view.findViewById(R.id.name_textfield)
        addressTextfield = view.findViewById(R.id.address_textfield)
        ratingBar = view.findViewById(R.id.rating_bar)
        imageView = view.findViewById(R.id.image_view)
    }

    private fun setupButtons(view: View){
        var submitBtn = view.findViewById<Button>(R.id.submit_button)
        submitBtn.setOnClickListener{ checkIfFieldsAreValid() }
        var uploadImageBtn = view.findViewById<Button>(R.id.upload_image_btn)
        uploadImageBtn.setOnClickListener{ uploadImageClicked() }
    }

    private fun checkIfFieldsAreValid(){
        //TODO Add validation
        submit()
    }

    private fun setupLayoutFromSearchResults(){
        var restaurant = (activity as AddRestaurantActivity).tempRestaurant!!
        nameTextfield.setText(restaurant.name)
        addressTextfield.setText(restaurant.address)
        ratingBar.rating = restaurant.rating.toFloat()
        if (!restaurant.googlePhotoReference.equals("")){
            PicassoUtil.loadGoogleImageIntoImageview(context!!, restaurant.googlePhotoReference, imageView, imageLoader!!)
        }
//        if (restaurant.imageUri != null){
//            Picasso.with(context).load(restaurant.imageUri).into(imageView)
//            currentUri = restaurant.imageUri
//        } else {
//            imageView.setImageDrawable(resources.getDrawable(R.drawable.question_mark_icon))
//        }
    }

    private fun setupEditing(){
        var restaurant = (activity as RestaurantDetailActivity).restaurantToEdit!!
        nameTextfield.setText(restaurant.name)
        addressTextfield.setText(restaurant.address)
        ratingBar.rating = restaurant.rating.toFloat()

        if (!restaurant.googlePhotoReference.equals("")){
            PicassoUtil.loadGoogleImageIntoImageview(context!!, restaurant.googlePhotoReference, imageView, imageLoader!!)
        } else if (restaurant.imageUri != null){
            Picasso.with(context).load(restaurant.imageUri).into(imageView)
            currentUri = restaurant.imageUri
        } else {
            imageView.setImageDrawable(resources.getDrawable(R.drawable.question_mark_icon))
        }
    }

    private fun submit(){
        if (fromSearch){
            var restaurantToSend = (activity as AddRestaurantActivity).tempRestaurant!!
            restaurantToSend.rating = ratingBar.rating.toInt()
            FirebaseUtil.submitRestaurantWithRestaurantObject(restaurantToSend, activity!!)
        } else {
            FirebaseUtil.submitRestaurant(nameTextfield.text.toString(),
                addressTextfield.text.toString(),
                ratingBar.rating.toInt(),
                currentUri,
                imageFromSearchResults,
                activity!!,
                editing,
                editingRestaurantUuid
            )
        }
    }



    private fun uploadImageClicked(){
        ImagePicker.with(this)
            .crop()	    			//Crop image(Optional), Check Customization for more option
            .compress(1024)			//Final image size will be less than 1 MB(Optional)
            .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
            .start {resultCode, data ->
                if (resultCode == Activity.RESULT_OK) {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data
                    imageView.setImageURI(fileUri)
                    imageFromSearchResults = false

                    //You can get File object from intent
                    val file: File = ImagePicker.getFile(data)!!
                    currentUri = Uri.fromFile(file)

                    //You can also get File Path from intent
                    val filePath:String = ImagePicker.getFilePath(data)!!
                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }

    }


}