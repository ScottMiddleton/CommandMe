package com.middleton.scott.cmboxing

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
            R.id.splashScreen
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

            when(destination.id){
                R.id.randomWorkoutScreen -> {
                    menu?.setGroupVisible(R.id.workout_menu, true)
                    supportActionBar?.setDisplayShowCustomEnabled(false)
                    supportActionBar?.setDisplayShowTitleEnabled(true)
                }
                R.id.structuredWorkoutScreen -> {
                    menu?.setGroupVisible(R.id.workout_menu, true)
                    supportActionBar?.setDisplayShowCustomEnabled(false)
                    supportActionBar?.setDisplayShowTitleEnabled(true)
                }
                R.id.splashScreen -> {
                    supportActionBar?.hide()
                    menu?.setGroupVisible(R.id.workout_menu, false)
                    supportActionBar?.setDisplayShowCustomEnabled(false)
                    supportActionBar?.setDisplayShowTitleEnabled(true)
                }
                R.id.loginScreen -> {
                    supportActionBar?.hide()
                    menu?.setGroupVisible(R.id.workout_menu, false)
                    supportActionBar?.setDisplayShowCustomEnabled(false)
                    supportActionBar?.setDisplayShowTitleEnabled(true)
                }
                R.id.createAccountScreen -> {
                    supportActionBar?.hide()
                    menu?.setGroupVisible(R.id.workout_menu, false)
                    supportActionBar?.setDisplayShowCustomEnabled(false)
                    supportActionBar?.setDisplayShowTitleEnabled(true)
                }
                R.id.createWorkoutScreen -> {
                    menu?.setGroupVisible(R.id.workout_menu, false)
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    supportActionBar?.setDisplayShowCustomEnabled(true)
                    supportActionBar?.setDisplayShowTitleEnabled(false)
                    val view: View = layoutInflater.inflate(R.layout.appbar_create_workout, null)
                    supportActionBar?.customView = view
                    val parent: androidx.appcompat.widget.Toolbar = view.parent as androidx.appcompat.widget.Toolbar
                    parent.setContentInsetsAbsolute(0, 0)
                }
                R.id.myWorkoutsScreen -> {
                    menu?.setGroupVisible(R.id.workout_menu, false)
                    supportActionBar?.setDisplayShowCustomEnabled(false)
                    supportActionBar?.setDisplayShowTitleEnabled(true)
                    supportActionBar?.show()
                }
                R.id.commandsScreen -> {
                    menu?.setGroupVisible(R.id.workout_menu, false)
                    supportActionBar?.setDisplayShowCustomEnabled(false)
                    supportActionBar?.setDisplayShowTitleEnabled(true)
                }
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
