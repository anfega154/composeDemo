package ui.Field

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.unit.dp
import androidx.compose.material.Text
import androidx.compose.foundation.background
import androidx.compose.ui.draw.shadow
import getColorTheme


object BasicTextField {
    @Composable
    fun CustomTextField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier,
        placeholder: String = "",
        textStyle: TextStyle = TextStyle.Default,
        field: String = ""
    ) {
        val color = getColorTheme()
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .shadow(elevation = 1.dp),
                textStyle = textStyle,
                decorationBox = { innerTextField ->
                    Box(
                        Modifier
                            .background(color = color.colorCards)
                            .padding(8.dp)
                    ) {
                        if (value.isEmpty()) Text(placeholder)
                        innerTextField()
                    }
                }
            )
    }
}