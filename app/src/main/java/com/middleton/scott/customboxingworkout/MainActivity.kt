package com.middleton.scott.customboxingworkout

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.other.Constants.ACTION_SHOW_WORKOUT_SCREEN
import com.middleton.scott.customboxingworkout.utils.DialogManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var menu: Menu? = null

    companion object {
        var currentWorkoutId = -1L
    }

    private lateinit var navController: NavController
    private val topLevelDestinations =
        setOf(R.id.combinationsScreen, R.id.workoutsScreen)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigateToWorkoutScreenIfNeeded(intent)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            topLevelDestinations
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.setDisplayShowCustomEnabled(false)
            if (!topLevelDestinations.contains(destination.id)) {
                nav_view.visibility = GONE
            } else {
                nav_view.visibility = VISIBLE
            }

            if (destination.id == R.id.createWorkoutScreen) {
                (this as AppCompatActivity).supportActionBar?.hide()
            } else {
                (this as AppCompatActivity).supportActionBar?.show()
            }

            if (destination.id == R.id.workoutScreen) {
                menu?.setGroupVisible(R.id.workout_menu, true)
            } else {
                menu?.setGroupVisible(R.id.workout_menu, false)
            }
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
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
            R.id.home -> {
                onBackPressed()
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun navigateToWorkoutScreenIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_WORKOUT_SCREEN) {
            nav_host_fragment.findNavController().navigate(R.id.action_global_workout_screen)
        }
    }
}
