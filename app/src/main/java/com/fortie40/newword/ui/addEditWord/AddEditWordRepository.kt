package com.fortie40.newword.ui.addEditWord

import android.app.Application
import com.fortie40.newword.roomdatabase.WordDao
import com.fortie40.newword.roomdatabase.WordModel
import com.fortie40.newword.roomdatabase.WordRoomDatabase

class AddEditWordRepository() {
    private lateinit var wordDao: WordDao

    constructor(application: Application): this() {
        val database = WordRoomDatabase(application)
        wordDao = database.wordDao()
    }

    suspend fun saveWord(wordModel: WordModel) {
        wordDao.saveWord(wordModel)
    }

    suspend fun updateWord(wordLearned: String, language: String, meaning: String, id: Int) {
        wordDao.updateWord(wordLearned, language, meaning, id)
    }

    suspend fun deleteWord(wordModel: WordModel) {
        wordDao.deleteWord(wordModel)
    }
}