package com.example.customboxingworkout

import android.app.ActionBar
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.volume_seekbar_layout.*


class MainActivity : AppCompatActivity() {
    private val closeBtnDestinations =
        setOf(R.id.navigation_workouts)
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_quickstart, R.id.navigation_workouts, R.id.navigation_stats
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        actionBar?.setCustomView(R.layout.volume_seekbar_layout)
        setupVolumeSlider()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (closeBtnDestinations.contains(navController.currentDestination?.id)) {
            menu?.removeItem(R.id.privacy_policy)
            menu?.removeItem(R.id.app_license)
            menu?.removeItem(R.id.terms_and_conditions)
            menu?.findItem(R.id.close_btn)?.isVisible = true
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
//            R.id.handbook -> {
//                navController.navigate(
//                    R.id.pdfPreviewScreen,
//                    bundleOf("assetName" to "Handbook_08052020.pdf")
//                )
//                true
//            }
            R.id.close_btn -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupVolumeSlider() {
        // Control the media volume

        // Control the media volume
        volumeControlStream = AudioManager.STREAM_MUSIC
        // Initialize the AudioManager
        // Initialize the AudioManager
        val mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Inflate the custom ActionBar View

        // Inflate the custom ActionBar View
        val view: View = layoutInflater.inflate(R.layout.volume_seekbar_layout, null)
        // Set the max range of the SeekBar to the max volume stream type
        // Set the max range of the SeekBar to the max volume stream type
        volume_seekbar.max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        // Bind the OnSeekBarChangeListener

        volume_seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Apply the custom View to the ActionBar

        // Apply the custom View to the ActionBar
        actionBar!!.setCustomView(view, ActionBar.LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }
}
