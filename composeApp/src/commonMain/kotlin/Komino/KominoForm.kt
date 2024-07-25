package Komino

import Komino.Ultils.FormSchema
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import getColorTheme
import ui.BasicSelect.DropdownSelector
import ui.Field.BasicTextField.CustomTextField
import ui.Field.TextAreaField.LabeledTextField

@Composable
fun KominoForm(schema: FormSchema, onSave: (Map<String, String>) -> Unit) {
    val fieldValues = remember { mutableStateMapOf<String, String>() }

    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        schema.fields.forEach { field ->
            when (field.type) {
                "text" -> {
                    CustomTextField(
                        value = fieldValues.getOrPut(field.label) { "" },
                        onValueChange = { fieldValues[field.label] = it },
                        placeholder = field.placeholder ?: ""
                    )
                }
                "dropdown" -> {
                    DropdownSelector(
                        options = field.options ?: emptyList(),
                        selectedOption = fieldValues.getOrPut(field.label) { field.options?.firstOrNull() ?: "" },
                        onOptionSelected = { fieldValues[field.label] = it },
                        title = field.label
                    )
                }
                "textarea" -> {
                    LabeledTextField(
                        value = fieldValues.getOrPut(field.label) { "" },
                        onValueChange = { fieldValues[field.label] = it },
                        label = field.label
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onSave(fieldValues) },
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = getColorTheme().mantum),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar")
        }
    }
}