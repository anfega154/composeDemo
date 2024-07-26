package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object BasicSelect {

    @Composable
    fun DropdownSelector(
        options: List<String>,
        selectedOption: String,
        onOptionSelected: (String) -> Unit,
        modifier: Modifier = Modifier,
        title: String = ""
    ) {
        var expanded by remember { mutableStateOf(false) }

        Box(modifier = modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                label = { Text(title) },
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
                    .padding(4.dp)
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }) {
                        Text(option)
                    }
                }
            }
        }
    }
}