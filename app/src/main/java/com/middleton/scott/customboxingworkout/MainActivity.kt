package com.middleton.scott.customboxingworkout

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.middleton.scott.commandMeBoxing.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private val topLevelDestinations =
        setOf(R.id.combinationsScreen, R.id.workoutsScreen, R.id.statsScreen)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.workoutsScreen, R.id.combinationsScreen, R.id.statsScreen
            )
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (!topLevelDestinations.contains(destination.id)) {
                nav_view.visibility = GONE
            } else {
                nav_view.visibility = VISIBLE
            }
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.options_menu, menu)
        return true
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

    private fun setupCustomAppBar() {
        // Control the media volume
        volumeControlStream = AudioManager.STREAM_MUSIC
        // Initialize the AudioManager
        val mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val view: View = layoutInflater.inflate(R.layout.title_bar_with_volume_slider_layout, null)
//        val mVolumeControls = view.findViewById<View>(R.id.volume_SB) as SeekBar
//        // Set the max range of the SeekBar to the max volume stream type
//        mVolumeControls.max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
//        // Bind the OnSeekBarChangeListener
//
//        mVolumeControls.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//        })

//        view.layoutParams = LinearLayout.LayoutParams(
//            FrameLayout.LayoutParams.MATCH_PARENT,
//            FrameLayout.LayoutParams.MATCH_PARENT
//        )

        // Apply the custom View to the ActionBar
        supportActionBar?.customView = view
        supportActionBar?.setDisplayShowCustomEnabled(true)
    }
}
