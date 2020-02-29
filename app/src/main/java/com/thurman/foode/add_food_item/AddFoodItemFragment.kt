package com.thurman.foode.add_restaurant

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.github.dhaval2404.imagepicker.ImagePicker
import java.io.File
import com.thurman.foode.R
import com.thurman.foode.Utility.FirebaseUtil


class AddFoodItemFragment : Fragment() {

    lateinit var nameTextfield: TextInputEditText
    lateinit var ratingBar: RatingBar
    lateinit var imageView: ImageView
    lateinit var restaurantUuid: String
    var currentUri: Uri? = null
    lateinit var contentView: ScrollView
    lateinit var loadingContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_food_item_fragment,container,false)
        restaurantUuid = arguments!!.getString("restaurantUuid")!!
        nameTextfield = view.findViewById(R.id.name_textfield)
        ratingBar = view.findViewById(R.id.rating_bar)
        imageView = view.findViewById(R.id.image_view)
        contentView = view.findViewById(R.id.content_container)
        loadingContainer = view.findViewById(R.id.loading_container)
        var submitBtn = view.findViewById<Button>(R.id.submit_button)
        submitBtn.setOnClickListener{ checkIfFieldsAreValid() }
        var uploadImageBtn = view.findViewById<Button>(R.id.upload_image_btn)
        uploadImageBtn.setOnClickListener{ uploadImageClicked() }
        return view
    }

    private fun checkIfFieldsAreValid(){
        //TODO Add validation
        submit()
    }

    private fun submit(){
        setLoading(true)
        FirebaseUtil.submitFoodItemToRestaurant(restaurantUuid, nameTextfield.text.toString(), ratingBar.rating.toInt(), currentUri, activity!!, this)

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

    fun setLoading(loading: Boolean){
        if (loading){
            contentView.visibility = View.GONE
            loadingContainer.visibility = View.VISIBLE
        } else {
            contentView.visibility = View.VISIBLE
            loadingContainer.visibility = View.GONE
        }
    }

}