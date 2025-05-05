package pl.skolimowski.musicapp.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import pl.skolimowski.musicapp.R

@Composable
fun CoverPhoto(
    imageUrl: String?,
    contentDescription: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        placeholder = painterResource(R.drawable.ic_album_placeholder),
        error = painterResource(R.drawable.ic_album_placeholder),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier.size(size)
    )
} 