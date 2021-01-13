package com.middleton.scott.cmboxing.ui.createworkout

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.datasource.local.model.StructuredCommandCrossRef
import java.io.IOException
import java.util.*

class RoundCommandsAdapter(
    private val context: Context,
    private val audioFileDirectory: String,
    val commands: List<Command>,
    private val structuredCombinationCrossRefs: List<StructuredCommandCrossRef>,
    private val onEditStructuredCommandCrossRef: ((StructuredCommandCrossRef) -> Unit)
) : RecyclerView.Adapter<RoundCommandsAdapter.RoundCommandViewHolder>() {

    private var mediaPlayer = MediaPlayer()

    private var audioPlayingIndex = -1
    private var currentPlayingAudioLottie: LottieAnimationView? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoundCommandViewHolder {
        return RoundCommandViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_round_command,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return structuredCombinationCrossRefs.size
    }

    override fun onBindViewHolder(holder: RoundCommandViewHolder, position: Int) {
        val currentCommand =
            commands.firstOrNull { structuredCombinationCrossRefs[position].command_id == it.id }

        holder.commandNameTV.text = currentCommand?.name

        val playAudioLottie = holder.playAudioLottie
        playAudioLottie.speed = 3f
        playAudioLottie.setMinAndMaxFrame(30, 60)
        playAudioLottie.setOnClickListener {
            if (!mediaPlayer.isPlaying || audioPlayingIndex != position) {
                handlePlayAnimationLottie(true, currentPlayingAudioLottie)
                handlePlayAnimationLottie(false, playAudioLottie)
                currentCommand?.file_name?.let { fileName ->
                    startPlaying(
                        fileName,
                        playAudioLottie
                    )
                }
                currentPlayingAudioLottie = playAudioLottie
                audioPlayingIndex = position
            } else {
                stopPlaying(playAudioLottie)
            }
        }
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

    fun setBackgroundSelected(viewHolder: RoundCommandViewHolder) {
        viewHolder.parent.background =
            ContextCompat.getDrawable(context, R.drawable.rounded_stroke_background_highighted)
    }

    fun setBackgroundUnselected(viewHolder: RoundCommandViewHolder) {
        viewHolder.parent.background =
            ContextCompat.getDrawable(context, R.drawable.rounded_stroke_background)
    }

    class RoundCommandViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val parent: ConstraintLayout = view.findViewById(R.id.round_command_parent_cl)
        val commandNameTV: TextView = view.findViewById(R.id.command_name_tv)
        val playAudioLottie: LottieAnimationView = view.findViewById(R.id.play_audio_lottie)
    }

    fun onPositionChanged(startPosition: Int, endPosition: Int) {
        notifyItemMoved(
            startPosition,
            endPosition
        )
        val startCrossRef = structuredCombinationCrossRefs[startPosition]
        val endCrossRef = structuredCombinationCrossRefs[endPosition]

        startCrossRef.position_index = endPosition
        endCrossRef.position_index = startPosition

        onEditStructuredCommandCrossRef(startCrossRef)
        onEditStructuredCommandCrossRef(endCrossRef)

        Collections.swap(structuredCombinationCrossRefs, startPosition, endPosition)
    }
}