package com.thurman.foode.forgot_password
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.thurman.foode.R
import com.thurman.foode.Utility.AlertUtil
import com.tuyenmonkey.mkloader.MKLoader
import kotlinx.android.synthetic.main.forgot_password_fragment.*


class ForgotPasswordFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.forgot_password_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reset_password_button.setOnClickListener { resetPassword() }
    }

    private fun validEmail(): Boolean{
        var isValid = true
        if (email_textfield.text == null || email_textfield.text!!.toString() == "" || !email_textfield.text!!.contains("@")){
            isValid = false
            email_textfield.error = "Please enter a valid email"
        }
        return isValid
    }


    private fun resetPassword(){
        if (validEmail()){
            setLoading(true)
            FirebaseAuth.getInstance().sendPasswordResetEmail(email_textfield.text!!.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        transitionToPasswordResetFragment()
                    } else {
                        setLoading(false)
                        AlertUtil.StandardAlert(null, "Something went wrong. Please try again.", "OK", context!!)
                    }
                }
        }
    }

    private fun setLoading(loading: Boolean){
        if (loading){
            forgot_password_loader.visibility = View.VISIBLE
            reset_password_button.visibility = View.GONE
        } else {
            forgot_password_loader.visibility = View.GONE
            reset_password_button.visibility = View.VISIBLE
        }
    }

    private fun transitionToPasswordResetFragment(){
        val fragmentTransaction = fragmentManager!!.beginTransaction()
        fragmentTransaction.replace(android.R.id.content, PasswordSuccessfullyResetFragment())
        fragmentTransaction.commit()
    }

}