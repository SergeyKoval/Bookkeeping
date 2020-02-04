package by.bk.bookkeeper.android.ui

sealed class RecyclerClick(val position: Int) {
    class Row(position: Int) : RecyclerClick(position)
}