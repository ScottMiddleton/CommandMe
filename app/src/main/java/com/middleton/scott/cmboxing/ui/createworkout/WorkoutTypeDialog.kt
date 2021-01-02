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
import kotlinx.android.synthetic.main.dialog_workout_type.*

class WorkoutTypeDialog(
    private val onChoice: ((CreateWorkoutType) -> Unit)
) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_workout_type, container)
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
        custom_btn.setOnClickListener {
            onChoice(CreateWorkoutType.CUSTOM)
            dismiss()
        }

        packs_btn.setOnClickListener {
            onChoice(CreateWorkoutType.PACKS)
            dismiss()
        }
    }
}