package com.thurman.foode.Utility

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import com.thurman.foode.R
import com.thurman.foode.models.Restaurant
import com.tuyenmonkey.mkloader.MKLoader
import org.json.JSONObject

class PicassoUtil {

    companion object {

        fun loadGoogleImageIntoImageview(context: Context, googlePhotoReferenceId: String, imageView: ImageView, loadingView: View){
            var imageUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + googlePhotoReferenceId + "&key=AIzaSyDCyMRUMFciuOhvLFdWp-FrxapIkPMY-JI"
            Picasso.with(context).load(imageUrl).into(imageView, object: com.squareup.picasso.Callback {
                override fun onSuccess() {
                    imageView.visibility = View.VISIBLE
                    loadingView.visibility = View.GONE
                }

                override fun onError() {
                    imageView.setImageDrawable(context.resources.getDrawable(R.drawable.question_mark_icon))
                    imageView.visibility = View.VISIBLE
                    loadingView.visibility = View.GONE
                }
            })
        }

        fun loadUriIntoImageview(imageUri: Uri?, imageView: ImageView, imageLoader: View, context: Context){
            if (imageUri != null){
                Picasso.with(context).load(imageUri).into(imageView, object: com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        imageView.visibility = View.VISIBLE
                        imageLoader.visibility = View.GONE
                    }

                    override fun onError() {
                        setQuestionMarkImage(imageView, imageLoader, context)
                    }
                })
            } else {
                setQuestionMarkImage(imageView, imageLoader, context)
            }

        }

        private fun setQuestionMarkImage(imageView: ImageView, imageLoader: View, context: Context){
            imageView.setImageDrawable(context.resources.getDrawable(R.drawable.question_mark_icon))
            imageView.visibility = View.VISIBLE
            imageLoader.visibility = View.GONE
        }


    }


}