package com.thurman.foode.add_restaurant

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.github.dhaval2404.imagepicker.ImagePicker
import com.squareup.picasso.Picasso
import java.io.File
import com.thurman.foode.R
import com.thurman.foode.Utility.FireBaseKeys
import com.thurman.foode.Utility.FirebaseUtil
import com.thurman.foode.Utility.Keys
import com.thurman.foode.view_restaurants.RestaurantDetailActivity
import kotlinx.android.synthetic.main.add_food_item_fragment.*


class AddOrEditFoodItemFragment : Fragment() {

    lateinit var restaurantUuid: String
    var currentUri: Uri? = null
    var editing: Boolean = false
    private var editingFoodItemUuid: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_food_item_fragment,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        submit_button.setOnClickListener{ submit() }
        upload_image_btn.setOnClickListener{ uploadImageClicked() }
        restaurantUuid = arguments?.getString(Keys.restUUID) ?: ""
        val editing = arguments?.getBoolean(Keys.editingFlag) ?: false
        if (editing){
            this.editing = true
            submit_button.text = "Update"
            editingFoodItemUuid = arguments?.getString(Keys.foodUUID) ?: ""
            setupEditing()
        }

    }

    private fun submit(){
        setLoading(true)
        FirebaseUtil.submitFoodItemToRestaurant(restaurantUuid, name_textfield.text.toString(), rating_bar.rating.toInt(), comments_textfield.text.toString(), currentUri, activity!!, this, editing, editingFoodItemUuid)

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
                    image_view.setImageURI(fileUri)

                    val file: File = ImagePicker.getFile(data)!!
                    currentUri = Uri.fromFile(file)
                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun setLoading(loading: Boolean){
        if (loading){
            content_container.visibility = View.GONE
            loading_container.visibility = View.VISIBLE
        } else {
            content_container.visibility = View.VISIBLE
            loading_container.visibility = View.GONE
        }
    }

    private fun setupEditing(){
        var foodItem = (activity as RestaurantDetailActivity).foodItemToEdit!!
        remove_button.visibility = View.VISIBLE
        remove_button.setOnClickListener{ FirebaseUtil.removeFoodItem(restaurantUuid, foodItem.uuid, activity!!) }
        name_textfield.setText(foodItem.name)
        comments_textfield.setText(foodItem.comments)
        rating_bar.rating = foodItem.rating.toFloat()
        foodItem.imageUri?.let {
            Picasso.with(context).load(it).into(image_view)
            currentUri = it
        } ?: image_view.setImageDrawable(resources.getDrawable(R.drawable.question_mark_icon_square, null))
    }

}