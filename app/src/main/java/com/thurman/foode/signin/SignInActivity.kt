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
import com.thurman.foode.forgot_password.ForgotPasswordActivity
import com.thurman.foode.forgot_password.ForgotPasswordFragment
import com.tuyenmonkey.mkloader.MKLoader

class SignInActivity : FragmentActivity() {

    lateinit var usernameInput: TextInputEditText
    lateinit var passwordInput: TextInputEditText
    lateinit var forgotPasswordText: TextView
    lateinit var signInButton: Button
    lateinit var signUpButton: Button
    lateinit var loader: MKLoader
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
        loader = findViewById(R.id.sign_in_loader)
        val currentUser = auth.currentUser
        if (currentUser != null){
            transitionScreen()
        }
    }

    private fun validLoginFields(): Boolean{
        var isValid = true
        if (usernameInput.text == null || usernameInput.text!!.toString().equals("")){
            isValid = false
            usernameInput.error = "Please enter a valid username"
        }
        if (passwordInput.text == null || passwordInput.text!!.toString().equals("")){
            isValid = false
            passwordInput.error = "Please enter a valid password"
        }
        return isValid
    }

    private fun setupButtons(){
        signInButton = findViewById(R.id.sign_in_button)
        signInButton.setOnClickListener {
            if (validLoginFields()){
                setLoading(true)
                signIn()
            }
        }
        signUpButton = findViewById(R.id.sign_up_button)
        signUpButton.setOnClickListener{
            if (validLoginFields()){
                setLoading(true)
                signUp()
            }
        }
        forgotPasswordText = findViewById(R.id.forgot_password_text)
        forgotPasswordText.setOnClickListener {
            forgotPassword()
        }
    }

    private fun checkForFriendLink(){
        if (intent.action == Intent.ACTION_VIEW){
            var url: String = intent.data!!.path!!
            var segments = url.split("id=")
            friendId = segments[1]
        }
    }

    private fun setupTextfields(){
        usernameInput = findViewById(R.id.username_textfield)
        usernameInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                usernameInput.setError(null)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
        passwordInput = findViewById(R.id.password_textfield)
        passwordInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                passwordInput.setError(null)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
    }

    private fun signUp(){
        setLoading(true)
        auth.createUserWithEmailAndPassword(usernameInput.text.toString(), passwordInput.text.toString())
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
        var permission = android.Manifest.permission.ACCESS_FINE_LOCATION;
        var res = checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private fun signIn(){
        auth.signInWithEmailAndPassword(usernameInput.text.toString(), passwordInput.text.toString())
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
            messageToDisplay = "Something went wrong"
        }
        setLoading(false)
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(messageToDisplay)
            .setPositiveButton("OK", null)
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
            intent.putExtra("friendId", friendId)
        }
        startActivity(intent)
        setLoading(false)
    }

    private fun setLoading(loading: Boolean){
        if (loading){
            loader.visibility = View.VISIBLE
            usernameInput.isEnabled = false
            passwordInput.isEnabled = false
            signInButton.visibility = View.GONE
            signUpButton.visibility = View.GONE
        } else {
            loader.visibility = View.GONE
            usernameInput.isEnabled = true
            passwordInput.isEnabled = true
            signInButton.visibility = View.VISIBLE
            signUpButton.visibility = View.VISIBLE
        }
    }
}