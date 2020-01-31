package by.bk.bookkeeper.android.ui

sealed class RecyclerClick(val position: Int) {
    class AddAssociation(position: Int) : RecyclerClick(position)
    class EditAssociation(position: Int) : RecyclerClick(position)
    class RemoveAssociation(position: Int) : RecyclerClick(position)
}