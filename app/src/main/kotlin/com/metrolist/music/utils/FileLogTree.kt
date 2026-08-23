/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.utils

import android.content.Context
import android.util.Log
import com.metrolist.music.constants.LogErrorsOnlyKey
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Persists Timber logs to a capped file on disk, so a user can retrieve
 * recent logs for a bug report (e.g. an Android Auto crash) without needing
 * adb or a separate logging app — see [logFile]/readLogFile in the Debug
 * settings section.
 *
 * Capped at [MAX_BYTES] and trimmed back to [TRIM_TO_BYTES] once exceeded, so
 * a chatty session can't grow this file unbounded.
 */
class FileLogTree(context: Context) : Timber.Tree() {
    private val file = logFile(context)
    private val dateFormat = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US)

    init {
        errorsOnly.set(context.dataStore.get(LogErrorsOnlyKey, false))
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (errorsOnly.get() && priority < Log.WARN) return

        val line = buildString {
            append(dateFormat.format(Date()))
            append(' ')
            append(priorityLabel(priority))
            append('/')
            append(tag ?: "TuneTube")
            append(": ")
            append(message)
            if (t != null) {
                append('\n')
                append(Log.getStackTraceString(t))
            }
            append('\n')
        }

        synchronized(this) {
            try {
                file.appendText(line)
                if (file.length() > MAX_BYTES) trim()
            } catch (_: Exception) {
                // Logging must never crash the app it's trying to help debug.
            }
        }
    }

    private fun trim() {
        val bytes = file.readBytes()
        val start = (bytes.size - TRIM_TO_BYTES.toInt()).coerceAtLeast(0)
        val kept = bytes.copyOfRange(start, bytes.size)
        file.writeBytes(kept)
    }

    private fun priorityLabel(priority: Int) = when (priority) {
        Log.VERBOSE -> "V"
        Log.DEBUG -> "D"
        Log.INFO -> "I"
        Log.WARN -> "W"
        Log.ERROR -> "E"
        Log.ASSERT -> "A"
        else -> "?"
    }

    companion object {
        private const val MAX_BYTES = 1_500_000L
        private const val TRIM_TO_BYTES = 1_000_000L

        // In-memory mirror of LogErrorsOnlyKey, checked on every log() call —
        // a DataStore read per log line would be far too slow. Seeded from the
        // persisted preference when the tree is planted (App.onCreate) and
        // updated directly (not just via DataStore) by the Debug settings
        // toggle, so flipping it takes effect immediately without a restart.
        val errorsOnly = AtomicBoolean(false)

        fun logFile(context: Context): File = File(context.cacheDir, "app_log.txt")

        fun readLogText(context: Context): String =
            logFile(context).takeIf { it.exists() }?.readText().orEmpty()
    }
}
