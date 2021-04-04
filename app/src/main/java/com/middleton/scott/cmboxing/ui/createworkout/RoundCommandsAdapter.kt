package com.middleton.scott.cmboxing.ui.createworkout

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.datasource.local.model.StructuredCommandCrossRef
import com.middleton.scott.cmboxing.utils.DateTimeUtils
import java.io.IOException
import java.util.*


class RoundCommandsAdapter(
    private val audioFileDirectory: String,
    val commands: List<Command>,
    private val structuredCommandCrossRefs: List<StructuredCommandCrossRef>,
    val onPositionsChanged: ((List<StructuredCommandCrossRef>) -> Unit),
    val onCommandDeleted: ((StructuredCommandCrossRef)) -> Unit
) : RecyclerView.Adapter<RoundCommandsAdapter.RoundCommandViewHolder>() {

    private var mediaPlayer = MediaPlayer()
    private var context: Context? = null

    private var audioPlayingIndex = -1
    private var currentPlayingAudioLottie: LottieAnimationView? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoundCommandViewHolder {
        context = parent.context
        return RoundCommandViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_round_command,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return structuredCommandCrossRefs.size
    }

    override fun onBindViewHolder(holder: RoundCommandViewHolder, position: Int) {
        val crossRef = structuredCommandCrossRefs[position]
        val currentCommand = commands.firstOrNull { crossRef.command_id == it.id }

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

        currentCommand?.let { command ->
            holder.timeTV.text = DateTimeUtils.toMinuteSeconds(crossRef.time_allocated_secs)

            holder.timeTV.setOnClickListener {
                context?.getString(R.string.time_allocated)?.let { it1 ->
                    NumberPickerMinutesSecondsDialog(
                        it1,
                        crossRef.time_allocated_secs,
                        { newSecs ->
                            command.timeToCompleteSecs = newSecs
                            crossRef.time_allocated_secs = newSecs
                            onPositionsChanged(structuredCommandCrossRefs)
                            holder.timeTV.text = DateTimeUtils.toMinuteSeconds(newSecs)
                        },
                        {}, command.file_name).show((context as AppCompatActivity).supportFragmentManager, null)
                }
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
            context?.let { ContextCompat.getDrawable(it, R.drawable.rounded_stroke_background_highighted) }
    }

    fun setBackgroundUnselected(viewHolder: RoundCommandViewHolder) {
        context?.let {
            viewHolder.parent.background =
                ContextCompat.getDrawable(it, R.drawable.rounded_stroke_background)
        }
    }

    class RoundCommandViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val parent: ConstraintLayout = view.findViewById(R.id.round_command_parent_cl)
        val commandNameTV: TextView = view.findViewById(R.id.command_name_tv)
        val playAudioLottie: LottieAnimationView = view.findViewById(R.id.play_audio_lottie)
        val timeTV: TextView = view.findViewById(R.id.time_allocated_tv)
    }

    fun onPositionChanged(startPosition: Int, endPosition: Int) {
        notifyItemMoved(
            startPosition,
            endPosition
        )
        Collections.swap(structuredCommandCrossRefs, startPosition, endPosition)
    }

    fun onPositionsChanged() {
        onPositionsChanged(structuredCommandCrossRefs)
    }

    fun onDelete(position: Int){
        onCommandDeleted(structuredCommandCrossRefs[position])
    }
}