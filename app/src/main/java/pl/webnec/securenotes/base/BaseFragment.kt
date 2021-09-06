package pl.webnec.securenotes.base

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar

abstract class BaseFragment<VM : ViewModel, B : ViewBinding> : Fragment() {

    protected lateinit var binding: B
    protected lateinit var viewModel: VM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = getFragmentBinding(inflater, container)
        viewModel = ViewModelProvider(requireActivity()).get(getViewModel())
        return binding.root
    }

    fun Fragment.successInfo(message: String) {
        requireView().snackbarSuccess(message)
    }

    fun Fragment.errorInfo(message: String) {
        requireView().snackbarError(message)
    }

    private fun View.snackbarError(message: String) {
        val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED)
        snackbar.show()
    }

    private fun View.snackbarSuccess(message: String) {
        val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_LONG).setBackgroundTint(Color.parseColor("#009933"))
        snackbar.show()
    }

    abstract fun getViewModel(): Class<VM>

    abstract fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): B
}