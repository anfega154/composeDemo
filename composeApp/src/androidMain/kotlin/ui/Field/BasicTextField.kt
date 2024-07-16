package ui.Field
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.foundation.background


object BasicTextField {

    @Composable
    fun CustomTextField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier,
        placeholder: String = "",
        textStyle: TextStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            textStyle = textStyle,
            decorationBox = { innerTextField ->
                Box(
                    Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                ) {
                    if (value.isEmpty()) Text(placeholder)
                    innerTextField()
                }
            }
        )
    }
}