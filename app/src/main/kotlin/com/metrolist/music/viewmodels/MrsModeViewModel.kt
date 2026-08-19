/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metrolist.music.constants.ActiveProfileKey
import com.metrolist.music.constants.MrsAccountChannelHandleKey
import com.metrolist.music.constants.MrsAccountEmailKey
import com.metrolist.music.constants.MrsAccountNameKey
import com.metrolist.music.constants.MrsDataSyncIdKey
import com.metrolist.music.constants.MrsInnerTubeCookieKey
import com.metrolist.music.constants.MrsModeProfile
import com.metrolist.music.constants.MrsVisitorDataKey
import com.metrolist.music.extensions.toEnum
import com.metrolist.music.utils.MrsModeManager
import com.metrolist.music.utils.dataStore
import com.metrolist.music.utils.safeDataStoreEdit
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MrsModeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val manager: MrsModeManager,
) : ViewModel() {
    fun toggle() {
        viewModelScope.launch {
            manager.toggle()
        }
    }

    /**
     * Force-switches back to NORMAL first if MRS is currently active (so the
     * live session/local like-blacklist view are restored correctly), then
     * forgets the stored Mrs account vault.
     */
    fun removeMrsAccount() {
        viewModelScope.launch {
            val current = context.dataStore.data.first()[ActiveProfileKey].toEnum(MrsModeProfile.NORMAL)
            if (current == MrsModeProfile.MRS) {
                manager.toggle()
            }
            context.safeDataStoreEdit { settings ->
                settings.remove(MrsInnerTubeCookieKey)
                settings.remove(MrsVisitorDataKey)
                settings.remove(MrsDataSyncIdKey)
                settings.remove(MrsAccountNameKey)
                settings.remove(MrsAccountEmailKey)
                settings.remove(MrsAccountChannelHandleKey)
            }
        }
    }
}
