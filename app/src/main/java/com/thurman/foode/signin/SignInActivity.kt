package com.thurman.foode.signin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.thurman.foode.MainActivity
import com.thurman.foode.R

class SignInActivity : FragmentActivity() {

    lateinit var usernameInput: TextInputEditText
    lateinit var passwordInput: TextInputEditText

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_in_layout)
        auth = FirebaseAuth.getInstance()
        usernameInput = findViewById(R.id.username_textfield)
        passwordInput = findViewById(R.id.password_textfield)
        var signInButton = findViewById<Button>(R.id.sign_in_button)
        signInButton.setOnClickListener { signIn() }
        var signUpButton = findViewById<Button>(R.id.sign_up_button)
        signUpButton.setOnClickListener{
            signUp()
        }
        val currentUser = auth.currentUser
    }


    private fun signUp(){
        System.out.println("1")
        auth.createUserWithEmailAndPassword(usernameInput.text.toString(), passwordInput.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    System.out.println("2")
                    // Sign in success, update UI with the signed-in user's information
                    System.out.println("Success user")
                    val user = auth.currentUser


                    proceedToLogin()
                } else {
                    System.out.println("3")
                    //TODO Sign up fail
                    System.out.println("ERROR: " + task.exception.toString())
                }
            }
    }

    private fun signIn(){
        auth.signInWithEmailAndPassword(usernameInput.text.toString(), passwordInput.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    proceedToLogin()
                } else {
                    // If sign in fails, display a message to the user.
                    //TODO Sign in fail
                    System.out.println("ERROR: " + task.exception.toString())
                }

                // ...
            }
    }

    private fun proceedToLogin(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}