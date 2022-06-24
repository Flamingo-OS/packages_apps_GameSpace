/*
 * Copyright (C) 2022 FlamingoOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flamingo.gamespace.ui.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ListPreference(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    clickable: Boolean = true,
    onClick: (() -> Unit)? = null,
    entries: List<Entry<T>>,
    value: T?,
    onEntrySelected: (T) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {},
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
            shape = RoundedCornerShape(32.dp),
            title = {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(entries) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showDialog = false
                                    onEntrySelected(it.value)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                onClick = {
                                    showDialog = false
                                    onEntrySelected(it.value)
                                },
                                selected = it.value == value
                            )
                            Text(
                                modifier = Modifier.weight(1f),
                                text = it.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
        )
    }
    Preference(
        title = title,
        modifier = modifier,
        summary = summary ?: entries.find { it.value == value }?.name,
        clickable = clickable,
        onClick = {
            if (onClick == null) {
                showDialog = true
            } else {
                onClick()
            }
        },
    )
}

@Preview
@Composable
fun PreviewListPreference() {
    ListPreference(title = "List preference",
        summary = "This is a list preference",
        entries = listOf(
            Entry("Entry 1", 0),
            Entry("Entry 2", 1),
            Entry("Entry 3", 2)
        ),
        value = 1,
        onEntrySelected = {}
    )
}