package pl.webnec.securenotes.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import java.util.*

class MainViewModel(): ViewModel() {


    private val _secretMessages: MutableLiveData<MutableList<String>> = MutableLiveData<MutableList<String>>()
    val secretMessages: LiveData<MutableList<String>> get() = _secretMessages


    init {
        _secretMessages.value = LinkedList<String>()
    }

    fun clearViewModelHoldingData(){
        _secretMessages.value?.clear()
        _secretMessages.value = _secretMessages.value
    }

    fun addMessage(message: String){
        _secretMessages.value?.add(message);
        _secretMessages.value = _secretMessages.value
    }

    fun editMessage(index: Int, message: String){
        _secretMessages.value?.set(index, message)
        _secretMessages.value = _secretMessages.value
    }

    fun deleteMessage(index: Int){
        _secretMessages.value?.removeAt(index)
        _secretMessages.value = _secretMessages.value
    }

    fun setDataFromFile(rawDataFromFile: String){
        val messagesFromFile = Gson().fromJson<List<String>>(rawDataFromFile, List::class.java)
        _secretMessages.value = messagesFromFile as MutableList<String>
    }

    fun getSecretMessagesInJSON(): String {
        return Gson().toJson(_secretMessages.value, List::class.java)
    }

}