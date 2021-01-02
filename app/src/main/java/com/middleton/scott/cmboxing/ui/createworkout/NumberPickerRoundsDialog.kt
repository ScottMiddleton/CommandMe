package com.middleton.scott.cmboxing.ui.createworkout

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import com.middleton.scott.cmboxing.R
import kotlinx.android.synthetic.main.dialog_number_picker_mins_secs.*
import kotlinx.android.synthetic.main.dialog_number_picker_mins_secs.cancel_btn
import kotlinx.android.synthetic.main.dialog_number_picker_mins_secs.save_btn
import kotlinx.android.synthetic.main.dialog_number_picker_mins_secs.title_tv
import kotlinx.android.synthetic.main.dialog_number_picker_rounds.*

class NumberPickerRoundsDialog(
    private val numberOfRounds: Int,
    private val onSave: ((Int) -> Unit),
    private val onCancel: (() -> Unit)
) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_number_picker_rounds, container)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title_tv.text = view.context.getString(R.string.rounds)
        rounds_np.maxValue = 99
        rounds_np.minValue = 1
        rounds_np.value = numberOfRounds
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )
        setClickListeners()
    }

    private fun setClickListeners() {
        save_btn.setOnClickListener {
            onSave(rounds_np.value)
            dismiss()
        }

        cancel_btn.setOnClickListener {
            onCancel()
            dismiss()
        }
    }
}