/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.db.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Tracks how many times a song has been skipped quickly (abandoned within
 * [com.metrolist.music.playback.MusicService.FAST_SKIP_THRESHOLD_MS] without
 * reaching the end). Used to drive auto-blacklist-after-N-fast-skips.
 */
@Immutable
@Entity(
    tableName = "song_skip",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class SongSkipEntity(
    @PrimaryKey val songId: String,
    val count: Int = 0,
)
