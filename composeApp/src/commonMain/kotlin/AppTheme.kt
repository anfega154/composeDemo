import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = MaterialTheme.colors.copy(primary = Color.Black),
        shapes = MaterialTheme.shapes.copy(
            small = AbsoluteCutCornerShape(0.dp),
            medium = AbsoluteCutCornerShape(0.dp),
            large = AbsoluteCutCornerShape(0.dp)
        )
    ){
        content()
    }

}

@Composable
fun getColorTheme() : DarkModeColors {
    val isDarkMode = false

    val Mantum = Color(0xFFFFA500)
    val BackgroundColor = if(isDarkMode) Color(0XFF1E1C1C) else Color.White
    val Textcolor = if(isDarkMode) Color.White else Color.Black
    val ColorCards = Color(0xF9F6F2)
    val Success = Color(0xFF32CD32)
    val Basicbutton = Color(0xFFDCDCDC)
    val BlackDefault = Color.Black

    return DarkModeColors(
        mantum = Mantum,
        backgroundColor = BackgroundColor,
        textColor = Textcolor,
        colorCards = ColorCards,
        success = Success,
        basicbutton = Basicbutton,
        blackDefault = BlackDefault
    )
}

data class DarkModeColors(
    val mantum : Color,
    val backgroundColor : Color,
    val textColor : Color,
    val colorCards : Color,
    val success : Color,
    val basicbutton : Color,
    val blackDefault : Color
)