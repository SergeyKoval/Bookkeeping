package by.bk.bookkeeper.android.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import by.bk.bookkeeper.android.R

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/

class LogoutConfirmationDialog : DialogFragment() {

    interface OnLogoutConfirmedListener {
        fun onLogoutConfirmed()
    }

    private var listener: OnLogoutConfirmedListener? = null

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = activity?.run {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.dialog_logout_title)
        builder.setMessage(R.string.dialog_logout_message)
        builder.setPositiveButton(R.string.btn_confirm) { _, _ -> listener?.onLogoutConfirmed() }
        builder.setNegativeButton(R.string.btn_cancel) { _, _ -> dismiss() }
        builder.create()
    } ?: throw IllegalStateException("Activity cannot be null")

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnLogoutConfirmedListener) {
            listener = context
        } else throw java.lang.IllegalStateException("$context must implement ${OnLogoutConfirmedListener::class.java.simpleName}")
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {

        val TAG = LogoutConfirmationDialog::class.java.simpleName

        fun show(activity: AppCompatActivity) {
            activity.supportFragmentManager.findFragmentByTag(TAG)
                ?: newInstance().show(activity.supportFragmentManager, TAG)
        }

        private fun newInstance() = LogoutConfirmationDialog()
    }

}