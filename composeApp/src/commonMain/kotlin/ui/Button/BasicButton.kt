package ui.Button

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import getColorTheme

object BasicButton {
    @Composable
    fun GenericButton(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        color: String = ""
    ) {
        val colorTheme = getColorTheme()
        val backgroundColor = when (color) {
            "Success" -> colorTheme.success
            "Danger" -> colorTheme.danger
            "Warning" -> colorTheme.warning
            else -> colorTheme.mantum
        }
        Button(
            onClick = { onClick() },
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = backgroundColor),
            modifier = modifier
        ) {
            Text(text)
        }
    }
}