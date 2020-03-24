package com.thurman.foode.forgot_password

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

class ForgotPasswordActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(android.R.id.content,
            ForgotPasswordFragment()
        )
        fragmentTransaction.commit()
    }

}