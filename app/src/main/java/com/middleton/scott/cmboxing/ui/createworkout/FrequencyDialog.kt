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
import com.middleton.scott.cmboxing.datasource.local.enums.CommandFrequencyType
import kotlinx.android.synthetic.main.dialog_frequency.*

class FrequencyDialog(
    private val onSelected: ((CommandFrequencyType) -> Unit)
) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_frequency, container)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )

        setClickListeners()
    }

    private fun setClickListeners() {
        very_often_tv.setOnClickListener {
            onSelected.invoke(CommandFrequencyType.VERY_OFTEN)
            dismiss()
        }

        often_tv.setOnClickListener {
            onSelected.invoke(CommandFrequencyType.OFTEN)
            dismiss()
        }

        average_tv.setOnClickListener {
            onSelected.invoke(CommandFrequencyType.AVERAGE)
            dismiss()
        }

        rare_tv.setOnClickListener {
            onSelected.invoke(CommandFrequencyType.RARE)
            dismiss()
        }

        very_rare_tv.setOnClickListener {
            onSelected.invoke(CommandFrequencyType.VERY_RARE)
            dismiss()
        }
    }
}