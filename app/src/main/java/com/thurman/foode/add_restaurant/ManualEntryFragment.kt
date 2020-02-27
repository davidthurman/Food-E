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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.thurman.foode.models.Restaurant
import android.widget.ImageView
import android.widget.Toast
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import com.thurman.foode.R
import com.thurman.foode.Utility.FirebaseUtil


class ManualEntryFragment : Fragment() {

    lateinit var nameTextfield: TextInputEditText
    lateinit var addressTextfield: TextInputEditText
    lateinit var ratingBar: RatingBar
    lateinit var imageView: ImageView
    var currentUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.manual_entry,container,false)
        setupFields(view)
        setupButtons(view)
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

    private fun submit(){
        FirebaseUtil.submitRestaurant(nameTextfield.text.toString(),
                                      addressTextfield.text.toString(),
                                      ratingBar.numStars,
                                      currentUri,
                                      activity!!)
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


}