package com.nononsenseapps.feeder.ui.compose.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.compose.minimumTouchSize
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemDateStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemFeedTitleStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemTitleTextStyle
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens

@Composable
fun FeedItemSuperCompact(
    item: FeedListItem,
    showThumbnail: Boolean,
    imagePainter: @Composable (String) -> Unit,
    modifier: Modifier = Modifier,
    onMarkAboveAsRead: () -> Unit,
    onMarkBelowAsRead: () -> Unit,
    onShareItem: () -> Unit,
    onTogglePinned: () -> Unit,
    onToggleBookmarked: () -> Unit,
    dropDownMenuExpanded: Boolean,
    onDismissDropdown: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .padding(
                start = LocalDimens.current.margin,
                end = if (item.imageUrl?.isNotBlank() != true || !showThumbnail) LocalDimens.current.margin else 0.dp
            )
            .height(IntrinsicSize.Min)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .weight(weight = 1.0f, fill = true)
                .requiredHeightIn(min = minimumTouchSize)
                .padding(vertical = 4.dp)
        ) {
            val titleAlpha = if (item.shouldBeShownAsUnread) {
                ContentAlpha.high
            } else {
                ContentAlpha.medium
            }
            CompositionLocalProvider(LocalContentAlpha provides titleAlpha) {
                Text(
                    text = item.title,
                    style = FeedListItemTitleTextStyle(),
                    modifier = Modifier
                        .padding(start = 4.dp, end = 4.dp)
                )
            }
            // Want the dropdown to center on the middle text row
            Box {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = buildAnnotatedString {
                                if (item.pubDate.isNotBlank()) {
                                    append("${item.pubDate} ‧ ")
                                }
                                withStyle(FeedListItemFeedTitleStyle().toSpanStyle()) {
                                    append(item.feedTitle)
                                }
                            },
                            style = FeedListItemDateStyle(),
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, end = 4.dp)
                        )
                    }
                }
                DropdownMenu(
                    expanded = dropDownMenuExpanded,
                    onDismissRequest = onDismissDropdown
                ) {
                    DropdownMenuItem(
                        onClick = {
                            onDismissDropdown()
                            onTogglePinned()
                        },
                        text = {
                            Text(
                                text = stringResource(
                                    when (item.pinned) {
                                        true -> R.string.unpin_article
                                        false -> R.string.pin_article
                                    }
                                )
                            )
                        }
                    )
                    DropdownMenuItem(
                        onClick = {
                            onDismissDropdown()
                            onToggleBookmarked()
                        },
                        text = {
                            Text(
                                text = stringResource(
                                    when (item.bookmarked) {
                                        true -> R.string.remove_bookmark
                                        false -> R.string.bookmark_article
                                    }
                                )
                            )
                        }
                    )
                    DropdownMenuItem(
                        onClick = {
                            onDismissDropdown()
                            onMarkAboveAsRead()
                        },
                        text = {
                            Text(
                                text = stringResource(id = R.string.mark_items_above_as_read)
                            )
                        }
                    )
                    DropdownMenuItem(
                        onClick = {
                            onDismissDropdown()
                            onMarkBelowAsRead()
                        },
                        text = {
                            Text(
                                text = stringResource(id = R.string.mark_items_below_as_read)
                            )
                        }
                    )
                    DropdownMenuItem(
                        onClick = {
                            onDismissDropdown()
                            onShareItem()
                        },
                        text = {
                            Text(
                                text = stringResource(R.string.share)
                            )
                        }
                    )
                }
            }
        }

        if (showThumbnail) {
            item.imageUrl?.let { imageUrl ->
                imagePainter(imageUrl)
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun preview() {
    FeedItemSuperCompact(
        item = FeedListItem(
            title = "title",
            snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
            feedTitle = "Super Duper Feed One two three hup di too dasf",
            pubDate = "Jun 9, 2021",
            unread = true,
            imageUrl = null,
            link = null,
            id = ID_UNSET,
            pinned = false,
            bookmarked = false,
        ),
        showThumbnail = true,
        imagePainter = {},
        onMarkAboveAsRead = {},
        onMarkBelowAsRead = {},
        onShareItem = {},
        onTogglePinned = {},
        onToggleBookmarked = {},
        dropDownMenuExpanded = false,
        onDismissDropdown = {}
    )
}
