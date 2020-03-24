package com.thurman.foode.forgot_password

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.thurman.foode.R

class PasswordSuccessfullyResetFragment : Fragment() {

    lateinit var homeButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater!!.inflate(R.layout.password_successfully_reset_fragment, container, false)
        homeButton = view.findViewById(R.id.home_button)
        homeButton.setOnClickListener { returnHome() }
        return view
    }

    private fun returnHome(){
        activity!!.finish()
    }

}