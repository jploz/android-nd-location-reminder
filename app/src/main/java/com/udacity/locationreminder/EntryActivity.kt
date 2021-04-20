package com.udacity.locationreminder

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.udacity.locationreminder.authentication.AuthenticationActivity
import com.udacity.locationreminder.authentication.AuthenticationViewModel
import com.udacity.locationreminder.locationreminders.RemindersActivity
import org.koin.android.ext.android.inject


/**
 * This class should be the starting point of the app, It loads the next screen depending on
 * whether a user is already logged in or not.
 */
class EntryActivity : AppCompatActivity() {

    private val authViewModel: AuthenticationViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val className = getScreenClassName()
        val launchActivity = Class.forName(className)
        val launchIntent = Intent(applicationContext, launchActivity)
        startActivity(launchIntent)
        finish()
    }

    private fun getScreenClassName(): String {
        if (authViewModel.currentUser != null) {
            return RemindersActivity::class.java.name
        }
        return AuthenticationActivity::class.java.name
    }
}
