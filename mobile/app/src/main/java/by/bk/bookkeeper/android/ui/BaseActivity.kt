package by.bk.bookkeeper.android.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.bk.bookkeeper.android.Injection
import io.reactivex.disposables.CompositeDisposable

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

abstract class BaseActivity<VM : ViewModel> : AppCompatActivity() {

    protected var subscriptionsDisposable: CompositeDisposable = CompositeDisposable()
    protected lateinit var viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, Injection.provideViewModelFactory()).get(getViewModelClass())
    }

    override fun onStop() {
        subscriptionsDisposable.clear()
        super.onStop()
    }

    abstract fun getViewModelClass(): Class<VM>
}