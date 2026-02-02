package com.example.emotionsai.ui.hr.analytics.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.emotionsai.data.remote.HrEventDto

@Composable
fun FilterBar(
    startDate: String,
    endDate: String,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,

    emotionsOrder: List<String>,
    selectedEmotions: List<String>,
    onToggleEmotion: (String) -> Unit,

    departments: List<Pair<Int, String>>,
    selectedDepartments: List<Int>,
    onToggleDepartment: (Int) -> Unit,

    events: List<HrEventDto>,
    selectedEventId: Int?,
    onSelectEventId: (Int?) -> Unit,

    hasEvent: Boolean?,
    onSelectHasEvent: (Boolean?) -> Unit
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {

        // dates (пока простые текстовые поля yyyy-MM-dd)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = startDate,
                onValueChange = onStartChange,
                label = { Text("Start date") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = endDate,
                onValueChange = onEndChange,
                label = { Text("End date") },
                modifier = Modifier.weight(1f)
            )
        }

        // has_event toggle
        SingleChoiceSegment(
            title = "Has event",
            options = listOf(
                "All" to null,
                "Only with" to true,
                "Only without" to false
            ),
            selected = hasEvent,
            onSelect = onSelectHasEvent
        )

        // event dropdown
        EventDropdown(
            events = events,
            selectedEventId = selectedEventId,
            onSelect = onSelectEventId
        )

        // emotions chips
        MultiChoiceChips(
            title = "Emotions",
            options = emotionsOrder,
            selected = selectedEmotions,
            onToggle = onToggleEmotion
        )

        // departments chips
        MultiChoiceChips(
            title = "Departments",
            options = departments.map { it.second },
            selected = selectedDepartments.mapNotNull { id -> departments.firstOrNull { it.first == id }?.second },
            onToggleLabel = { label ->
                val id = departments.firstOrNull { it.second == label }?.first ?: return@MultiChoiceChips
                onToggleDepartment(id)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDropdown(
    events: List<HrEventDto>,
    selectedEventId: Int?,
    onSelect: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedLabel = events.firstOrNull { it.id == selectedEventId }?.title ?: "All events"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Event") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("All events") },
                onClick = { onSelect(null); expanded = false }
            )
            events.forEach { e ->
                DropdownMenuItem(
                    text = { Text(e.title) },
                    onClick = { onSelect(e.id); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun MultiChoiceChips(
    title: String,
    options: List<String>,
    selected: List<String>,
    onToggle: ((String) -> Unit)? = null,
    onToggleLabel: ((String) -> Unit)? = null
) {
    Text(title, style = MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { opt ->
            val isSelected = selected.contains(opt)
            FilterChip(
                selected = isSelected,
                onClick = {
                    onToggle?.invoke(opt)
                    onToggleLabel?.invoke(opt)
                },
                label = { Text(opt) }
            )
        }
    }
}

@Composable
private fun <T> SingleChoiceSegment(
    title: String,
    options: List<Pair<String, T>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    Text(title, style = MaterialTheme.typography.labelLarge)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (label, value) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(label) }
            )
        }
    }
}
