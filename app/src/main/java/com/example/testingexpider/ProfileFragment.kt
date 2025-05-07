package com.example.testingexpider

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var signOutButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var emailTextView: TextView
    private lateinit var profileIcon: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Find the sign-out button
        signOutButton = view.findViewById(R.id.signOutButton)
        emailTextView = view.findViewById(R.id.emailTextView)
        profileIcon = view.findViewById(R.id.profileIcon) // Find the ImageView

        // Get and display the user's email
        val currentUser = auth.currentUser
        if (currentUser != null) {
            emailTextView.text = currentUser.email // Set the email
        } else {
            emailTextView.text = "No user logged in"
        }

        // Set click listener for the sign-out button
        signOutButton.setOnClickListener {
            // Sign out the user
            auth.signOut()

            // Navigate back to the SignInActivity
            val intent =
                Intent(activity, SignInActivity::class.java) // Use 'activity' to get the context
            startActivity(intent)
            activity?.finish() // Finish the current activity (HomeScreenActivity)
        }

        return view
    }
}