/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.db.entities.Album
import com.metrolist.music.db.entities.Artist
import com.metrolist.music.db.entities.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlacklistViewModel
@Inject
constructor(
    private val database: MusicDatabase,
) : ViewModel() {
    val blacklistedSongs =
        database.blacklistedSongs()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList<Song>())

    val blacklistedArtists =
        database.blacklistedArtists()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList<Artist>())

    val blacklistedAlbums =
        database.blacklistedAlbums()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList<Album>())

    // Local-only: blacklisting has no YouTube-sync component.
    fun removeAllAutoBlacklisted() {
        viewModelScope.launch(Dispatchers.IO) { database.unblacklistAllAuto() }
    }

    fun removeAllManualBlacklisted() {
        viewModelScope.launch(Dispatchers.IO) { database.unblacklistAllManual() }
    }
}
