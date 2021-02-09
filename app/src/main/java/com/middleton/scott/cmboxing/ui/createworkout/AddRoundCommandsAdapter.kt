package com.middleton.scott.cmboxing.ui.createworkout

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.datasource.local.model.SelectedCommandCrossRef
import com.middleton.scott.cmboxing.datasource.local.model.StructuredCommandCrossRef
import com.middleton.scott.cmboxing.utils.customviews.CounterView
import kotlinx.android.synthetic.main.counter_layout.view.*
import java.io.IOException
import java.util.ArrayList

class AddRoundCommandsAdapter(
    private val audioFileDirectory: String,
    val commands: List<Command>
) : RecyclerView.Adapter<AddRoundCommandsAdapter.AddRoundCommandsViewHolder>() {

    lateinit var context: Context
    private var mediaPlayer = MediaPlayer()

    val commandCountList: MutableList<CommandCount> = arrayListOf()

    private var audioPlayingIndex = -1
    private var currentPlayingAudioLottie: LottieAnimationView? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddRoundCommandsViewHolder {
        context = parent.context
        return AddRoundCommandsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_add_round_command,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return commands.size
    }

    override fun onBindViewHolder(holder: AddRoundCommandsViewHolder, position: Int) {
        val command = commands[position]
        holder.nameTV.text = commands[position].name

        holder.countView.count_TV.doAfterTextChanged { editable ->
            val count = editable.toString().toInt()

            val commandCount = commandCountList.firstOrNull { it.command.id == command.id }

            if(commandCount != null){
                commandCount.count = count
            } else {
                commandCountList.add(CommandCount(command, count))
            }
        }

        val playAudioLottie = holder.playAudioLottie
        playAudioLottie.speed = 3f
        playAudioLottie.setMinAndMaxFrame(30, 60)
        playAudioLottie.setOnClickListener {
            if (!mediaPlayer.isPlaying || audioPlayingIndex != position) {
                handlePlayAnimationLottie(true, currentPlayingAudioLottie)
                handlePlayAnimationLottie(false, playAudioLottie)
                startPlaying(commands[position].file_name, playAudioLottie)
                currentPlayingAudioLottie = playAudioLottie
                audioPlayingIndex = position
            } else {
                stopPlaying(playAudioLottie)
            }
        }
    }

    class AddRoundCommandsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTV: TextView = view.findViewById(R.id.round_command_name_tv)
        val parent: ConstraintLayout = view.findViewById(R.id.parent_cl)
        val playAudioLottie: LottieAnimationView = view.findViewById(R.id.play_audio_lottie)
        val countView: CounterView = view.findViewById(R.id.round_command_count_cv)
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

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    data class CommandCount(var command: Command, var count: Int)
}