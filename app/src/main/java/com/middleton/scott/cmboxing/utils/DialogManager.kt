package com.middleton.scott.cmboxing.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.middleton.scott.cmboxing.R


object DialogManager {
    fun showDialog(
        context: Context,
        titleId: Int? = null,
        messageId: Int? = null,
        positiveBtnTextId: Int = android.R.string.ok,
        positiveBtnClick: (() -> Unit)? = null,
        negativeBtnTextId: Int = android.R.string.cancel,
        negativeBtnClick: (() -> Unit)? = null
    ) {

        val builder = MaterialAlertDialogBuilder(context)
        val dialogView: View =
            LayoutInflater.from(context).inflate(R.layout.alert_dialog_with_divider_layout, null)
        builder.setView(dialogView)

        titleId?.let {
            val titleTV = dialogView.findViewById<TextView>(R.id.title_tv)
            titleTV.text = context.getText(it)
            titleTV.visibility = VISIBLE
        }

        messageId?.let {
            val messageTV = dialogView.findViewById<TextView>(R.id.message_tv)
            messageTV.text = context.getText(it)
            messageTV.visibility = VISIBLE
        }

        val positiveBtn = dialogView.findViewById<TextView>(R.id.positive_btn)
        val negativeBtn = dialogView.findViewById<TextView>(R.id.negative_btn)

        negativeBtn.text = context.getString(negativeBtnTextId)

        val alertDialog = builder.create()
        val back = ColorDrawable(Color.TRANSPARENT)
        val inset = InsetDrawable(back, 80)
        alertDialog.window?.setBackgroundDrawable(inset)

        if (positiveBtnClick == null) {
            positiveBtn.visibility = GONE
        } else {
            positiveBtn.visibility = VISIBLE
            positiveBtn.text = context.getString(positiveBtnTextId)
            positiveBtn.setOnClickListener {
                alertDialog.dismiss()
                positiveBtnClick()
            }
        }

        if (negativeBtnClick == null) {
            negativeBtn.visibility = GONE
        } else {
            negativeBtn.visibility = VISIBLE
            negativeBtn.setOnClickListener {
                alertDialog.dismiss()
                negativeBtnClick()
            }
        }

        alertDialog.show()
    }
}