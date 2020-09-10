package com.middleton.scott.customboxingworkout.ui.combinations

import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutCombinations
import java.io.IOException

class CombinationsAdapter(
    private val audioFileDirectory: String,
    private val workoutId: Long = -1,
    private val onCheckWorkout: ((workoutCombination: WorkoutCombinations, isChecked: Boolean) -> Unit)? = null
) : RecyclerView.Adapter<CombinationsAdapter.CombinationsViewHolder>() {

    private var workoutCombinations = mutableListOf<WorkoutCombinations>()
    private var allCombinations = mutableListOf<Combination>()
    private var mediaPlayer = MediaPlayer()

    private var audioPlayingIndex = -1
    private var currentPlayingAudioLottie: LottieAnimationView? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CombinationsViewHolder {
        return CombinationsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_combination,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return allCombinations.size
    }

    override fun onBindViewHolder(holder: CombinationsViewHolder, position: Int) {
        holder.nameTV.text = allCombinations[position].name

        var isChecked = false

        val workoutCombination = workoutCombinations.filter {
            it.combination_id == allCombinations[position].id
        }.firstOrNull()

        isChecked = workoutCombination != null

        holder.checkBox.isChecked = isChecked

        if (onCheckWorkout == null) {
            holder.checkBox.visibility = GONE
            holder.editButton.visibility = VISIBLE
        } else {
            holder.checkBox.visibility = VISIBLE
            holder.editButton.visibility = GONE
            holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                onCheckWorkout.invoke(
                    workoutCombination ?: WorkoutCombinations(
                        workoutId,
                        allCombinations[position].id
                    ), isChecked
                )
            }
        }

        val playAudioLottie = holder.playAudioLottie
        playAudioLottie.speed = 3f
        playAudioLottie.setMinAndMaxFrame(30, 60)
        playAudioLottie.setOnClickListener {
            if (!mediaPlayer.isPlaying || audioPlayingIndex != position) {
                handlePlayAnimationLottie(true, currentPlayingAudioLottie)
                handlePlayAnimationLottie(false, playAudioLottie)
                startPlaying(allCombinations[position].file_name, playAudioLottie)
                currentPlayingAudioLottie = playAudioLottie
                audioPlayingIndex = position
            } else {
                stopPlaying(playAudioLottie)
            }
        }
    }

    class CombinationsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTV: TextView = view.findViewById(R.id.combination_name_tv)
        val parent: ConstraintLayout = view.findViewById(R.id.parent_cl)
        val playAudioLottie: LottieAnimationView = view.findViewById(R.id.play_audio_lottie)
        val checkBox: CheckBox = view.findViewById(R.id.checkbox)
        val editButton: ImageButton = view.findViewById(R.id.edit_btn)
    }

    private fun startPlaying(fileName: String, playAudioLottie: LottieAnimationView) {
        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioFileDirectory + fileName)
                prepare()
                this.setOnCompletionListener {
                    audioPlayingIndex = -1
                    currentPlayingAudioLottie = null
                    handlePlayAnimationLottie(true, playAudioLottie)
                }
                start()
            } catch (e: IOException) {
                Log.e("LOG_TAG", "prepare() failed")
            }
        }
    }

    private fun stopPlaying(playAudioLottie: LottieAnimationView) {
        audioPlayingIndex = -1
        currentPlayingAudioLottie = null
        handlePlayAnimationLottie(true, playAudioLottie)
        mediaPlayer.stop()
        mediaPlayer.reset()
    }

    private fun handlePlayAnimationLottie(
        playInReverse: Boolean,
        playAudioLottie: LottieAnimationView?
    ) {
        if (playInReverse) {
            playAudioLottie?.setMinAndMaxFrame(0, 30)
            playAudioLottie?.playAnimation()
        } else {
            playAudioLottie?.setMinAndMaxFrame(30, 60)
            playAudioLottie?.playAnimation()
        }
    }

    fun setAdapter(
        allCombinations: List<Combination>,
        workoutCombinations: List<WorkoutCombinations>?
    ) {
        this.allCombinations = allCombinations as MutableList<Combination>
        workoutCombinations?.let {
            this.workoutCombinations = workoutCombinations as MutableList<WorkoutCombinations>
        }
        this.notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}