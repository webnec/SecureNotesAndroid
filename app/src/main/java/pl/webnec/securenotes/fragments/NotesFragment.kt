package pl.webnec.securenotes.fragments

import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.webnec.securenotes.R
import pl.webnec.securenotes.adapters.NotesViewAdapter
import pl.webnec.securenotes.base.BaseFragment
import pl.webnec.securenotes.base.RecyclerViewClickListener
import pl.webnec.securenotes.databinding.FragmentNotesBinding
import pl.webnec.securenotes.utilities.DATA_FILENAME
import pl.webnec.securenotes.utilities.FileCryptographicUtils
import pl.webnec.securenotes.viewmodels.MainViewModel


class NotesFragment : BaseFragment<MainViewModel, FragmentNotesBinding>(), RecyclerViewClickListener {

    private val adapter = NotesViewAdapter(mutableListOf<String>(), this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(requireActivity(), object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                quit(0)
            }
        })

        binding.toolbar.setNavigationOnClickListener {
            quit(0)
        }

        binding.toolbar.setOnMenuItemClickListener {
          //  Log.d(TAG, "onViewCreated: CLICK")
            when (it.itemId) {
                R.id.menu_add -> {
                    addNote()
                    true
                }
                R.id.menu_delete -> {
                    clearAllNotes()
                    true
                }
                R.id.menu_share -> {
                    findNavController().navigate(R.id.action_notesFragment_to_shareViaBluetoothFragment)
                    true
                }
                R.id.menu_lock -> {
                    lockFile()
                    quit(1000L)
                    true
                }
                else -> false
            }
        }

        binding.recyclerViewNotes.also { recycler ->
            recycler.layoutManager = LinearLayoutManager(requireContext())
            recycler.setHasFixedSize(true)
            (recycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            recycler.adapter = adapter
        }

        viewModel.secretMessages.observe(viewLifecycleOwner, Observer {
            adapter.setNotes(it)
        })

    }

    private fun addNote(){
        findNavController().navigate(R.id.action_notesFragment_to_editNoteFragment)
    }

    private fun editNote(messageEditIndex: Int){
        val bundle = bundleOf(
            "messageEditIndex" to messageEditIndex.toString()
        )
        findNavController().navigate(R.id.action_notesFragment_to_editNoteFragment, bundle)
    }

    private fun clearAllNotes(){
        viewModel.clearViewModelHoldingData()
    }

    private fun lockFile(){
        try {
            val secretMessagesJSON = viewModel.getSecretMessagesInJSON()
            FileCryptographicUtils.saveToFile(requireContext().applicationContext, DATA_FILENAME, secretMessagesJSON)
            successInfo(getString(R.string.file_locked))
        } catch (e: Exception) {
            errorInfo(getString(R.string.error_file_not_saved))
        }
    }

    private fun quit(delay: Long){
        lifecycleScope.launch {
            delay(delay)
            requireActivity().finishAffinity()
        }
    }

    override fun onRecyclerViewItemClick(view: View, messagePosition: Any) {
        if(messagePosition is Int){
            editNote(messagePosition)
        }
    }

    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentNotesBinding.inflate(inflater, container, false)
}