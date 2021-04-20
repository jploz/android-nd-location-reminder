package com.udacity.locationreminder.authentication

import android.app.Application
import androidx.lifecycle.map
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.udacity.locationreminder.base.BaseViewModel
import com.udacity.locationreminder.utils.SingleLiveEvent

class AuthenticationViewModel(app: Application) : BaseViewModel(app) {

    enum class AuthenticationState {
        AUTHENTICATED,
        UNAUTHENTICATED
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

    val currentUser: FirebaseUser? get() = FirebaseAuth.getInstance().currentUser

    val launchSignInFlow: SingleLiveEvent<Unit> = SingleLiveEvent()

    fun onLoginClicked() {
        launchSignInFlow.call()
    }
}
