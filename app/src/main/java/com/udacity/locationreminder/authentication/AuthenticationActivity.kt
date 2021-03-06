package com.udacity.locationreminder.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.udacity.locationreminder.R
import com.udacity.locationreminder.databinding.ActivityAuthenticationBinding
import com.udacity.locationreminder.locationreminders.RemindersActivity
import org.koin.android.ext.android.inject

private const val TAG = "AuthenticationActivity"

/**
 * Login screen; asks the users to sign in / register,
 * and redirects the signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private val authViewModel: AuthenticationViewModel by inject()

    private lateinit var binding: ActivityAuthenticationBinding

    private val openAuthUiActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            Log.d(TAG, "resultCode: ${ActivityResult.resultCodeToString(result.resultCode)}")
            val response = IdpResponse.fromResultIntent(result.data)
            if (result.resultCode == Activity.RESULT_OK) {
                Log.i(
                    TAG,
                    "Successfully signed in user: ${authViewModel.currentUser?.displayName}!"
                )
                navigateToRemindersActivity()
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Log.i(TAG, "Sign in failed: ${response?.error?.errorCode}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_authentication
        )

        binding.viewModel = authViewModel

        authViewModel.launchSignInFlow.observe(this, {
            launchSignInFlow()
        })

        // do not observe authenticationState here, logged in users are only redirected
        // explicitly after receiving the result from login flow
        // this avoids redirection loops when users logout and are then send back to this
        // login screen

        // TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )
        openAuthUiActivity.launch(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
        )
    }

    private fun navigateToRemindersActivity() {
        val intent = Intent(this, RemindersActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
