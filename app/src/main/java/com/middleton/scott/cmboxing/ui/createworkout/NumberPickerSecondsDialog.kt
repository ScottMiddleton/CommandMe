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
import kotlinx.android.synthetic.main.dialog_number_picker_secs.*
import kotlinx.android.synthetic.main.dialog_save_command.save_btn

class NumberPickerSecondsDialog(
    private val millis: Long = 2000,
    private val onSave: ((millis: Long) -> Unit),
    private val onCancel: (() -> Unit)
) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_number_picker_secs, container)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        secs_np.maxValue = 59
        secs_np.minValue = 0

        val decimalValues = arrayOf("0", "5")
        decimal_np.maxValue = 1
        decimal_np.minValue = 0
        decimal_np.wrapSelectorWheel = true

        decimal_np.displayedValues = decimalValues

        secs_np.value = (millis / 1000).toInt()

        if(millis % 1000 == 500L){
            decimal_np.value = 1
        }

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )
        setClickListeners()
    }

    private fun setClickListeners() {
        save_btn.setOnClickListener {
            var millis = secs_np.value * 1000L
            val decimalIndexValue: Int = decimal_np.value

            if(decimalIndexValue == 1) {
                millis += 500
            }

            onSave(millis)
            dismiss()
        }

        cancel_btn.setOnClickListener {
            onCancel()
            dismiss()
        }
    }
}