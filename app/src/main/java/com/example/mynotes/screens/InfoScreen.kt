package com.example.mynotes.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mynotes.routing.MyNotesRouter
import com.example.mynotes.routing.Screen
import com.example.mynotes.viewmodel.MainViewModel
import com.example.mynotes.R
import com.example.mynotes.domain.model.ColorModel
import com.example.mynotes.domain.model.NEW_NOTE_ID
import com.example.mynotes.domain.model.NoteModel
import com.example.mynotes.ui.components.NoteColor
import com.example.mynotes.util.fromHex
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun InfoScreen(viewModel: MainViewModel) {
    val noteEntry by viewModel.noteEntry.observeAsState(NoteModel())

    val colors: List<ColorModel> by viewModel.colors.observeAsState(listOf())

    val bottomDrawerState = rememberBottomDrawerState(BottomDrawerValue.Closed)

    val coroutineScope = rememberCoroutineScope()

    val moveNoteToTrashDialogShownState = rememberSaveable { mutableStateOf(false) }

    BackHandler {
        if (bottomDrawerState.isOpen) {
            coroutineScope.launch { bottomDrawerState.close() }
        } else {
            MyNotesRouter.navigateTo(Screen.Notes)
        }
    }

    Scaffold(
        topBar = {
            val isEditingMode: Boolean = noteEntry.id != NEW_NOTE_ID
            InfoTopAppBar(
                isEditingMode = isEditingMode,
                onBackClick = { MyNotesRouter.navigateTo(Screen.Notes) },
                //onSaveNoteClick = { viewModel.saveNote(noteEntry) },
                onOpenColorPickerClick = {
                    coroutineScope.launch { bottomDrawerState.open() }
                },
                onDeleteNoteClick = {
                    moveNoteToTrashDialogShownState.value = true
                },
                onInfoClick = {
                    viewModel.onInfoClick(noteEntry)
                }
            )
        }
    ) {
        BottomDrawer(
            drawerState = bottomDrawerState,
            drawerContent = {
                ColorPicker(
                    colors = colors,
                    onColorSelect = { color ->
                        viewModel.onNoteEntryChange(noteEntry.copy(color = color))
                    }
                )
            }
        ) {
            SaveNoteContent(
                note = noteEntry,
                onNoteChange = { updateNoteEntry ->
                    viewModel.onNoteEntryChange(updateNoteEntry)
                }
            )
        }

        if (moveNoteToTrashDialogShownState.value) {
            AlertDialog(
                onDismissRequest = {
                    moveNoteToTrashDialogShownState.value = false
                },
                title = {
                    Text("Move note to the trash?")
                },
                text = {
                    Text(
                        "Are you sure you want to " +
                                "move this note to the trash?"
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.moveNoteToTrash(noteEntry)
                    }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        moveNoteToTrashDialogShownState.value = false
                    }) {
                        Text("Dismiss")
                    }
                }
            )
        }
    }
}

@Composable
fun InfoTopAppBar(
    isEditingMode: Boolean,
    onBackClick: () -> Unit,
    onInfoClick: () -> Unit,
    onOpenColorPickerClick: () -> Unit,
    onDeleteNoteClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Contact Info",
                color = MaterialTheme.colors.onPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back Button",
                    tint = MaterialTheme.colors.onPrimary
                )
            }
        },
        actions = {
            if (isEditingMode) {
                IconButton(onClick = onInfoClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Contact Button",
                        tint = MaterialTheme.colors.onPrimary
                    )
                }
            }
        }
    )
}

@Composable
private fun SaveNoteContent(
    note: NoteModel,
    onNoteChange: (NoteModel) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        NoteColor(
            modifier = Modifier.padding(top = 20.dp),
            color = Color.fromHex(note.color.hex),
            size = 100.dp,
            border = 1.dp
        )

        Text(
            modifier = Modifier.padding(top = 50.dp),
            text = note.title,
            fontSize = 36.sp

        )

        Text(
            modifier = Modifier.padding(top = 20.dp),
            text = note.category,
            fontSize = 18.sp,
        )
        Card(
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.surface
        ){
            Row(
                Modifier
                    .padding(8.dp)
            ) {
                Text(
                    text = "Phone",
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                )
                Text(
                    text = note.content,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 80.dp)
                )
            }
        }

        Icon(
            modifier = Modifier
                .padding(top = 250.dp)
                .size(72.dp),
            imageVector = Icons.Default.Phone,
            contentDescription = "Call",
            tint = MaterialTheme.colors.onSecondary,
        )
    }
}

@Composable
private fun PickedColor(color: ColorModel) {
    Row(
        Modifier
            .padding(8.dp)
            .padding(top = 16.dp)
    ) {
        Text(
            text = "Phone",
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        )
        NoteColor(
            color = Color.fromHex(color.hex),
            size = 40.dp,
            border = 1.dp,
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Composable
private fun ColorPicker(
    colors: List<ColorModel>,
    onColorSelect: (ColorModel) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Color picker",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(colors.size) { itemIndex ->
                val color = colors[itemIndex]
                ColorItem(
                    color = color,
                    onColorSelect = onColorSelect
                )
            }
        }
    }
}

