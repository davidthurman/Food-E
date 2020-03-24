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
import com.thurman.foode.add_restaurant.AddRestaurantActivity
import com.thurman.foode.add_restaurant.ManualEntryFragment
import com.thurman.foode.add_restaurant.RestaurantSearchFragment
import com.thurman.foode.add_restaurant.ShareRestaurantsFragment
import com.tuyenmonkey.mkloader.MKLoader


class ForgotPasswordFragment : Fragment() {

    lateinit var emailTextfield: TextInputEditText
    lateinit var resetPasswordButton: Button
    lateinit var loadingContainer: MKLoader

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater!!.inflate(R.layout.forgot_password_fragment, container, false)
        emailTextfield = view.findViewById(R.id.email_textfield)
        resetPasswordButton = view.findViewById(R.id.reset_password_button)
        loadingContainer = view.findViewById(R.id.forgot_password_loader)
        resetPasswordButton.setOnClickListener { resetPassword() }
        return view
    }

    private fun validEmail(): Boolean{
        var isValid = true
        if (emailTextfield.text == null || emailTextfield.text!!.toString().equals("") || !emailTextfield.text!!.contains("@")){
            isValid = false
            emailTextfield.error = "Please enter a valid email"
        }
        return isValid
    }


    private fun resetPassword(){
        if (validEmail()){
            setLoading(true)
            FirebaseAuth.getInstance().sendPasswordResetEmail(emailTextfield.text!!.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        transitionToPasswordResetFragment()
                    } else {
                        setLoading(false)
                        //TODO handle reset password fail
                    }
                }
        }
    }

    private fun setLoading(loading: Boolean){
        if (loading){
            loadingContainer.visibility = View.VISIBLE
            resetPasswordButton.visibility = View.GONE
        } else {
            loadingContainer.visibility = View.GONE
            resetPasswordButton.visibility = View.VISIBLE
        }
    }

    private fun transitionToPasswordResetFragment(){
        val fragmentTransaction = fragmentManager!!.beginTransaction()
        fragmentTransaction.replace(android.R.id.content, PasswordSuccessfullyResetFragment())
        fragmentTransaction.commit()
    }

}