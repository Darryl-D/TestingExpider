package com.example.testingexpider

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signInButton: Button
    private lateinit var signUpTextView: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signInPage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.passwordedittext)
        signInButton = findViewById(R.id.buttonsignin)
        signUpTextView = findViewById(R.id.signup)

        // Set up click listener for the Sign In button
        signInButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter your email or phone", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Sign in with email and password
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(this, "Sign in successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(
                            this,
                            HomeScreenActivity::class.java
                        ) // Replace HomeActivity with your actual home screen activity
                        startActivity(intent)
                        finish() // Close the sign-in activity so the user can't go back without logging out
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            this, "Authentication failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
                return
            }
        }
        val time = System.currentTimeMillis() + 60_000 // 1 minute later
        scheduleNotification(this, "Test Title", "This is a test message", time)



        // Set up click listener for the Sign Up TextView
        signUpTextView.setOnClickListener {
            // Navigate to the sign-up activity
        }

        // You might want to add a click listener for the "Forgot Password" TextView as well
        findViewById<TextView>(R.id.forgetpass).setOnClickListener {
            Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.noaccount).setOnClickListener {

        }
    }

    //Check if the user is already signed in when the activity starts
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is already signed in, navigate to home screen
            val intent = Intent(
                this,
                HomeScreenActivity::class.java
            ) // Replace HomeActivity with your actual home screen activity
            startActivity(intent)
            finish()
        }
    }
}