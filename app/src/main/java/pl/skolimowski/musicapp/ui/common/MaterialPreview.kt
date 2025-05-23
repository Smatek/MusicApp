package pl.skolimowski.musicapp.ui.common

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import pl.skolimowski.musicapp.ui.theme.MusicAppTheme

@Preview(
    name = "dynamic colors - blue, dark mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE
)
@Preview(
    name = "dynamic colors - blue, light mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE
)
@Preview(
    name = "dynamic colors - red, dark mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE
)
@Preview(
    name = "dynamic colors - red, light mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE
)
@Preview(
    name = "dynamic colors - green, dark mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE
)
@Preview(
    name = "dynamic colors - green, light mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE
)
@Preview(
    name = "dynamic colors - yellow, dark mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = Wallpapers.YELLOW_DOMINATED_EXAMPLE
)
@Preview(
    name = "dynamic colors - yellow, light mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    wallpaper = Wallpapers.YELLOW_DOMINATED_EXAMPLE
)
@Preview(name = "1dark mode", apiLevel = 30, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "2light mode", apiLevel = 30, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class MaterialPreview

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PreviewContainer(
    content: @Composable () -> Unit
) {
    MusicAppTheme {
        Scaffold {
            content.invoke()
        }
    }
}
