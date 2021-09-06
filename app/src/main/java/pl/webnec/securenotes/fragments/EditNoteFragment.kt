package pl.webnec.securenotes.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import pl.webnec.securenotes.R
import pl.webnec.securenotes.base.BaseFragment
import pl.webnec.securenotes.databinding.FragmentEditNoteBinding
import pl.webnec.securenotes.utilities.TAG
import pl.webnec.securenotes.viewmodels.MainViewModel

class EditNoteFragment : BaseFragment<MainViewModel, FragmentEditNoteBinding>() {

    var messageEditIndex: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tempMessageEditIndex = arguments?.getString("messageEditIndex")
        if(tempMessageEditIndex != null){
            messageEditIndex = tempMessageEditIndex.toIntOrNull()
            editNote()
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_editNoteFragment_to_notesFragment)
        }

        activity?.onBackPressedDispatcher?.addCallback(requireActivity(), object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_editNoteFragment_to_notesFragment)
            }
        })

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_edit_accept -> {
                    if(messageEditIndex != null){
                        viewModel.editMessage(messageEditIndex!!, binding.bodyEditText.text.toString())
                    } else {
                        viewModel.addMessage(binding.bodyEditText.text.toString())
                    }
                    findNavController().navigate(R.id.action_editNoteFragment_to_notesFragment)
                    true
                }
                R.id.menu_edit_delete -> {
                    if(messageEditIndex != null){
                        viewModel.deleteMessage(messageEditIndex!!)
                    }
                    findNavController().navigate(R.id.action_editNoteFragment_to_notesFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun editNote(){
        if(messageEditIndex != null){
            val editedMessage = viewModel.secretMessages.value?.get(messageEditIndex!!)
            binding.bodyEditText.setText(editedMessage.toString())
        }
    }

    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentEditNoteBinding.inflate(inflater, container, false)
}