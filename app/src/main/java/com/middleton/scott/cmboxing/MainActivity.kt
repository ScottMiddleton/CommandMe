package com.middleton.scott.cmboxing

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.google.android.material.navigation.NavigationView
import com.middleton.scott.cmboxing.billing.PurchasePremiumDialog
import com.middleton.scott.cmboxing.other.Constants.ACTION_SHOW_WORKOUT_SCREEN
import com.middleton.scott.cmboxing.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.menu_header.*
import kotlinx.android.synthetic.main.menu_header.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {
    var menu: Menu? = null
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val viewModel: MainViewModel by viewModel()

    private val purchaseUpdateListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            Log.v("TAG_INAPP", "billingResult responseCode : ${billingResult.responseCode}")

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    handleNonConsumablePurchase(purchase)
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
            } else {
                // Handle any other error codes.
            }
        }

    companion object {
        var currentWorkoutId = -1L
        lateinit var instance: MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpBillingClient(purchaseUpdateListener)
        instance = this
        setContentView(R.layout.activity_main)
        setupNavigationMenu()
        navigateToWorkoutScreenIfNeeded(intent)

        viewModel.getUserLD().observe(this, Observer {
            if (it != null) {
                if (it.hasPurchasedUnlimitedCommands) {
//                    showPremiumMenuItem(false)
//                    nav_view.getHeaderView(0).premium_tv.visibility = VISIBLE
                } else {
//                    showPremiumMenuItem(true)
//                    nav_view.getHeaderView(0).premium_tv.visibility = GONE
                }
            }
        })
    }

    fun handleNonConsumablePurchase(purchase: Purchase) {
        Log.v("TAG_INAPP", "handlePurchase : $purchase")
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken).build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    val billingResponseCode = billingResult.responseCode
                    val billingDebugMessage = billingResult.debugMessage

                    Log.v("TAG_INAPP", "response code: $billingResponseCode")
                    Log.v("TAG_INAPP", "debugMessage : $billingDebugMessage")

                    viewModel.updateUserPurchasedUnlimitedCommands()
                }
            }
        }
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

            when (destination.id) {
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
                    val parent: androidx.appcompat.widget.Toolbar =
                        view.parent as androidx.appcompat.widget.Toolbar
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

//            nav_view.menu.findItem(R.id.premium)
//                .setOnMenuItemClickListener {
//                    startConnectionForProducts {
//                        for (skuDetails in it) {
//                            Log.v("TAG_INAPP", "skuDetailsList : $it")
//                            //This list should contain the products added above
//
//                            // Had to add this to remove random new line google was adding
//                            val description = skuDetails.description.replace(" \n", " ")
//                            PurchasePremiumDialog(skuDetails.title, description) {
//                                launchBillingFlow(skuDetails)
//                            }.show(
//                                supportFragmentManager,
//                                ""
//                            )
//                        }
//                    }
//                    drawer_layout.closeDrawer(GravityCompat.START)
//                    true
//                }
        }

        drawer_layout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerOpened(drawerView: View) {
                hideKeyboard(drawer_layout)
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

//    private fun showPremiumMenuItem(show: Boolean) {
//        nav_view.menu.findItem(R.id.premium).isVisible = show
//    }

    override fun onDestroy() {
        billingClient.endConnection()
        super.onDestroy()
    }
}
