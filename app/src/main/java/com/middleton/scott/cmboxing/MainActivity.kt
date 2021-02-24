package com.middleton.scott.cmboxing

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.crashlytics.internal.common.CommonUtils.hideKeyboard
import com.middleton.scott.cmboxing.other.Constants.ACTION_SHOW_WORKOUT_SCREEN
import com.middleton.scott.cmboxing.utils.DialogManager
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    var menu: Menu? = null
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val viewModel: MainViewModel by viewModel()

    companion object {
        var currentWorkoutId = -1L
        lateinit var instance: MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        setContentView(R.layout.activity_main)
        setupNavigationMenu()
        navigateToWorkoutScreenIfNeeded(intent)
    }

    private fun setupNavigationMenu() {
        val topLevelMenuDestinations = setOf(
            R.id.myWorkoutsScreen,
            R.id.commandsScreen,
            R.id.packsScreen,
        )

        val menuDestinations = mutableSetOf<Int>()
        menuDestinations.addAll(topLevelMenuDestinations)

        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setIcon(R.drawable.ic_menu)

        val navView: NavigationView = findViewById(R.id.nav_view)
        appBarConfiguration = AppBarConfiguration(menuDestinations, drawer_layout)
        navController = findNavController(R.id.nav_host_fragment)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.findViewById<TextView>(R.id.logout_tv).setOnClickListener {
            viewModel.logout()
            nav_host_fragment.findNavController().navigate(R.id.action_global_login_screen)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            setupMenuVisibility(topLevelMenuDestinations.contains(destination.id))

            if (destination.id == R.id.randomWorkoutScreen || destination.id == R.id.structuredWorkoutScreen) {
                menu?.setGroupVisible(R.id.workout_menu, true)
            } else {
                menu?.setGroupVisible(R.id.workout_menu, false)
            }

            if (destination.id == R.id.splashScreen || destination.id == R.id.loginScreen || destination.id == R.id.createAccountScreen || destination.id == R.id.createWorkoutScreen) {
                supportActionBar?.hide()
            } else {
                supportActionBar?.show()
            }
        }

        drawer_layout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerOpened(drawerView: View) {
                hideKeyboard(this@MainActivity, drawer_layout)
            }
        })
    }

    private fun setupMenuVisibility(enableMenu: Boolean) {
        if (enableMenu) {
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        } else {
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.options_menu, menu)
        menu?.setGroupVisible(R.id.workout_menu, false)
        this.menu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_workout -> {
                DialogManager.showDialog(
                    this,
                    R.string.edit_this_workout,
                    R.string.edit_workout_dialog_message,
                    R.string.cancel,
                    { },
                    R.string.edit_workout,
                    {
                        navController.popBackStack()
                        navController.navigate(
                            R.id.createWorkoutScreen,
                            bundleOf("workoutId" to currentWorkoutId)
                        )
                    })
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp() = NavigationUI.navigateUp(navController, appBarConfiguration)

    private fun navigateToWorkoutScreenIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_WORKOUT_SCREEN) {
            nav_host_fragment.findNavController().navigate(R.id.action_global_workout_screen)
        }
    }
}
