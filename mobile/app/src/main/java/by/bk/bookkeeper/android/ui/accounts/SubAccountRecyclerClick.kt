package by.bk.bookkeeper.android.ui.accounts

import by.bk.bookkeeper.android.network.response.Account
import by.bk.bookkeeper.android.network.response.Association
import by.bk.bookkeeper.android.network.response.SubAccount

sealed class SubAccountRecyclerClick(val account: Account, val subAccount: SubAccount) {
    class AddAssociation(account: Account, subAccount: SubAccount) : SubAccountRecyclerClick(account, subAccount)
    class EditAssociation(account: Account, subAccount: SubAccount, val association: Association) : SubAccountRecyclerClick(account, subAccount)
    class RemoveAssociation(account: Account, subAccount: SubAccount, val association: Association) : SubAccountRecyclerClick(account, subAccount)
}