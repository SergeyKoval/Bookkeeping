package by.bk.bookkeeper.android.ui

import android.view.Gravity
import android.view.View
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
abstract class BaseViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {

    abstract fun setItem(item: T?)

    open fun setClickObservable(observable: Observable<RecyclerClick>): Disposable? = null

    /**
     * @param anchor Anchor view the popup will appear below
     * @param menuRes Menu resource id to be inflated
     */
    protected fun createActionsPopup(anchor: View, @MenuRes menuRes: Int) = PopupMenu(itemView.context, anchor).apply {
        menuInflater.inflate(menuRes, menu)
        gravity = Gravity.END
    }

    /**
     * @param menuBuilder Menu associated with popup
     * @param anchor Anchor view the popup will appear below
     */
    protected fun showPopupMenuWithIcons(menuBuilder: MenuBuilder, anchor: View) =
            MenuPopupHelper(itemView.context, menuBuilder, anchor).apply {
                setForceShowIcon(true)
                show()
            }
}