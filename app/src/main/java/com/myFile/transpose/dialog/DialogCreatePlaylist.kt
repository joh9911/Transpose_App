package com.myFile.transpose.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.view.WindowManager
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.myFile.transpose.R


class DialogCreatePlaylist: DialogFragment() {
    private lateinit var listener: NoticeDialogListener
    lateinit var editTextTitle: EditText

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
            editTextTitle = dialogView.findViewById(R.id.title)
            editTextTitle.requestFocus()
//            editTextTitle.setOnKeyListener(object: View.OnKeyListener{
//                override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
//                    if (keyCode == event)
//                }
//
//            })
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder
                .setTitle("새 재생목록")
                .setView(dialogView)
                // Add action buttons
                .setPositiveButton(
                    R.string.dialog_create_text,
                    DialogInterface.OnClickListener { dialog, id ->
                        val text = editTextTitle.text
                        listener.onDialogPositiveClick(this, text)
                    })
                .setNegativeButton(
                    R.string.dialog_cancel_text,
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onDialogNegativeClick(this)
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    fun setListener(noticeDialogListener: NoticeDialogListener){
        this.listener = noticeDialogListener
    }

}