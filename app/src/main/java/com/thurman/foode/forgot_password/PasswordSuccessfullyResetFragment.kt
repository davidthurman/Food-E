package com.thurman.foode.forgot_password

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.thurman.foode.R
import kotlinx.android.synthetic.main.password_successfully_reset_fragment.*

class PasswordSuccessfullyResetFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.password_successfully_reset_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        home_button.setOnClickListener { returnHome() }

    }

    private fun returnHome(){
        activity!!.finish()
    }

}