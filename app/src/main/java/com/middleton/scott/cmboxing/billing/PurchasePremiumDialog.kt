package com.middleton.scott.cmboxing.billing

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
import kotlinx.android.synthetic.main.dialog_premium_purchase.*

class PurchasePremiumDialog(
    private val title: String, private val description: String, private val onBuy: (() -> Unit),
) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_premium_purchase, container)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )

        dialog?.setCanceledOnTouchOutside(true)

        setClickListeners()

        title_tv.text = title
        description_tv.text = description
    }

    private fun setClickListeners() {
        buy_tv.setOnClickListener {
            onBuy()
            dismiss()
        }

        close_btn.setOnClickListener {
            dismiss()
        }
    }
}