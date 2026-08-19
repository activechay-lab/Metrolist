/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.utils

import android.content.Context
import com.metrolist.innertube.YouTube
import com.metrolist.music.constants.AccountChannelHandleKey
import com.metrolist.music.constants.AccountEmailKey
import com.metrolist.music.constants.AccountNameKey
import com.metrolist.music.constants.ActiveProfileKey
import com.metrolist.music.constants.DataSyncIdKey
import com.metrolist.music.constants.InnerTubeCookieKey
import com.metrolist.music.constants.MrsAccountChannelHandleKey
import com.metrolist.music.constants.MrsAccountEmailKey
import com.metrolist.music.constants.MrsAccountNameKey
import com.metrolist.music.constants.MrsDataSyncIdKey
import com.metrolist.music.constants.MrsInnerTubeCookieKey
import com.metrolist.music.constants.MrsModeProfile
import com.metrolist.music.constants.MrsVisitorDataKey
import com.metrolist.music.constants.NormalAccountChannelHandleKey
import com.metrolist.music.constants.NormalAccountEmailKey
import com.metrolist.music.constants.NormalAccountNameKey
import com.metrolist.music.constants.NormalDataSyncIdKey
import com.metrolist.music.constants.NormalInnerTubeCookieKey
import com.metrolist.music.constants.NormalVisitorDataKey
import com.metrolist.music.constants.VisitorDataKey
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.extensions.toEnum
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single entrypoint for switching between the Normal and Mrs Mode profiles.
 *
 * This is a credential-vault swap, not a recommendation engine: each profile's
 * YouTube Music session (cookie/visitorData/dataSyncId/account info) is kept in
 * its own set of DataStore keys, and toggling copies the active profile's
 * values into the live keys ([InnerTubeCookieKey] etc.) that [com.metrolist.music.App]
 * already watches reactively. This deliberately never touches the player or
 * media session, so switching never interrupts the currently playing song.
 */
@Singleton
class MrsModeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: MusicDatabase,
    private val syncUtils: SyncUtils,
) {
    /**
     * Flips the active profile. Returns the newly active profile, or null if
     * the toggle could not proceed (switching to MRS with no Mrs account
     * configured yet).
     */
    suspend fun toggle(): MrsModeProfile? {
        val prefs = context.dataStore.data.first()
        val current = prefs[ActiveProfileKey].toEnum(MrsModeProfile.NORMAL)
        val target = current.toggle()

        // First-ever toggle on an existing install: the live keys today ARE the
        // Normal profile's session, but nothing has backfilled the Normal vault
        // yet. Do that now so the swap below has something to restore later.
        if (prefs[NormalInnerTubeCookieKey] == null && prefs[InnerTubeCookieKey] != null) {
            context.safeDataStoreEdit { settings ->
                settings[NormalInnerTubeCookieKey] = settings[InnerTubeCookieKey].orEmpty()
                settings[NormalVisitorDataKey] = settings[VisitorDataKey].orEmpty()
                settings[NormalDataSyncIdKey] = settings[DataSyncIdKey].orEmpty()
                settings[NormalAccountNameKey] = settings[AccountNameKey].orEmpty()
                settings[NormalAccountEmailKey] = settings[AccountEmailKey].orEmpty()
                settings[NormalAccountChannelHandleKey] = settings[AccountChannelHandleKey].orEmpty()
            }
        }

        if (target == MrsModeProfile.MRS && prefs[MrsInnerTubeCookieKey].isNullOrBlank()) {
            Timber.w("MrsModeManager: no Mrs account configured, aborting toggle")
            return null
        }

        val saved = context.safeDataStoreEdit { settings ->
            // Snapshot the OUTGOING profile's current live values back into its
            // own vault first — defensive, covers a session silently refreshed
            // (e.g. a new visitorData minted) since it was last written.
            if (current == MrsModeProfile.NORMAL) {
                settings[NormalInnerTubeCookieKey] = settings[InnerTubeCookieKey].orEmpty()
                settings[NormalVisitorDataKey] = settings[VisitorDataKey].orEmpty()
                settings[NormalDataSyncIdKey] = settings[DataSyncIdKey].orEmpty()
                settings[NormalAccountNameKey] = settings[AccountNameKey].orEmpty()
                settings[NormalAccountEmailKey] = settings[AccountEmailKey].orEmpty()
                settings[NormalAccountChannelHandleKey] = settings[AccountChannelHandleKey].orEmpty()
            } else {
                settings[MrsInnerTubeCookieKey] = settings[InnerTubeCookieKey].orEmpty()
                settings[MrsVisitorDataKey] = settings[VisitorDataKey].orEmpty()
                settings[MrsDataSyncIdKey] = settings[DataSyncIdKey].orEmpty()
                settings[MrsAccountNameKey] = settings[AccountNameKey].orEmpty()
                settings[MrsAccountEmailKey] = settings[AccountEmailKey].orEmpty()
                settings[MrsAccountChannelHandleKey] = settings[AccountChannelHandleKey].orEmpty()
            }

            // Copy the TARGET profile's vault into the live keys.
            if (target == MrsModeProfile.NORMAL) {
                settings[InnerTubeCookieKey] = settings[NormalInnerTubeCookieKey].orEmpty()
                settings[VisitorDataKey] = settings[NormalVisitorDataKey].orEmpty()
                settings[DataSyncIdKey] = settings[NormalDataSyncIdKey].orEmpty()
                settings[AccountNameKey] = settings[NormalAccountNameKey].orEmpty()
                settings[AccountEmailKey] = settings[NormalAccountEmailKey].orEmpty()
                settings[AccountChannelHandleKey] = settings[NormalAccountChannelHandleKey].orEmpty()
            } else {
                settings[InnerTubeCookieKey] = settings[MrsInnerTubeCookieKey].orEmpty()
                settings[VisitorDataKey] = settings[MrsVisitorDataKey].orEmpty()
                settings[DataSyncIdKey] = settings[MrsDataSyncIdKey].orEmpty()
                settings[AccountNameKey] = settings[MrsAccountNameKey].orEmpty()
                settings[AccountEmailKey] = settings[MrsAccountEmailKey].orEmpty()
                settings[AccountChannelHandleKey] = settings[MrsAccountChannelHandleKey].orEmpty()
            }

            // Flip the active-profile flag atomically with the swap above, so
            // there's never a window where they disagree.
            settings[ActiveProfileKey] = target.name
        }
        if (!saved) return null

        // Apply the swap to the live YouTube client SYNCHRONOUSLY, rather than
        // waiting for App.kt's reactive DataStore collectors to pick up the
        // writes above — those run on their own independently-scheduled
        // coroutine and are not guaranteed to have processed the change by
        // the time this function returns. Any server call made right after
        // toggle() returns (e.g. fetching the new profile's Liked Music to
        // seed "jump to her music") must already be authenticated as the new
        // account, not racing that Flow emission.
        val livePrefs = context.dataStore.data.first()
        YouTube.cookie = livePrefs[InnerTubeCookieKey]
        YouTube.visitorData = livePrefs[VisitorDataKey]?.takeIf { it != "null" }
        YouTube.dataSyncId = livePrefs[DataSyncIdKey]?.let {
            it.takeIf { s -> !s.contains("||") }
                ?: it.takeIf { s -> s.endsWith("||") }?.substringBefore("||")
                ?: it.substringAfter("||")
        }

        // Swap the local liked/blacklisted view (see DatabaseDao.swapActiveProfile).
        database.query { swapActiveProfile(target.name) }

        // Force an immediate re-sync of account-tied library data (playlists,
        // liked songs, etc.) under the new session — bypasses the normal
        // cooldown on purpose, since the account just changed underneath it.
        syncUtils.performFullSync()

        return target
    }
}
