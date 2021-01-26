package com.middleton.scott.cmboxing.ui.createworkout

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.datasource.local.model.StructuredCommandCrossRef
import kotlinx.android.synthetic.main.dialog_add_round_commands.*

class PasteRoundDialog(
    private val audioFileBaseDirectory: String,
    private val currentNumberOfStructuredCrossRefs: Int,
    private val round: Int,
    val commands: List<Command>,
    private val onApply: ((List<StructuredCommandCrossRef>) -> Unit)
) : DialogFragment() {

    lateinit var adapter: AddRoundCommandsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        adapter = AddRoundCommandsAdapter(
            audioFileBaseDirectory,
            commands
        )

        return inflater.inflate(R.layout.dialog_paste_round, container)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        add_round_commands_RV.adapter = adapter
        setClickListeners()

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val back = ColorDrawable(Color.TRANSPARENT)
        val inset = InsetDrawable(back, 0, 240, 0, 240)
        dialog?.window?.setBackgroundDrawable(inset)

        round_tv.text = context?.getString(R.string.round) + " " + round.toString()
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