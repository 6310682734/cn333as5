package com.example.mynotes.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.painterResource
import com.example.mynotes.R
import com.example.mynotes.domain.model.NoteModel
import com.example.mynotes.routing.Screen
import com.example.mynotes.ui.components.AppDrawer
import com.example.mynotes.ui.components.Note
import com.example.mynotes.viewmodel.MainViewModel
import kotlinx.coroutines.launch

private const val NO_DIALOG = 1
private const val RESTORE_NOTES_DIALOG = 2
private const val PERMANENTLY_DELETE_DIALOG = 3

@Composable
@ExperimentalMaterialApi
fun TrashScreen(viewModel: MainViewModel) {

    val notesInThrash: List<NoteModel> by viewModel.notesInTrash
        .observeAsState(listOf())

    val selectedNotes: List<NoteModel> by viewModel.selectedNotes
        .observeAsState(listOf())

    val dialogState = rememberSaveable { mutableStateOf(NO_DIALOG) }

    val scaffoldState = rememberScaffoldState()

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            val areActionsVisible = selectedNotes.isNotEmpty()
            TrashTopAppBar(
                onNavigationIconClick = {
                    coroutineScope.launch { scaffoldState.drawerState.open() }
                },
                onRestoreNotesClick = { dialogState.value = RESTORE_NOTES_DIALOG },
                onDeleteNotesClick = { dialogState.value = PERMANENTLY_DELETE_DIALOG },
                areActionsVisible = areActionsVisible
            )
        },
        scaffoldState = scaffoldState,
        drawerContent = {
            AppDrawer(
                currentScreen = Screen.Trash,
                closeDrawerAction = {
                    coroutineScope.launch { scaffoldState.drawerState.close() }
                }
            )
        },
        content = {
            Content(
                notes = notesInThrash,
                onNoteClick = { viewModel.onNoteSelected(it) },
                selectedNotes = selectedNotes
            )

            val dialog = dialogState.value
            if (dialog != NO_DIALOG) {
                val confirmAction: () -> Unit = when (dialog) {
                    RESTORE_NOTES_DIALOG -> {
                        {
                            viewModel.restoreNotes(selectedNotes)
                            dialogState.value = NO_DIALOG
                        }
                    }
                    PERMANENTLY_DELETE_DIALOG -> {
                        {
                            viewModel.permanentlyDeleteNotes(selectedNotes)
                            dialogState.value = NO_DIALOG
                        }
                    }
                    else -> {
                        {
                            dialogState.value = NO_DIALOG
                        }
                    }
                }

                AlertDialog(
                    onDismissRequest = { dialogState.value = NO_DIALOG },
                    title = { Text(mapDialogTitle(dialog)) },
                    text = { Text(mapDialogText(dialog)) },
                    confirmButton = {
                        TextButton(onClick = confirmAction) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { dialogState.value = NO_DIALOG }) {
                            Text("Dismiss")
                        }
                    }
                )
            }
        }
    )
}

@Composable
private fun TrashTopAppBar(
    onNavigationIconClick: () -> Unit,
    onRestoreNotesClick: () -> Unit,
    onDeleteNotesClick: () -> Unit,
    areActionsVisible: Boolean
) {
    TopAppBar(
        title = { Text(text = "Trash", color = MaterialTheme.colors.onPrimary) },
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick) {
                Icon(
                    imageVector = Icons.Filled.List,
                    contentDescription = "Drawer Button"
                )
            }
        },
        actions = {
            if (areActionsVisible) {
                IconButton(onClick = onRestoreNotesClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_restore_from_trash_24),
                        contentDescription = "Restore Notes Button",
                        tint = MaterialTheme.colors.onPrimary
                    )
                }
                IconButton(onClick = onDeleteNotesClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_delete_forever_24),
                        contentDescription = "Delete Notes Button",
                        tint = MaterialTheme.colors.onPrimary
                    )
                }
            }
        }
    )
}

@Composable
@ExperimentalMaterialApi
private fun Content(
    notes: List<NoteModel>,
    onNoteClick: (NoteModel) -> Unit,
    selectedNotes: List<NoteModel>,
) {
    val tabs = listOf("REGULAR", "CHECKABLE")

    // Init state for selected tab
    var selectedTab by remember { mutableStateOf(0) }

    Column {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = selectedTab == index,
                    onClick = { selectedTab = index }
                )
            }
        }

        val filteredNotes = when (selectedTab) {
            0 -> {
                notes.filter { it.isCheckedOff == null }
            }
            1 -> {
                notes.filter { it.isCheckedOff != null }
            }
            else -> throw IllegalStateException("Tab not supported - index: $selectedTab")
        }

        LazyColumn {
            items(count = filteredNotes.size) { noteIndex ->
                val note = filteredNotes[noteIndex]
                val isNoteSelected = selectedNotes.contains(note)
                Note(
                    note = note,
                    onNoteClick = onNoteClick,
                    isSelected = isNoteSelected
                )
            }
        }
    }
}

private fun mapDialogTitle(dialog: Int): String = when (dialog) {
    RESTORE_NOTES_DIALOG -> "Restore notes"
    PERMANENTLY_DELETE_DIALOG -> "Delete notes forever"
    else -> throw RuntimeException("Dialog not supported: $dialog")
}

private fun mapDialogText(dialog: Int): String = when (dialog) {
    RESTORE_NOTES_DIALOG -> "Are you sure you want to restore selected notes?"
    PERMANENTLY_DELETE_DIALOG -> "Are you sure you want to delete selected notes permanently?"
    else -> throw RuntimeException("Dialog not supported: $dialog")
}