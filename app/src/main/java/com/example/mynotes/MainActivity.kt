package com.example.mynotes

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mynotes.routing.MyNotesRouter
import com.example.mynotes.routing.Screen
import com.example.mynotes.screens.InfoScreen
import com.example.mynotes.screens.NotesScreen
import com.example.mynotes.screens.SaveNoteScreen
import com.example.mynotes.screens.TrashScreen
import com.example.mynotes.ui.theme.MyNotesTheme
import com.example.mynotes.ui.theme.MyNotesThemeSettings
import com.example.mynotes.viewmodel.MainViewModel
import com.example.mynotes.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyNotesTheme(darkTheme = MyNotesThemeSettings.isDarkThemeEnabled) {
                val viewModel: MainViewModel = viewModel(
                    factory = MainViewModelFactory(LocalContext.current.applicationContext as Application)
                )
                MainActivityScreen(viewModel)
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun MainActivityScreen(viewModel: MainViewModel) {
    Surface {
        when (MyNotesRouter.currentScreen) {
            is Screen.Notes -> NotesScreen(viewModel)
            is Screen.SaveNote -> SaveNoteScreen(viewModel)
            is Screen.Trash -> TrashScreen(viewModel)
            is Screen.Info -> InfoScreen(viewModel)
        }
    }
}
