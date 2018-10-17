/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.components

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText

import com.dertyp7214.appstore.R

class InputDialog(title: String, text: String, hint: String, private val context: Context) {
    private var listener: Listener? = null
    private val builder: AlertDialog.Builder = AlertDialog.Builder(context)

    init {
        builder.setTitle(title)

        val input = EditText(context)
        input.hint = hint
        input.setText(text)
        input.maxLines = 1
        builder.setView(input)

        builder.setPositiveButton(context.getString(R.string.popup_ok)) { dialog, _ ->
            listener!!.onSubmit(input.text.toString())
            dialog.cancel()
        }
        builder.setNegativeButton(context.getString(R.string.popup_close)
        ) { dialog, _ -> dialog.cancel() }
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    fun show() {
        builder.show()
    }

    interface Listener {
        fun onSubmit(text: String)
        fun onCancel()
    }
}