package com.middleton.scott.customboxingworkout.ui.combinations

import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import java.io.IOException

class CombinationsAdapter(
    private val audioFileDirectory: String,
    private val onCheckWorkout: ((Combination: Combination, checked: Boolean) -> Unit)? = null
) : RecyclerView.Adapter<CombinationsAdapter.CombinationsViewHolder>() {

    private var checkedCombinations = emptyList<Combination>()
    private var allCombinations = mutableListOf<Combination>()
    private var mediaPlayer = MediaPlayer()

    private var audioPlayingIndex = -1
    private var currentPlayingAudioLottie: LottieAnimationView? = null

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
        return allCombinations.size
    }

    override fun onBindViewHolder(holder: CombinationsViewHolder, position: Int) {
        holder.nameTV.text = allCombinations[position].name
        holder.checkBox.isChecked = checkedCombinations.contains(allCombinations[position])

        if (onCheckWorkout == null) {
            holder.checkBox.visibility = GONE
        } else {
            holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                onCheckWorkout.invoke(allCombinations[position], isChecked)
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

    fun setCheckedCombinations(combinations: List<Combination>) {
        checkedCombinations = combinations
        this.notifyDataSetChanged()
    }

    fun setAdapter(combinations: List<Combination>) {
        this.allCombinations.clear()
        this.allCombinations.addAll(combinations)
        this.notifyDataSetChanged()
    }
}