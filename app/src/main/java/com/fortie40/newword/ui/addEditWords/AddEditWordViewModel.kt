package com.fortie40.newword.ui.addEditWords

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.fortie40.newword.roomdatabase.WordModel

class AddEditWordViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AddEditWordRepository = AddEditWordRepository(application)

    var word = MutableLiveData<String>("")
    var language = MutableLiveData<String>("")
    var meaning = MutableLiveData<String>("")

    suspend fun saveWord(wordModel: WordModel) {
        repository.saveWord(wordModel)
    }
}
