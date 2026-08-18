/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.library

import java.time.format.DateTimeFormatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.metrolist.music.LocalPlayerAwareWindowInsets
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.R
import com.metrolist.music.extensions.toMediaItem
import com.metrolist.music.playback.queues.ListQueue
import com.metrolist.music.ui.component.ChipsRow
import com.metrolist.music.ui.component.DefaultDialog
import com.metrolist.music.ui.component.DraggableScrollbar
import com.metrolist.music.ui.component.EmptyPlaceholder
import com.metrolist.music.ui.component.IconButton
import com.metrolist.music.ui.component.LibraryArtistListItem
import com.metrolist.music.ui.component.LibraryAlbumListItem
import com.metrolist.music.ui.component.LocalMenuState
import com.metrolist.music.ui.component.SongListItem
import com.metrolist.music.ui.menu.SongMenu
import com.metrolist.music.ui.utils.backToMain
import com.metrolist.music.viewmodels.BlacklistViewModel

private enum class BlacklistFilter {
    SONGS,
    ARTISTS,
    ALBUMS,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlacklistScreen(
    navController: NavController,
    viewModel: BlacklistViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val coroutineScope = rememberCoroutineScope()
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsStateWithLifecycle()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsStateWithLifecycle()

    var filter by rememberSaveable { mutableStateOf(BlacklistFilter.SONGS) }
    val blacklistTitle = stringResource(R.string.blacklist)

    val songs by viewModel.blacklistedSongs.collectAsStateWithLifecycle()
    val artists by viewModel.blacklistedArtists.collectAsStateWithLifecycle()
    val albums by viewModel.blacklistedAlbums.collectAsStateWithLifecycle()

    val blacklistDateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm") }

    var pendingBulkRemoval by remember { mutableStateOf<String?>(null) }

    pendingBulkRemoval?.let { action ->
        val matchingCount = if (action == "auto") {
            songs.count { it.song.blacklistReason == null || it.song.blacklistReason == "auto" }
        } else {
            songs.count { it.song.blacklistReason == "manual" }
        }
        val titleRes = if (action == "auto") R.string.remove_all_auto_blacklisted else R.string.remove_all_manual_blacklisted
        val confirmRes = if (action == "auto") {
            R.string.remove_all_auto_blacklisted_confirm
        } else {
            R.string.remove_all_manual_blacklisted_confirm
        }
        DefaultDialog(
            onDismiss = { pendingBulkRemoval = null },
            title = { Text(stringResource(titleRes)) },
            content = {
                Text(
                    text = stringResource(confirmRes, matchingCount),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 18.dp),
                )
            },
            buttons = {
                TextButton(onClick = { pendingBulkRemoval = null }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
                TextButton(
                    onClick = {
                        if (action == "auto") {
                            viewModel.removeAllAutoBlacklisted()
                        } else {
                            viewModel.removeAllManualBlacklisted()
                        }
                        pendingBulkRemoval = null
                    },
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            },
        )
    }

    val state = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = state,
            contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
        ) {
            item(key = "filter") {
                ChipsRow(
                    chips = listOf(
                        BlacklistFilter.SONGS to stringResource(R.string.filter_songs),
                        BlacklistFilter.ARTISTS to stringResource(R.string.filter_artists),
                        BlacklistFilter.ALBUMS to stringResource(R.string.filter_albums),
                    ),
                    currentValue = filter,
                    onValueUpdate = { filter = it },
                )
            }

            when (filter) {
                BlacklistFilter.SONGS -> {
                    if (songs.isEmpty()) {
                        item(key = "songs_empty") {
                            EmptyPlaceholder(
                                icon = R.drawable.music_note,
                                text = stringResource(R.string.blacklist_songs_empty),
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = songs,
                            key = { _, song -> song.id },
                        ) { index, song ->
                            val reasonRes = if (song.song.blacklistReason == "manual") {
                                R.string.blacklist_reason_manual
                            } else {
                                R.string.blacklist_reason_auto
                            }
                            val dateText = song.song.blacklistedDate
                                ?.format(blacklistDateFormatter)
                                .orEmpty()
                            val subtitle = stringResource(
                                R.string.blacklisted_on,
                                dateText,
                                stringResource(reasonRes),
                            )

                            SongListItem(
                                song = song,
                                isActive = song.song.id == mediaMetadata?.id,
                                isPlaying = isPlaying,
                                showInLibraryIcon = true,
                                subtitleOverride = subtitle,
                                trailingContent = {
                                    IconButton(
                                        onClick = {
                                            menuState.show {
                                                SongMenu(
                                                    originalSong = song,
                                                    onDismiss = menuState::dismiss,
                                                )
                                            }
                                        },
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.more_vert),
                                            contentDescription = null,
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (song.song.id == mediaMetadata?.id) {
                                            playerConnection.togglePlayPause()
                                        } else {
                                            playerConnection.playQueue(
                                                ListQueue(
                                                    title = blacklistTitle,
                                                    items = songs.map { it.toMediaItem(allowBlacklistedPlayback = true) },
                                                    startIndex = index,
                                                ),
                                            )
                                        }
                                    }
                                    .animateItem(),
                            )
                        }
                    }
                }

                BlacklistFilter.ARTISTS -> {
                    if (artists.isEmpty()) {
                        item(key = "artists_empty") {
                            EmptyPlaceholder(
                                icon = R.drawable.artist,
                                text = stringResource(R.string.blacklist_artists_empty),
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = artists,
                            key = { _, artist -> artist.id },
                        ) { _, artist ->
                            LibraryArtistListItem(
                                menuState = menuState,
                                coroutineScope = coroutineScope,
                                artist = artist,
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }

                BlacklistFilter.ALBUMS -> {
                    if (albums.isEmpty()) {
                        item(key = "albums_empty") {
                            EmptyPlaceholder(
                                icon = R.drawable.album,
                                text = stringResource(R.string.blacklist_albums_empty),
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = albums,
                            key = { _, album -> album.id },
                        ) { _, album ->
                            LibraryAlbumListItem(
                                menuState = menuState,
                                album = album,
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
            }
        }

        DraggableScrollbar(
            modifier = Modifier
                .padding(
                    LocalPlayerAwareWindowInsets.current
                        .union(WindowInsets.ime)
                        .asPaddingValues(),
                )
                .align(Alignment.CenterEnd),
            scrollState = state,
            headerItems = 1,
        )

        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.blacklist),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = { navController.navigateUp() },
                    onLongClick = { navController.backToMain() },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = null,
                    )
                }
            },
            actions = {
                if (filter == BlacklistFilter.SONGS) {
                    var showBulkMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showBulkMenu = true }) {
                            Icon(
                                painter = painterResource(R.drawable.more_vert),
                                contentDescription = null,
                            )
                        }
                        DropdownMenu(
                            expanded = showBulkMenu,
                            onDismissRequest = { showBulkMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.remove_all_auto_blacklisted)) },
                                onClick = {
                                    showBulkMenu = false
                                    pendingBulkRemoval = "auto"
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.remove_all_manual_blacklisted)) },
                                onClick = {
                                    showBulkMenu = false
                                    pendingBulkRemoval = "manual"
                                },
                            )
                        }
                    }
                }
            },
        )
    }
}
