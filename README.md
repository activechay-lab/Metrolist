<div align="center">

<img src="https://github.com/MetrolistGroup/Metrolist/blob/main/fastlane/metadata/android/en-US/images/icon.png" alt="TuneTube app icon" width="200" />

# TuneTube

### YouTube Music client for Android — a personal fork of Metrolist

<br/>

<a href="https://www.blacksmith.sh">
  <img src="https://github.com/MetrolistGroup/Metrolist/blob/main/assets/blacksmith-powered.png" alt="CI powered by Blacksmith" width="280" />
</a>

<br/>
<br/>

[![License](https://img.shields.io/github/license/MetrolistGroup/metrolist?style=for-the-badge&labelColor=0d1117)](https://github.com/activechay-lab/Metrolist/blob/main/LICENSE)

<br/>

[**Fork of**](#about-this-fork) · [**Fork features**](#fork-features) · [**Staying in sync**](#staying-in-sync-with-upstream) · [**Features**](#features) · [**Building**](#building) · [**FAQ**](#faq)

</div>

> [!WARNING]
> **Regional Restriction** - If YouTube Music is unavailable in your region, this app will not work without a **VPN or proxy** connecting to a supported region.

---

<div align="center">

<h1><a id="about-this-fork"></a>About This Fork</h1>

</div>

TuneTube is a personal fork of [Metrolist](https://github.com/MetrolistGroup/Metrolist), maintained on the `custom` branch of this repo for my own day-to-day use. It's **branding only**: the app's identity (name, icon labels, crash/report text, in-app links) says TuneTube, but the underlying Kotlin package (`com.metrolist.music`), resource filenames, and most of the codebase are untouched on purpose — so that pulling in upstream Metrolist's changes stays a low-conflict, mostly-mechanical merge instead of a fight against a renamed package tree.

All the credit for the app itself belongs to [Mo Agamy](https://github.com/mostafaalagamy) and the Metrolist project — see [Special Thanks](#special-thanks) below.

---

<div align="center">

<h1><a id="fork-features"></a>Fork Features</h1>

</div>

On top of everything upstream Metrolist provides, this fork adds:

- **Mrs Mode** — link a second YouTube Music account and swap the entire personalized experience (Home, radio, autoplay, likes, history) over to it with one toggle
- **Blacklist manager** — a unified screen for blacklisting songs, artists, and albums, with an overflow-menu shortcut from the artist screen and a toggle button right on the Android Auto now-playing controls
- **Smart automation** — auto-like songs on repeat listens, auto-blacklist on fast skips, and auto-delete podcast episodes after they've been played a configurable number of times
- **Bulk actions & auto-liked tracking** on the blacklist/liked screens, so cleaning up a library doesn't mean tapping one song at a time
- **Launcher name switcher & custom color picker** — pick from several alternate home-screen names/icons and a custom accent color, right from Settings
- **Fork-sync automation** (see below) — this repo stays mergeable against upstream without manual babysitting

---

<div align="center">

<h1><a id="staying-in-sync-with-upstream"></a>Staying in Sync With Upstream</h1>

</div>

This repo has three git remotes and two branches set up specifically so upstream Metrolist releases can be pulled in with minimal friction:

- **`origin`** — this fork (`activechay-lab/Metrolist`)
- **`upstream`** — the real Metrolist project (`https://github.com/metrolistgroup/metrolist.git`)
- **`main`** — kept as a byte-for-byte mirror of `upstream/main`, fast-forwarded automatically every night by [`sync-main.yml`](.github/workflows/sync-main.yml). Never commit to `main` directly.
- **`custom`** — the actual working branch (this fork's default). All TuneTube branding and fork features live here, on top of upstream's history.

Every night, [`check-custom-conflicts.yml`](.github/workflows/check-custom-conflicts.yml) does a dry-run merge of the freshly-synced `upstream/main` into `custom` and fails loudly (which triggers a GitHub notification) if it would conflict — so conflicts get caught early instead of being discovered mid-merge weeks later.

To actually pull in an upstream update once `main` is synced:

```bash
git fetch upstream
git checkout custom
git merge upstream/main
```

Since the package/namespace and most of the codebase are unchanged from upstream, this is usually a clean, mechanical merge. CI (`build.yml`, `build_pr.yml`, `build_quick.yml`, `release.yml`) builds and releases from `custom` on every push.

---

<div align="center">

<h1><a id="features"></a>Features</h1>

<table>
  <tr>
    <td width="50%" valign="top">

#### Playback
- Stream any song or video from YouTube Music
- Background playback
- Download & cache for offline use
- Skip silence
- Sleep timer

</td>
    <td width="50%" valign="top">

#### Audio
- Audio normalization
- Tempo & pitch control
- Equalizer

</td>
  </tr>
  <tr>
    <td width="50%" valign="top">

#### Lyrics & Discovery
- Live synced lyrics
- AI-powered lyrics translation
- Personalized quick picks
- Search songs, albums, artists, videos, and playlists

</td>
    <td width="50%" valign="top">

#### Library & Account
- Full library management
- Local playlists
- Import playlists
- Reorder songs in playlist or queue
- YouTube Music account login
- Sync songs, artists, albums, and playlists

</td>
  </tr>
  <tr>
    <td width="50%" valign="top">

#### Social
- Listen together with friends in real-time

</td>
    <td width="50%" valign="top">

#### Interface
- Home screen widget
- Light / Dark / Black / Dynamic theme modes
- Dynamic color + 19 preset color palettes
- Built with Material 3

</td>
  </tr>
</table>

</div>

---

<div align="center">

<h1><a id="building"></a>Building</h1>

<h3>This is a personal build, not publicly distributed. See <a href="development_guide.md">development_guide.md</a> for setting up a local dev environment and building the APK yourself, or grab a build from this fork's own <a href="https://github.com/activechay-lab/Metrolist/releases">Releases page</a>.</h3>

</div>

---

<div align="center">

<h1><a id="faq"></a>FAQ</h1>

<h3>Got questions? Check out upstream Metrolist's <a href="https://metrolist.cc/#faq">FAQ page</a> for answers to the most common ones.</h3>

</div>

---

<div align="center">

<h1><a id="special-thanks"></a>Special Thanks</h1>

<h3>TuneTube is a fork of Metrolist, which stands on the shoulders of incredible open-source work.</h3>

<h3>Main Inspirations</h3>

<table>
  <thead>
    <tr>
      <th align="center">Project</th>
      <th align="center">Authors</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td align="center"><strong>Metrolist</strong></td>
      <td align="center"><a href="https://github.com/mostafaalagamy">Mo Agamy</a></td>
    </tr>
    <tr>
      <td align="center"><strong>InnerTune</strong></td>
      <td align="center"><a href="https://github.com/z-huang">Zion Huang</a> · <a href="https://github.com/Malopieds">Malopieds</a></td>
    </tr>
    <tr>
      <td align="center"><strong>OuterTune</strong></td>
      <td align="center"><a href="https://github.com/DD3Boh">Davide Garberi</a> · <a href="https://github.com/mikooomich">Michael Zh</a></td>
    </tr>
  </tbody>
</table>

<h3>Libraries & Integrations</h3>

<table>
  <thead>
    <tr>
      <th align="center">Project</th>
      <th align="center">Contribution</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td align="center"><a href="https://better-lyrics.boidu.dev"><strong>Better Lyrics</strong></a></td>
      <td>Time-synced lyrics with word-by-word highlighting & YouTube Music integration</td>
    </tr>
    <tr>
      <td align="center"><a href="https://github.com/MetrolistGroup/metroserver"><strong>metroserver</strong></a></td>
      <td>Listen-together real-time backend</td>
    </tr>
    <tr>
      <td align="center"><a href="https://github.com/aleksey-saenko/MusicRecognizer"><strong>MusicRecognizer</strong></a></td>
      <td>Music recognition feature & Shazam API integration</td>
    </tr>
    <tr>
      <td align="center"><a href="https://github.com/ZemerTeam/zemer-cipher"><strong>zemer-cipher</strong></a></td>
      <td>YouTube cipher deobfuscation and PoToken generation</td>
    </tr>
    <tr>
      <td align="center"><a href="https://www.blacksmith.sh"><strong>Blacksmith</strong></a></td>
      <td>High-performance GitHub Actions runners powering CI</td>
    </tr>
  </tbody>
</table>

<br/>

<a href="https://www.blacksmith.sh">
  <img src="https://github.com/MetrolistGroup/Metrolist/blob/main/assets/blacksmith-powered.png" alt="CI powered by Blacksmith" width="280" />
</a>

<h3>We also thank the entire open-source community! For every library, tool, and API that powers this project.</h3>

</div>

---

<div align="center">

<h1>Disclaimer</h1>

This project is **not affiliated with, funded, authorized, endorsed by, or in any way associated** with YouTube, Google LLC, Metrolist Group LLC, or any of their affiliates and subsidiaries.

All trademarks, service marks, and intellectual property rights referenced in this project belong to their respective owners.

</div>

---

<div align="center">

<br/>

**A personal fork — original app made with ❤️ by [Mo Agamy](https://github.com/mostafaalagamy)**

**This project stands with Palestine 🇵🇸**

</div>
