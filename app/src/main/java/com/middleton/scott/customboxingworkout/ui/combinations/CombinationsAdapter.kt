package com.middleton.scott.customboxingworkout.ui.combinations

import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import java.io.IOException
import java.util.*


class CombinationsAdapter(
    private val audioFileDirectory: String,
    private val combinations: List<Combination>,
    private val onPlayAudio: ((String) -> Unit),
    private val onCheckWorkout: ((Long) -> Unit)
) : RecyclerView.Adapter<CombinationsAdapter.CombinationsViewHolder>() {

    private var mediaPlayer = MediaPlayer()
    val timer = Timer()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CombinationsViewHolder {
        return CombinationsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.combination_list_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return combinations.size
    }

    override fun onBindViewHolder(holder: CombinationsViewHolder, position: Int) {
        holder.nameTV.text = combinations[position].name
        holder.parent.setOnClickListener {
            onCheckWorkout(combinations[position].id)
        }

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onCheckWorkout(combinations[position].id)
        }

        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioFileDirectory + combinations[position].file_name)
                prepare()
            } catch (e: IOException) {
                Log.e("LOG_TAG", "prepare() failed")
            }
        }

        holder.audioProgressBar.max = mediaPlayer.duration
        holder.playAudioBtn.setOnClickListener {
            startPlaying(combinations[position].file_name, holder.audioProgressBar)
        }

    }

    class CombinationsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTV: TextView = view.findViewById(R.id.combination_name_tv)
        val parent: ConstraintLayout = view.findViewById(R.id.parent_cl)
        val playAudioBtn: ImageButton = view.findViewById(R.id.play_audio_btn)
        val checkBox: CheckBox = view.findViewById(R.id.checkbox)
        val audioProgressBar: ProgressBar = view.findViewById(R.id.audio_playback_pb)
    }

    private fun startPlaying(fileName: String, audioProgressBar: ProgressBar) {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioFileDirectory + fileName)
                prepare()
                timer.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        Runnable { audioProgressBar.progress = mediaPlayer.currentPosition }
                    }
                }, 0, 100)
                start()
            } catch (e: IOException) {
                Log.e("LOG_TAG", "prepare() failed")
            }
        }
    }
}