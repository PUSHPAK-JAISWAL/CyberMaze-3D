# 🌐 CyberMaze 3D: Syndicate Siege & Maze Warfare

[![Release](https://img.shields.io/github/v/release/PUSHPAK-JAISWAL/cybermaze-3d?color=00C882&label=Release&logo=github)](https://github.com/PUSHPAK-JAISWAL/cybermaze-3d/releases)
[![Build Status](https://img.shields.io/github/actions/workflow/status/PUSHPAK-JAISWAL/cybermaze-3d/android-ci.yml?branch=main&label=CI%20Tests&logo=githubactions)](https://github.com/PUSHPAK-JAISWAL/cybermaze-3d/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-00B4D8.svg)](https://opensource.org/licenses/MIT)
[![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024%2B)-00C882.svg?logo=android)](https://www.android.com/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

> **A unique cyberpunk tactical maze warfare game fusing real-time tactical raiding (Clash Royale squad deployment), fortress maze building (Clash of Clans base defense), real-world outdoor sensor exploration (pedometer & altimeter rewards), Bring-Your-Own-Key (BYOK) LLM tactical intelligence, and an in-app GitHub auto-update pipeline.**

Developed and maintained by **[Pushpak M. Jaiswal](https://github.com/PUSHPAK-JAISWAL)** ([pushpakmjaiswal@gmail.com](mailto:pushpakmjaiswal@gmail.com)).

---

## 🌟 The Game Concept

In **CyberMaze 3D**, you are an elite Cyber Syndicate Architect in Neo-Kyoto. The game combines four interconnected pillars into an addictive gameplay loop:

1. **⚔️ Syndicate Infiltration Raids (`Raid` Tab)**: Real-time strategic assaults on rival syndicate mazes where you manage Elixir to deploy specialized cyber troops and tactical orbital spells to destroy the enemy Quantum Core.
2. **🏰 Cyber Fortress Builder (`Base` Tab)**: Design your own 8×8 neon maze defense around your Core Server by placing walls to funnel invaders into turret kill zones, with a live AI invasion simulator.
3. **🛰️ Real-World Outdoor Radar (`Radar` Tab)**: Connects to your mobile device's actual motion sensors. Walking outside awards **2× Neon Bits**, uncovers physical **Darknet Nodes** with rare **Quantum Nanites**, and climbing hills or stairs charges **Orbital Satellite Airstrikes**.
4. **🧠 Bring Your Own Key (BYOK) LLM Integration**: Multi-provider AI (Gemini, Groq, OpenAI, OpenRouter) providing live tactical reconnaissance, defense security audits, and procedural sector generation.

---

## 🎮 How to Play & Rules Guide

Access the complete **Operator Field Manual** in-game at any time by tapping the **`?`** icon in the top header.

### 1. Infiltration Raids (Combat Rules)
* **Elixir Energy System**: Your Elixir bar recharges in real time up to **10⚡**. Every troop deployment and tactical spell consumes Elixir.
* **Deploying Troops**: Select a Troop Card in the bottom deck, then tap on the perimeter/breach border of the enemy maze to deploy.
* **Squad Units**:
  * **Byte Brawler (3⚡)**: Heavy armored cyber-tank with high HP. Smashes through walls and targets defensive turrets first.
  * **Glitch Sprinter (2⚡)**: High-speed runner that ignores traps and rushes straight to the Core Server.
  * **EMP Specialist (4⚡)**: Ranged hacker that disables and stuns enemy turrets with arc zaps.
  * **Phantom Drone (5⚡)**: Flying hover unit that glides directly over ground walls and mines.
* **Tactical Spells**:
  * **EMP Surge (3⚡)**: Disables all enemy defenses in a 2.5-tile radius for 5 seconds.
  * **Overclock (2⚡)**: Injects an adrenaline stim granting **+80% speed and damage** to all active squad units for 6 seconds.
  * **Orbital Beam (5⚡)**: Massive kinetic airstrike dealing 250 direct damage to target defense structures.
* **Victory Stars & Loot**:
  * ⭐ **1 Star**: Destroy 50% of enemy buildings.
  * ⭐⭐ **2 Stars**: Destroy the Quantum Core Server.
  * ⭐⭐⭐ **3 Stars**: 100% Total Wipeout!
  * Earn Neon Bits and Trophies to climb from **Bronze Hacker League** up to **Apex Master League**.

### 2. Base Defense Builder (Fortress Rules)
* **Protect the Core Server**: If an attacker destroys your Quantum Core Server, they score 2 stars and plunder your vault.
* **Neon Walls**: Solid barriers that force enemy infiltrators into long, winding corridors, exposing them to continuous turret fire.
* **Defensive Arsenal**:
  * **Pulse Laser Turret**: Rapid-fire single-target laser with continuous beam tracking.
  * **Tesla Shock Pylon**: Arc-lightning coil that zaps up to 3 clustered enemies at once.
  * **Plasma Mortar**: Long-range ballistic launcher dealing explosive area splash damage.
  * **Stealth Glitch Mine**: Invisible proximity trap that detonates when stepped on for 180 splash damage.
* **Live Test Defense**: Tap **"TEST DEFENSE"** to simulate an incoming AI syndicate squad assaulting your maze layout.

---

## 🛰️ Why Go Outside? (Real-World Sensor Compulsion Loop)

CyberMaze 3D actively incentivizes real-world physical activity through genuine device hardware sensors (`Sensor.TYPE_STEP_DETECTOR`, `Sensor.TYPE_PRESSURE` barometer, and accelerometer):

```
       ┌────────────────────────────────────────────────────────┐
       │                  1. STEP OUTSIDE                       │
       │  Physical walking tracks steps, distance & elevation   │
       │  - Pedometer awards 2× Neon Bits per physical step     │
       │  - Barometer tracks vertical elevation climbed         │
       └───────────────────────────┬────────────────────────────┘
                                   │
                                   ▼
       ┌────────────────────────────────────────────────────────┐
       │              2. CLAIM REAL-WORLD NODES                 │
       │  360° Circular Geo-Radar detects physical caches:       │
       │  - 100 Steps (80m)  : Scout Signal Cache (+5 Nanites)  │
       │  - 250 Steps + Alt  : High-Altitude Satellite Relay    │
       │  - 500 Steps (350m) : Darknet Vault (Troop Blueprints) │
       │  - 1000 Steps (600m): Apex Quantum Core (Legendary)    │
       └───────────────────────────┬────────────────────────────┘
                                   │
                                   ▼
       ┌────────────────────────────────────────────────────────┐
       │          3. UPGRADE SQUAD & FORTRESS DECK              │
       │  Level up Brawlers, Sprinters, Phantoms, and Turrets   │
       │  using Bits and Quantum Nanites                        │
       └───────────────────────────┬────────────────────────────┘
                                   │
                                   ▼
       ┌────────────────────────────────────────────────────────┐
       │              4. CRUSH SYNDICATE RAIDS                  │
       │  Dominate rival sectors with 3-Star victories and      │
       │  advance to the Apex Master League                     │
       └───────────────────────────┬────────────────────────────┘
                                   │ (Need more resources?)
                                   └────────────► Step outside!
```

* **⛰️ Hill & Stair Climbing**: Climbing **+5 meters of vertical elevation** outside charges the **Orbital Ion Satellite**, granting free orbital airstrikes in raids!
* **⚔️ Pokémon GO Proximity Player Base Raids**: When another player with CyberMaze is physically nearby, their custom base pings your Geo-Radar in red. Tap **"INFILTRATE & ATTACK THIS PLAYER'S BASE"** to infiltrate their defense maze, crack their Quantum Core, and loot their Bits & Trophies!
* **🕹️ Indoor Testing**: For computer emulators or staying indoors, tap **"RECON DRONE (+150m)"** in the Radar tab to test features without blocking progression.

---

## 🧠 Where Is the LLM Used?

CyberMaze 3D features a complete **Bring-Your-Own-Key (BYOK)** multi-provider LLM pipeline in the **Settings** tab supporting **Google Gemini**, **Groq (Llama-3)**, **OpenAI**, and **OpenRouter**:

1. **🛰️ Live AI Tactical Recon Briefing (`Raid` Tab)**:
   * Tap **"AI INTEL"** in the Raid Arena. The configured model analyzes the rival syndicate's defensive turrets and Core HP, producing real-time tactical advice on chokepoints and boss syndicate dialogue.
2. **🔍 AI Base Defense Security Audit (`Base` Tab)**:
   * Tap **"AI AUDIT"** in the Base Builder. The LLM audits your 8×8 wall layout, identifies security flaws, assigns a defense rating (S, A, B, or C), and provides tactical tips to counter enemy infiltrators.
3. **🌐 Neural Sector Synthesis**:
   * Synthesizes custom procedural syndicate sectors with custom hazard layouts based on real sensor movement vectors.
4. **Offline Procedural Fallback**:
   * If no API key is provided, local neural procedural engines run automatically, ensuring 100% playable functionality out of the box.

---

## 🔄 GitHub Releases In-App Auto-Update System

The app connects directly with GitHub Actions CI/CD to provide over-the-air updates without requiring an app store account:

```
GitHub Repository (git push / tag)
       │
       ▼ (GitHub Actions Runner)
package-apk.yml
 - Deterministic unified debug.keystore
 - Semantic Versioning tags (v1.x.y)
 - Builds CyberMaze-3D.apk
       │
       ▼
GitHub Releases (Assets: CyberMaze-3D.apk)
       ▲
       │ Query api.github.com/repos/.../releases/latest
In-App Client Updates:
 1. VersionUtil.kt: Semantic version comparison (Remote > Local)
 2. UpdateChecker.kt: Queries release tags and APK download URLs
 3. InAppUpdateDownloader.kt: Foreground streaming download with live MB/s progress
 4. ApkPreflightValidator.kt: Verifies certificate signature matches installed app
 5. UpdateInstaller.kt: FileProvider intent triggers Android package installer
```

---

## 🌐 React Introduction Website & GitHub Pages Deployment

The project includes an **introduction website** built in **React 19 + Vite + Tailwind CSS** located in `/website`:
* **Cyber Aesthetic Theme**: Mirrors the exact Cyber Mint and Neon palette of the game.
* **12 Sectors Explorer**: Interactive overview of all 12 campaign levels and their hazards.
* **Direct APK Download**: Prominent one-tap download button linking to the latest GitHub Releases APK asset.
* **Continuous Deployment Workflow (`.github/workflows/deploy.yml`)**: Automatically builds the React application and deploys it to **GitHub Pages** on push to `main` whenever changes are made in `website/**`.

```bash
# Run website locally
cd website
npm install
npm run dev

# Build website production bundle
npm run build
```

---

## 🛠️ Architecture & Tech Stack

* **Language**: Kotlin 2.0+ (100% Coroutines & Flow)
* **UI Toolkit**: Jetpack Compose with Material Design 3 (M3)
* **Graphics**: 3D axonometric math and custom `Canvas` rendering with painter's depth sorting
* **Local Database**: SQLite Room Database (`AppDatabase`, `LevelEntity`, `MovementLogEntity`, `GameSettingsEntity`)
* **Sensors**: Android Sensor Framework (`TYPE_STEP_DETECTOR`, `TYPE_PRESSURE`, `TYPE_ACCELEROMETER`, `TYPE_ROTATION_VECTOR`)
* **Networking**: OkHttp 4 for GitHub Releases API and LLM REST streaming
* **Testing**: Robolectric 4 for local JVM unit tests (`ExampleRobolectricTest.kt`)

---

## 🚀 Building & Running

### Requirements
* Android Studio Ladybug or newer
* JDK 17
* Android SDK 36 (minSdk 24, targetSdk 36)

### Terminal Commands
```bash
# Run unit and Robolectric tests
gradle :app:testDebugUnitTest

# Build debug APK
gradle :app:assembleDebug
```
The compiled APK will be available at `app/build/outputs/apk/debug/app-debug.apk`.
