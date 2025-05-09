package com.example.testingexpider

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class HomeScreenActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_screen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homeScreen)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        myFragment(HomeFragment())

        bottomNavigationView = findViewById(R.id.bottomNav)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            val fragment: Fragment? = when (item.itemId) {
                R.id.home -> HomeFragment()
                R.id.add -> AddFragment()
                R.id.remove -> RemoveFragment()
                R.id.profile -> ProfileFragment()
                else -> null
            }
            myFragment(fragment)
        }

    }

    private fun myFragment(fragment: Fragment?): Boolean {
        return if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.frame, fragment).commit()
            true
        } else {
            false
        }
    }
}
