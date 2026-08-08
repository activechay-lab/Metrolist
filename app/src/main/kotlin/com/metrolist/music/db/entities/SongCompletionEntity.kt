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
 * Tracks how many times a song has been played to completion (reached the
 * end of playback naturally, not skipped/stopped early). Used to drive
 * auto-like-after-N-completions.
 */
@Immutable
@Entity(
    tableName = "song_completion",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class SongCompletionEntity(
    @PrimaryKey val songId: String,
    val count: Int = 0,
)
