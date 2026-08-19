/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.db.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.LocalDateTime

/**
 * Per-profile (Normal vs Mrs) liked/blacklisted/downloaded state for a song.
 * [SongEntity]'s own `liked`/`blacklisted`/`isDownloaded` columns keep meaning
 * "the active profile's view" as they always have; a trigger (see
 * MIGRATION_43_44/MIGRATION_44_45) mirrors every write to those columns into
 * this table for whichever profile is currently active, and
 * DatabaseDao.swapActiveProfile() restores from here when the active profile
 * changes. Downloaded audio itself is never duplicated — this only scopes
 * which profile sees a song as downloaded; the underlying cached file (keyed
 * by song id in the shared ExoPlayer cache) is reused across profiles.
 */
@Immutable
@Entity(
    tableName = "song_profile_state",
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
data class SongProfileStateEntity(
    val songId: String,
    val profile: String,
    val liked: Boolean = false,
    val likedDate: LocalDateTime? = null,
    val likedReason: String? = null,
    val blacklisted: Boolean = false,
    val blacklistedDate: LocalDateTime? = null,
    val blacklistReason: String? = null,
    val isDownloaded: Boolean = false,
    val dateDownload: LocalDateTime? = null,
)
