package com.middleton.scott.cmboxing.ui.commands

import SaveCombinationDialog
import android.content.Context
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
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.datasource.local.model.SelectedCommandCrossRef
import java.io.IOException

class CommandsAdapter(
    private val audioFileDirectory: String,
    private val fragmentManager: FragmentManager,
    private val onCheckCombination: ((selectedCombinationCrossRef: SelectedCommandCrossRef, isChecked: Boolean) -> Unit)? = null,
    private val onEditCombination: ((Command) -> Unit),
    private val onDeleteCombination: ((Command) -> Unit)
) : RecyclerView.Adapter<CommandsAdapter.CombinationsViewHolder>() {

    lateinit var context: Context

    var selectedCombinations = mutableListOf<Command>()
    private var allCombinations = mutableListOf<Command>()
    private var mediaPlayer = MediaPlayer()

    private var audioPlayingIndex = -1
    private var currentPlayingAudioLottie: LottieAnimationView? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CombinationsViewHolder {
        context = parent.context
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
        val combination = allCombinations[position]
        holder.nameTV.text = allCombinations[position].name

        val selectedCombination = selectedCombinations.firstOrNull {
            it.id == combination.id
        }

        val isChecked = selectedCombination != null

        holder.checkBox.isChecked = isChecked

        if (onCheckCombination == null) {
            holder.checkBox.visibility = GONE
        } else {
            holder.checkBox.visibility = VISIBLE
            holder.checkBox.setOnCheckedChangeListener { _, checked ->
                onCheckCombination.invoke(
                    SelectedCommandCrossRef(workout_id = -1, command_id = combination.id),
                    checked
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

        holder.editButton.setOnClickListener {
            SaveCombinationDialog(
                (audioFileDirectory + combination.file_name),
                true,
                combination,
                { combination ->
                    onEditCombination(combination)
                }, {
                    onDeleteCombination(combination)
                }).show(fragmentManager, "")
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
        allCommands: List<Command>,
        selectedCommands: List<Command>?
    ) {
        this.allCombinations = allCommands as MutableList<Command>
        selectedCommands?.let {
            this.selectedCombinations = selectedCommands as MutableList<Command>
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