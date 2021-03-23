package com.middleton.scott.cmboxing.ui.createworkout

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import com.middleton.scott.cmboxing.MainActivity
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.datasource.local.model.StructuredCommandCrossRef
import kotlinx.android.synthetic.main.dialog_add_round_commands.*

class AddRoundCommandDialog(
    private val audioFileBaseDirectory: String,
    private val currentNumberOfStructuredCrossRefs: Int,
    private val round: Int,
    val commands: List<Command>,
    private val onApply: ((List<StructuredCommandCrossRef>) -> Unit)
) : DialogFragment() {

    lateinit var adapter: AddRoundCommandsAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val root = RelativeLayout(activity)
        // creating the fullscreen dialog
        val dialog = Dialog(MainActivity.instance)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(root)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        adapter = AddRoundCommandsAdapter(
            audioFileBaseDirectory,
            commands
        )

        return inflater.inflate(R.layout.dialog_add_round_commands, container)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        add_round_commands_RV.adapter = adapter
        setClickListeners()

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val back = ColorDrawable(Color.TRANSPARENT)
        val inset = InsetDrawable(back, 20, 40, 20, 40)
        dialog?.window?.setBackgroundDrawable(inset)

        round_tv.text = getString(R.string.round_number, round.toString())
    }

    private fun setClickListeners() {
        close_btn.setOnClickListener {
            dismiss()
        }

        confirm_btn.setOnClickListener {
            val structuredCommandCrossRefs: MutableList<StructuredCommandCrossRef> = arrayListOf()
            var index = currentNumberOfStructuredCrossRefs
            adapter.commandCountList.forEach { commandCount ->
                repeat(commandCount.count) {
                    structuredCommandCrossRefs.add(
                        StructuredCommandCrossRef(
                            -1,
                            commandCount.command.id,
                            round,
                            commandCount.command.timeToCompleteSecs,
                            index
                        )
                    )
                    index ++
                }
            }
            dismiss()
            onApply(structuredCommandCrossRefs)
        }
    }
}