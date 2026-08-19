/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.db.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Tracks how many times a song has been skipped quickly (abandoned within
 * [com.metrolist.music.playback.MusicService.FAST_SKIP_THRESHOLD_MS] without
 * reaching the end), separately per Mrs Mode profile. Used to drive
 * auto-blacklist-after-N-fast-skips.
 */
@Immutable
@Entity(
    tableName = "song_skip",
    primaryKeys = ["songId", "profile"],
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["songId"])],
)
data class SongSkipEntity(
    val songId: String,
    val profile: String = "NORMAL",
    val count: Int = 0,
)
