package com.ygoular.notes.view.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.ygoular.notes.R

class NoteActivity : AppCompatActivity() {

    companion object {
        var mSupportActionBar: ActionBar? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)
        mSupportActionBar = supportActionBar
        if (intent.action == getString(R.string.shortcut_create_note_action)) {
            navigateCreateNote(Navigation.findNavController(this, R.id.nav_host_fragment))
        }
    }

    fun navigateCreateNote(navController: NavController) {
        val bundle =
            bundleOf(getString(R.string.bundle_save_mode) to AddEditNoteFragment.SaveMode.CREATE)
        navController.navigate(R.id.action_noteFragment_to_addEditNoteFragment, bundle)
    }

    fun hideKeyboard(view: View?) {
        if (view != null) {
            val inputMethodManager: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
