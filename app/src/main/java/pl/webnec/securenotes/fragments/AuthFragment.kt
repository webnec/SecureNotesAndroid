package pl.webnec.securenotes.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.webnec.securenotes.R
import pl.webnec.securenotes.base.BaseFragment
import pl.webnec.securenotes.databinding.FragmentAuthBinding
import pl.webnec.securenotes.utilities.*
import pl.webnec.securenotes.viewmodels.MainViewModel

class AuthFragment : BaseFragment<MainViewModel, FragmentAuthBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.clearViewModelHoldingData()

        activity?.onBackPressedDispatcher?.addCallback(requireActivity(), object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finishAffinity()
            }
        })

        lifecycleScope.launch {
            delay(1000L)
            tryAuthenticateWithBiometric()
        }
    }

    private fun tryAuthenticateWithBiometric() {
        val biometricAvailabilityCode = BiometricManagerUtils.getAvailabilityCode(requireContext().applicationContext)
        if(BiometricManagerUtils.isBiometricAvailable(biometricAvailabilityCode))
            showBiometricPrompt()
        else
            showBiometricIsUnavailableInformationDialog(biometricAvailabilityCode)
    }

    private fun showBiometricPrompt() {
        val biometricPrompt = BiometricPromptUtils.getPrompt(requireActivity(), ::resultOfBiometric)
        val promptInfo = BiometricPromptUtils.getPromptInfo(requireActivity())
        biometricPrompt.authenticate(promptInfo)
    }

    private fun showBiometricIsUnavailableInformationDialog(biometricUnavailableCode: Int){
        val information: String = BiometricManagerUtils
            .getUnavailabilityInformation(biometricUnavailableCode, requireContext().applicationContext)
        val title = getString(R.string.authentication_problem)
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(information)
            .setPositiveButton("OK") { _, _ ->
                requireActivity().finishAffinity()
            }
            .show()
    }

    private fun getEncryptedDataFromFile(){
        try {
            val rawDataFromFile = FileCryptographicUtils.readFromFile(requireContext().applicationContext, DATA_FILENAME)
            if(rawDataFromFile.isNotEmpty())
                viewModel.setDataFromFile(rawDataFromFile)
        } catch (e: Exception) {
            Log.e(TAG, "getEncryptedDataFromFile: cant read from file")
        }
    }

    private fun resultOfBiometric(isSuccess: Boolean) {
        if(isSuccess){
            getEncryptedDataFromFile()
            findNavController().navigate(R.id.action_authFragment_to_notesFragment)
        }
    }

    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAuthBinding.inflate(inflater, container, false)

}