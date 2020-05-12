package com.thurman.foode.Utility

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

class AlertUtil {

    companion object {

        fun StandardAlert(title: String?, message: String, okButton: String, context: Context){
            val dialogBuilder = AlertDialog.Builder(context)
            dialogBuilder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(okButton, {
                        dialog, id -> {}
                })
            val alert = dialogBuilder.create()
            if (title != null){
                alert.setTitle(title)
            }
            alert.show()
        }

    }

}