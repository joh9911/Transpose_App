package com.myFile.Transpose

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.widget.EditText
import androidx.fragment.app.DialogFragment

class MyPlaylistDialogFragment: DialogFragment() {
    private lateinit var listener: NoticeDialogListener

    interface NoticeDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment, text: Editable?)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;
            val dialogView = inflater.inflate(R.layout.dialog_my_playlist, null)
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder
                .setTitle("새 재생목록")
                .setView(dialogView)
                // Add action buttons
                .setPositiveButton(R.string.dialog_create_text,
                    DialogInterface.OnClickListener { dialog, id ->
                        val text = dialogView.findViewById<EditText>(R.id.title).text
                        listener.onDialogPositiveClick(this, text)
                    })
                .setNegativeButton(R.string.dialog_cancel_text,
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onDialogNegativeClick(this)
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun setListener(noticeDialogListener: NoticeDialogListener){
        this.listener = noticeDialogListener
    }
}