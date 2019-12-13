package com.ygoular.notes.view.ui

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.snack(
    message: String, actionMessage: String = "", listener: View.OnClickListener? = null,
    indefinite: Boolean = false
) {
    val snackbar = Snackbar.make(
        this,
        message,
        if (indefinite)
            Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_LONG
    )

    if (actionMessage.isNotEmpty() && listener != null) {
        snackbar.setAction(actionMessage, listener)
    } else if (indefinite) {
        snackbar.setAction("OK") { snackbar.dismiss() }
    }

    snackbar.show()
}