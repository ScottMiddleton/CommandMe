package com.middleton.scott.cmboxing.billing

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.*
import android.widget.RelativeLayout
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import com.middleton.scott.cmboxing.MainActivity
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val root = RelativeLayout(activity)
        // creating the fullscreen dialog
        val dialog = Dialog(MainActivity.instance)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(root)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return dialog
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )

        dialog?.setCanceledOnTouchOutside(true)

        val back = ColorDrawable(Color.TRANSPARENT)
        val inset = InsetDrawable(back, 20, 40, 20, 40)
        dialog?.window?.setBackgroundDrawable(inset)

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