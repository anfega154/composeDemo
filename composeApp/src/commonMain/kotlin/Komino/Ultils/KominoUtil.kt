package Komino.Ultils

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class FormField(
    val type: String,
    val label: String,
    val placeholder: String? = null,
    val options: List<String>? = null
)

@Serializable
data class FormSchema(
    val fields: List<FormField>
)

fun parseJsonSchema(json: String): FormSchema {
    return Json.decodeFromString(json)
}