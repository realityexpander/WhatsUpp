package com.realityexpander.whatsupp.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.realityexpander.whatsupp.databinding.DialogConfirmBinding

fun AppCompatActivity.confirmDialog(
    context: Context,
    dialogMessage: String,
    userId: String = "",
    positiveAction: (userId: String) -> Unit,
) {

    val dialog = BottomSheetDialog(context)
    val bindDialog = DialogConfirmBinding.inflate(
        LayoutInflater.from(context),
        null,
        false
    )

    bindDialog.dialogMessageTv.text = dialogMessage

    dialog.setCancelable(true)
    bindDialog.closeIv.setOnClickListener {
        dialog.dismiss()
        (bindDialog.root.parent as ViewGroup).removeView(bindDialog.root)
    }
    bindDialog.positiveActionTv.setOnClickListener {
        dialog.dismiss()
        (bindDialog.root.parent as ViewGroup).removeView(bindDialog.root)

        positiveAction(userId)
    }
    bindDialog.negativeActionTv.setOnClickListener {
        dialog.dismiss()
        (bindDialog.root.parent as ViewGroup).removeView(bindDialog.root)
    }

    dialog.setContentView(bindDialog.root)
    dialog.show()
}
