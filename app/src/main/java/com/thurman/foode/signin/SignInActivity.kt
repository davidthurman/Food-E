package com.thurman.foode.signin

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.thurman.foode.MainActivity
import com.thurman.foode.R
import com.thurman.foode.Utility.Keys
import com.thurman.foode.forgot_password.ForgotPasswordActivity
import com.thurman.foode.forgot_password.ForgotPasswordFragment
import com.tuyenmonkey.mkloader.MKLoader
import kotlinx.android.synthetic.main.sign_in_layout.*

class SignInActivity : FragmentActivity() {

    var friendId: String? = null

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkForFriendLink()
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.sign_in_layout)
        auth = FirebaseAuth.getInstance()
        setupTextfields()
        setupButtons()
        checkIfUserIsLoggedIn()
    }

    private fun checkIfUserIsLoggedIn(){
        val currentUser = auth.currentUser
        currentUser?.let {
            friendId?.let {
                if (currentUser.uid == friendId){
                    friendId = null
                }
            }
            transitionScreen()
        }
    }

    private fun validLoginFields(): Boolean{
        var isValid = true
        if (username_textfield.text == null || username_textfield.text!!.toString() == ""){
            isValid = false
            username_textfield.error = getString(R.string.sign_in_username_error)
        }
        if (password_textfield.text == null || password_textfield.text!!.toString() == ""){
            isValid = false
            password_textfield.error = getString(R.string.sign_in_password_error)
        }
        return isValid
    }

    private fun setupButtons(){
        sign_in_button.setOnClickListener {
            if (validLoginFields()){
                setLoading(true)
                signIn()
            }
        }
        sign_up_button.setOnClickListener{
            if (validLoginFields()){
                setLoading(true)
                signUp()
            }
        }
        forgot_password_text.setOnClickListener {
            forgotPassword()
        }
    }

    private fun checkForFriendLink(){
        if (intent.action == Intent.ACTION_VIEW){
            val url: String = intent.data!!.path!!
            val segments = url.split("id=")
            friendId = segments[1]
        }
    }

    private fun setupTextfields(){
        username_textfield.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                username_textfield.error = null
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
        password_textfield.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                password_textfield.error = null
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
    }

    private fun signUp(){
        setLoading(true)
        auth.createUserWithEmailAndPassword(username_textfield.text.toString(), password_textfield.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    proceedToLogin()
                } else {
                    onError(task.exception?.message)
                }
            }
    }

    private fun checkLocationPermission(): Boolean
    {
        val permission = android.Manifest.permission.ACCESS_FINE_LOCATION;
        val res = checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private fun signIn(){
        auth.signInWithEmailAndPassword(username_textfield.text.toString(), password_textfield.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    proceedToLogin()
                } else {
                    onError(task.exception?.message)
                }
            }
    }

    private fun forgotPassword(){
        val intent = Intent(this, ForgotPasswordActivity::class.java)
        startActivity(intent)
    }

    private fun onError(errorMessage: String?){
        var messageToDisplay = errorMessage
        if (messageToDisplay == null){
            messageToDisplay = getString(R.string.sign_in_something_went_wrong)
        }
        setLoading(false)
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(messageToDisplay)
            .setPositiveButton(getString(R.string.sign_in_error_ok), null)
        val alert = dialogBuilder.create()
        alert.show()
    }

    private fun proceedToLogin(){
        if (checkLocationPermission()){
            transitionScreen()
        } else {
            val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(this, permissions,0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        transitionScreen()
    }

    private fun transitionScreen(){
        val intent = Intent(this, MainActivity::class.java)
        if (friendId != null){
            intent.putExtra(Keys.friendId, friendId)
        }
        startActivity(intent)
        setLoading(false)
    }

    private fun setLoading(loading: Boolean){
        if (loading){
            sign_in_loader.visibility = View.VISIBLE
            username_textfield.isEnabled = false
            password_textfield.isEnabled = false
            sign_in_button.visibility = View.GONE
            sign_up_button.visibility = View.GONE
        } else {
            sign_in_loader.visibility = View.GONE
            username_textfield.isEnabled = true
            password_textfield.isEnabled = true
            sign_in_button.visibility = View.VISIBLE
            sign_up_button.visibility = View.VISIBLE
        }
    }
}