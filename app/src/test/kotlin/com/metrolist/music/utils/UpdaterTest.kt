package com.metrolist.music.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.json.JSONArray

@RunWith(RobolectricTestRunner::class)
class UpdaterTest {
    @Test
    fun parsesTuneTubeReleaseArtifacts() {
        val assets = JSONArray(
            """[{"name":"TuneTube.apk","browser_download_url":"https://example.com/TuneTube.apk","size":42},{"name":"TuneTube-with-Google-Cast.apk","browser_download_url":"https://example.com/TuneTube-with-Google-Cast.apk","size":43}]""",
        )

        assertEquals(
            listOf("foss", "gms"),
            Updater.parseAssets(assets).map { it.variant },
        )
    }

    @Test
    fun parsesKmpReleaseArtifact() {
        val response =
            """
            {
              "tag_name": "v1.2.3",
              "body": null,
              "published_at": "2026-09-05T12:00:00Z",
              "assets": [{
                "name": "Metrolist.apk",
                "browser_download_url": "https://example.com/Metrolist.apk",
                "size": 42
              }]
            }
            """.trimIndent()
        val release = checkNotNull(Updater.parseKmpRelease(response))

        assertEquals("1.2.3", release.versionName)
        assertEquals("", release.description)
        assertEquals("https://example.com/Metrolist.apk", release.assets.single().downloadUrl)
        assertNull(Updater.parseKmpRelease(response.replace("Metrolist.apk", "Metrolist-with-Google-Cast.apk")))
    }
}
