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
import kotlinx.android.synthetic.main.dialog_paste_round.*

class PasteRoundDialog(
    private val copiedRound: Int,
    private val roundCount: Int,
    private val onPaste: ((roundsToPaste: List<Int>) -> Unit)
) : DialogFragment() {

    lateinit var adapter: PasteRoundsAdapter

    private var roundsToPaste = mutableListOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val rounds = mutableListOf<Int>()

        repeat(roundCount) {
            rounds.add(it + 1)
        }

        rounds.remove(copiedRound)

        adapter = PasteRoundsAdapter(rounds) {
            roundsToPaste = it
        }

        return inflater.inflate(R.layout.dialog_paste_round, container)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        paste_round_RV.adapter = adapter
        setClickListeners()

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val back = ColorDrawable(Color.TRANSPARENT)
        val inset = InsetDrawable(back, 0, 240, 0, 240)
        dialog?.window?.setBackgroundDrawable(inset)

        round_tv.text = view.context.getString(R.string.round_copied, copiedRound.toString())

        paste_btn.setOnClickListener {
            onPaste(roundsToPaste)
            dismiss()
        }

        select_all_cb.setOnCheckedChangeListener { _, isChecked ->
            adapter.selectAllChecked(isChecked)
        }
    }

    private fun setClickListeners() {
        close_btn.setOnClickListener {
            dismiss()
        }

        paste_btn.setOnClickListener {
        }
    }
}