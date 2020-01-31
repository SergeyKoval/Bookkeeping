package by.bk.bookkeeper.android

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

fun View.snackbar(@StringRes messageRes: Int, length: Int = Snackbar.LENGTH_SHORT) = Snackbar
        .make(this, resources.getString(messageRes), length).show()

fun View.snackbar(message: String, length: Int = Snackbar.LENGTH_SHORT) = Snackbar
        .make(this, message, length).show()

inline fun View.actionSnackbar(@StringRes messageRes: Int, length: Int = Snackbar.LENGTH_LONG, function: Snackbar.() -> Unit): Snackbar =
        actionSnackbar(resources.getString(messageRes), length, function)

inline fun View.actionSnackbar(message: String, length: Int = Snackbar.LENGTH_LONG, f: Snackbar.() -> Unit): Snackbar =
        Snackbar.make(this, message, length).also {
            it.f()
            it.show()
        }

fun Snackbar.action(@StringRes actionRes: Int, color: Int? = null, listener: (View) -> Unit) {
    action(view.resources.getString(actionRes), color, listener)
}

fun Snackbar.action(action: String, color: Int? = null, listener: (View) -> Unit) {
    setAction(action, listener)
    color?.let { setActionTextColor(ContextCompat.getColor(context, it)) }
}

fun hideKeyboard(view: View?) = view?.let {
    (it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(it.windowToken, 0)
}