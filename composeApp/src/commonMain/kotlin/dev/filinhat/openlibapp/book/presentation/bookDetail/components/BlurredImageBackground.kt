package dev.filinhat.openlibapp.book.presentation.bookDetail.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import dev.filinhat.openlibapp.core.presentation.PulseAnimation
import dev.filinhat.openlibapp.core.presentation.theme.DesertWhite
import openlibrarycmpapp.composeapp.generated.resources.Res
import openlibrarycmpapp.composeapp.generated.resources.book_cover
import openlibrarycmpapp.composeapp.generated.resources.book_error_2
import openlibrarycmpapp.composeapp.generated.resources.btn_go_back
import openlibrarycmpapp.composeapp.generated.resources.btn_mark_as_favorite
import openlibrarycmpapp.composeapp.generated.resources.btn_remove_from_favorites
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun BlurredImageBackground(
    imageUrl: String?,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var imageLoadResult by remember { mutableStateOf<Result<Painter>?>(null) }
    val painter =
        rememberAsyncImagePainter(
            model = imageUrl,
            onSuccess = {
                val size = it.painter.intrinsicSize
                imageLoadResult =
                    if (size.width > 1 && size.height > 1) {
                        Result.success(it.painter)
                    } else {
                        Result.failure(Exception("Invalid image size"))
                    }
            },
            onError = {
                it.result.throwable.stackTraceToString()
            },
        )

    Box(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier =
                    Modifier
                        .weight(0.3f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background),
            ) {
                imageLoadResult?.getOrNull()?.let { painter ->
                    Image(
                        painter = painter,
                        contentDescription = stringResource(Res.string.book_cover),
                        contentScale = ContentScale.Crop,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .blur(20.dp),
                    )
                }
            }

            Box(
                modifier =
                    Modifier
                        .weight(0.7f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
            }
        }

        IconButton(
            onClick = onBackClick,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 16.dp, start = 16.dp)
                    .statusBarsPadding(),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.btn_go_back),
                tint = DesertWhite,
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.fillMaxHeight(0.15f))

            ElevatedCard(
                modifier =
                    Modifier
                        .height(250.dp)
                        .aspectRatio(2 / 3f),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 15.dp),
            ) {
                AnimatedContent(
                    targetState = imageLoadResult,
                ) { result ->
                    when (result) {
                        null ->
                            Box(
                                modifier =
                                    Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                PulseAnimation(
                                    modifier = Modifier.size(60.dp),
                                )
                            }

                        else -> {
                            Box {
                                Image(
                                    painter = if (result.isSuccess) painter else painterResource(Res.drawable.book_error_2),
                                    contentDescription = stringResource(Res.string.book_cover),
                                    contentScale = if (result.isSuccess) ContentScale.Crop else ContentScale.Fit,
                                    modifier =
                                        Modifier
                                            .background(Color.Transparent)
                                            .fillMaxSize(),
                                )

                                IconButton(
                                    onClick = onFavoriteClick,
                                    modifier =
                                        Modifier
                                            .align(Alignment.BottomEnd)
                                            .background(
                                                brush =
                                                    Brush.radialGradient(
                                                        colors = listOf(MaterialTheme.colorScheme.tertiary, Color.Transparent),
                                                        radius = 80f,
                                                    ),
                                            ),
                                ) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription =
                                            if (isFavorite) {
                                                stringResource(Res.string.btn_remove_from_favorites)
                                            } else {
                                                stringResource(
                                                    Res.string.btn_mark_as_favorite,
                                                )
                                            },
                                        tint = MaterialTheme.colorScheme.tertiary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            content()
        }
    }
}
