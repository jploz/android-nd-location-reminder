package com.udacity.locationreminder.locationreminders.reminderslist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.location.LocationServices
import com.udacity.locationreminder.R
import com.udacity.locationreminder.authentication.AuthenticationActivity
import com.udacity.locationreminder.base.NavigationCommand
import com.udacity.locationreminder.databinding.FragmentRemindersBinding
import com.udacity.locationreminder.locationreminders.ReminderDescriptionActivity
import com.udacity.locationreminder.locationreminders.geofence.GeofenceBaseFragment
import com.udacity.locationreminder.utils.setDisplayHomeAsUpEnabled
import com.udacity.locationreminder.utils.setTitle
import com.udacity.locationreminder.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "ReminderListFragment"

class ReminderListFragment : GeofenceBaseFragment() {

    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()

    private lateinit var binding: FragmentRemindersBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )
        binding.lifecycleOwner = this
        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }

        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }

    override fun onStart() {
        super.onStart()
        // this fragment is the main screen for logged in user, so permissions are checked here first
        checkPermissionsAndStart()
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
            navigateToReminderDetails(it)
        }

        // setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }

    private fun navigateToReminderDetails(item: ReminderDataItem) {
        val intent = ReminderDescriptionActivity.newIntent(requireContext(), item)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                AuthUI
                    .getInstance()
                    .signOut(requireContext())
                    .addOnCompleteListener {
                        navigateToAuthenticationActivity()
                    }
            }
            R.id.action_delete_all -> {
                _viewModel.deleteAllReminders()
                removeGeofences()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }

    private fun navigateToAuthenticationActivity() {
        val intent = Intent(requireContext(), AuthenticationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onSuccessfullCheck() {
        Log.i(TAG, "Permissions check: success")
    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    private fun removeGeofences() {
        if (!foregroundAndBackgroundLocationPermissionApproved()) {
            return
        }
        geofencingClient.removeGeofences(geofencePendingIntent).run {
            addOnSuccessListener {
                Log.d(TAG, getString(R.string.geofences_removed))
                Toast.makeText(requireContext(), R.string.geofences_removed, Toast.LENGTH_SHORT)
                    .show()
            }
            addOnFailureListener {
                Log.d(TAG, getString(R.string.geofences_not_removed))
            }
        }
    }
}
