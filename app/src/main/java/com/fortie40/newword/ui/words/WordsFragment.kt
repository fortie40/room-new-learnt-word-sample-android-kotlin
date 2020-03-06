package com.fortie40.newword.ui.words

import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.fortie40.newword.R
import com.fortie40.newword.databinding.WordsFragmentBinding
import com.fortie40.newword.dialogs.DeleteDialog
import com.fortie40.newword.helperclasses.HelperFunctions
import com.fortie40.newword.interfaces.IClickListener
import com.fortie40.newword.roomdatabase.WordModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.words_fragment.*
import timber.log.Timber

class WordsFragment : Fragment(), IClickListener {
    private lateinit var wordsFragmentBinding: WordsFragmentBinding
    private lateinit var root: View
    private lateinit var viewModel: WordsViewModel
    private lateinit var wordAdapter: WordAdapter
    private lateinit var handler: Handler
    private lateinit var r: Runnable

    private var isInitialized: Boolean = false
    private var actionMode: ActionMode? = null
    private var numberOfItems: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // change toolbar title
        activity!!.title = getString(R.string.words)
        // back button
        activity!!.toolbar.navigationIcon = null

        wordsFragmentBinding = WordsFragmentBinding.inflate(inflater)
        root = wordsFragmentBinding.root
        setHasOptionsMenu(true)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        wordsFragmentBinding.apply {
            this.lifecycleOwner = viewLifecycleOwner
        }
        // swipe refresh layout
        swipe_to_refresh.setColorSchemeResources(
            R.color.colorAccent, R.color.colorPrimary,
            R.color.colorPrimaryDark
        )
        swipe_to_refresh.setOnRefreshListener {
            getWords()
        }

        // navigate to add edit word fragment
        addNewWord.setOnClickListener(
            Navigation.createNavigateOnClickListener(R.id.action_wordsFragment_to_addEditWordFragment)
        )
        viewModel = ViewModelProviders.of(this).get(WordsViewModel::class.java)
        getWords()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.queryHint = getString(R.string.search_anything)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                Timber.i("Query Submitted")
                return false
            }

            override fun onQueryTextChange(p0: String): Boolean {
                return if (!::wordAdapter.isInitialized) {
                    Timber.d("blank")
                    no_words.visibility = View.VISIBLE
                    when(p0) {
                        "" -> no_words.text = getString(R.string.no_words)
                        else -> no_words.text = getString(R.string.no_results_found, p0)
                    }
                    false
                } else {
                    wordAdapter.filter.filter(HelperFunctions.toLowerCase(p0))
                    isInitialized = true
                    handler = Handler()
                    r = Runnable { word_items.scrollToPosition(0) }
                    handler.postDelayed(r, 300)
                    false
                }
            }

        })
    }

    override fun onPause() {
        if (isInitialized) {
            handler.removeCallbacks(r)
        }
        super.onPause()
    }

    override fun onWordClick(wordModel: WordModel) {
        Timber.i("$wordModel")
    }

    override fun onWordLongClicked(clickedItemIndex: Int) {
        Timber.i("i was long Clicked $clickedItemIndex")
        if (actionMode == null) {
            actionMode = activity!!.startActionMode(ActionModeCallback())
        }
        toggleSelection(clickedItemIndex)
    }

    private fun getWords() {
        swipe_to_refresh.isRefreshing = true
        viewModel.allWords.observe(viewLifecycleOwner, Observer { words ->
            if (words.isEmpty()) {
                no_words.visibility = View.VISIBLE
                swipe_to_refresh.isRefreshing = false
            } else {
                no_words.visibility = View.GONE
                for (word in words) {
                    Timber.d("***************************************************************")
                    Timber.d("ID: ${word.wordId}")
                    Timber.d("Word: ${word.wordLearned}")
                    Timber.d("***************************************************************")
                }
                wordAdapter = WordAdapter(this, words)
                words.let { wordAdapter.submitList(it) }
                //word_items.layoutManager = LinearLayoutManager(activity)
                word_items.adapter = wordAdapter
                swipe_to_refresh.isRefreshing = false
            }
        })
    }

    private fun toggleSelection(position: Int) {
        wordAdapter.toggleSelection(position)
        val count = wordAdapter.getSelectedItemCount()

        if (count == 0) {
            actionMode?.finish()
        } else {
            actionMode?.title = count.toString()
            actionMode?.invalidate()
            numberOfItems = count
        }
    }

    private fun openDialog() {
        val deleteDialog = DeleteDialog()
        deleteDialog.show(activity!!.supportFragmentManager, getString(R.string.delete_dialog))
    }

    inner class ActionModeCallback : ActionMode.Callback {
        override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean {
            return when(p1?.itemId) {
                R.id.action_delete -> {
                    Timber.d("Selected")
                    openDialog()
                    true
                }
                else -> false
            }
        }

        override fun onCreateActionMode(p0: ActionMode?, p1: Menu?): Boolean {
            p0?.menuInflater?.inflate(R.menu.add_edit_menu, p1)
            return true
        }

        override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
            return false
        }

        override fun onDestroyActionMode(p0: ActionMode?) {
            wordAdapter.clearSelection()
            actionMode = null
        }
    }
}
