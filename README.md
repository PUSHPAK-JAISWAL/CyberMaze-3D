# 🌐 CyberMaze 3D

[![Release](https://img.shields.io/github/v/release/PUSHPAK-JAISWAL/cybermaze-3d?color=00C882&label=Release&logo=github)](https://github.com/PUSHPAK-JAISWAL/cybermaze-3d/releases)
[![Build Status](https://img.shields.io/github/actions/workflow/status/PUSHPAK-JAISWAL/cybermaze-3d/android-ci.yml?branch=main&label=CI%20Tests&logo=githubactions)](https://github.com/PUSHPAK-JAISWAL/cybermaze-3d/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-00B4D8.svg)](https://opensource.org/licenses/MIT)
[![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024%2B)-00C882.svg?logo=android)](https://www.android.com/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Dependabot](https://img.shields.io/badge/Dependabot-Active-02569B.svg?logo=dependabot)](https://github.com/dependabot)

> **A real-world motion-synthesized 3D cyberpunk puzzle game featuring Bring Your Own Key (BYOK) LLM generation, dynamic AI enemy behavior, and an integrated in-app auto-update system.**

Developed and maintained by **[Pushpak M. Jaiswal](https://github.com/PUSHPAK-JAISWAL)** ([pushpakmjaiswal@gmail.com](mailto:pushpakmjaiswal@gmail.com)).

---

## 🌟 Overview

**CyberMaze 3D** transforms your physical daily movement into an interactive 3D labyrinth puzzle:
- **Physical Sensor Tracking**: Your phone tracks vertical elevation gain (meters climbed via barometer), vertical depression (meters descended), and directional paces (linear acceleration & step detection).
- **AI-Driven Map Generation**: Feed your physical movement into any LLM model via **Bring Your Own Key (BYOK)** to automatically construct a customized 3D puzzle map with multi-tiered elevation slabs, hazard pits, energy cores, and security terminals.
- **Dynamic Enemy Tactics**: Drones dynamically adjust their pursuit strategies based on real-time LLM intelligence.
- **Direct GitHub In-App Updates**: Enjoy seamless over-the-air updates directly from GitHub Releases without requiring Google Play.

```
                  PHYSICAL REALITY ──► SENSOR TELEMETRY ──► LLM GENERATION ──► 3D ARENA
 ┌─────────────────────────────┐    ┌───────────────────┐    ┌────────────────┐    ┌──────────────────────────┐
 │  User Walks & Climbs Stairs │ ──►│ Elevation Gain:  │ ──►│ BYOK Prompt:   │ ──►│ 3D Isometric Labyrinth   │
 │  - Real Barometer (Δz)      │    │  +8.5m            │    │  Groq / Router │    │  - Elevation Slabs       │
 │  - Step Counter             │    │ Depression: -2.0m │    │  OpenAI/Gemini │    │  - Hazard Depression Pits│
 │  - Heading & Compass        │    │ Paces: 140 steps  │    │ Model: Any     │    │  - AI Drones & Portals   │
 └─────────────────────────────┘    └───────────────────┘    └────────────────┘    └──────────────────────────┘
```

---

## 🎮 Key Features

### 1. 🕹️ True 3D Isometric Viewport Engine
- Rendered purely in **Jetpack Compose Canvas** using 3D axonometric math and **Painter's Algorithm** depth sorting.
- **360° Orbit Camera**: Drag or use the orbit controls to rotate and view the puzzle from any angle.
- **Dynamic Terrain Geometry**: Raised elevation tiers ($z \ge 1$), sunken depression pits ($z = -1$), laser fences, hacking terminals, and swirling cyber portals.
- **Tactical Actions**: Virtual D-pad, Ascend/Jump ability for cliffs, Terminal Decryption, and Pulse Radar scanner.

### 2. 📡 Mobile Motion & Sensor Telemetry (Motion Lab)
- **Barometric Altimetry**: Tracks meters climbed and descended using atmospheric pressure (`Sensor.TYPE_PRESSURE`) with fallback accelerometer vertical estimation.
- **Live Waveform Visualizer**: Real-time canvas waveform graph showing recent altitude variations and heading.
- **Simulator & Calibration Studio**: Test climbing and pacing directly indoors or on emulators with 1-tap injectors (`+45 Steps`, `+4.2m Climb`, `-2.5m Trench`).

### 3. 🔑 Bring Your Own Key (BYOK) Architecture
- Multi-provider compatibility:
  - **Groq** (`api.groq.com`)
  - **OpenRouter** (`openrouter.ai`)
  - **OpenAI** (`api.openai.com`)
  - **Google Gemini** (`generativelanguage.googleapis.com`)
  - **Custom Endpoints** (Local Ollama, vLLM, or private proxies)
- **Flexible Model Selection**: Not locked into any single model—enter any model identifier (e.g. `llama-3.3-70b-versatile`, `deepseek/deepseek-chat`, `gpt-4o-mini`, `gemini-1.5-flash`).
- **Offline Fallback Synthesizer**: Fully playable out of the box even without network or API keys!

### 4. 🤖 Dynamic AI-Driven Enemy Pursuers
- In-game combat drones (Viper Hunters, Aegis Sentinels, Glitch Phantoms) query your configured LLM for contextual combat decisions, flank angles, and procedural radio chatter.

### 5. 🔄 In-App GitHub Auto-Update System
Connecting GitHub Actions CI/CD directly with Android client devices:
```
┌──────────────────┐            ┌────────────────────────────────────────┐
│  git push / tag  │            │ 1. App launches / user checks updates  │
└────────┬─────────┘            └───────────────────┬────────────────────┘
         │ (Actions Runner)                         │
         ▼                                          ▼
┌──────────────────┐            ┌────────────────────────────────────────┐
│ package-apk.yml  │            │ 2. Query GitHub Releases API           │
│ - Deterministic  │            │    api.github.com/repos/.../releases   │
│   debug.keystore │            └───────────────────┬────────────────────┘
│ - SemVer tags    │                                │
│   (v1-x.0-9.0-9) │                                ▼
│ - Upload APKs    │            ┌────────────────────────────────────────┐
└────────┬─────────┘            │ 3. Semantic Version Comparison         │
         │                      │    (VersionUtil.kt)                    │
         ▼                      └───────────────────┬────────────────────┘
┌──────────────────┐                                │ (Newer version found)
│ GitHub Releases  │◄───────────────────────────────┤
│ CyberMaze-3D.apk │            ┌───────────────────▼────────────────────┐
└──────────────────┘            │ 4. UpdateNotificationDialog shown      │
                                │    - Release notes, size, direct DL    │
                                └───────────────────┬────────────────────┘
                                                    │ (Tap "Update Now")
                                                    ▼
                                ┌────────────────────────────────────────┐
                                │ 5. InAppUpdateDownloader               │
                                │    - Foreground streaming download     │
                                │    - Progress bar & speed calculation  │
                                └───────────────────┬────────────────────┘
                                                    │
                                                    ▼
                                ┌────────────────────────────────────────┐
                                │ 6. Signature & Package Pre-flight Check│
                                │    - Check certificate hashes vs OS    │
                                └─────────┬────────────────────┬─────────┘
                    (Valid match) │                            │ (Mismatch)
                                  ▼                            ▼
                    ┌───────────────────────────┐ ┌──────────────────────┐
                    │ 7. FileProvider & Intent  │ │ "Signature Conflict" │
                    │    Launch Android package │ │ screen + Save to     │
                    │    installer to overwrite │ │ Downloads (clean)    │
                    └───────────────────────────┘ └──────────────────────┘
```

- **Deterministic Keystore Unification**: Unified Base64 keystore ensures every GitHub Actions build shares the identical signature, preventing "Package conflicts with existing package" errors.
- **In-App Streaming Downloader**: Foreground streaming with live speed (MB/s) and progress percentage.
- **Pre-flight Cryptographic Verification**: Validates package name and certificate fingerprints prior to handoff.
- **FileProvider OS Installer**: Triggers Android's native package manager to perform seamless in-place upgrades.

### 6. 💾 Offline Caching (Room SQLite)
- **Holodeck Archive**: Saves generated maps, level stats, cleared times, and star ratings locally.
- **Movement Session Logs**: Full audit trail of your physical steps, elevation climbed, and total distances.

### 7. 🎨 Uplifting & Stress-Free Aesthetic
- Bright, welcoming, vibrant cyber mint & emerald design (`#00C882`, `#38EFAB`, `#F0FAF5`) preventing claustrophobic eye strain.
- Fully responsive across 16:9, 19.5:9, 21:9 phones, foldables, and tablets with adaptive navigation rails.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.2
- **UI Toolkit**: Jetpack Compose (Material 3)
- **Architecture**: MVVM + Clean Repository Pattern
- **Local Persistence**: Android Room Database (KSP Symbol Processing)
- **Sensors**: Hardware Barometer (`TYPE_PRESSURE`), Linear Accelerometer, Step Detector, Compass Vector
- **Networking**: OkHttp 4 + Kotlin Coroutines & Flow
- **Testing**: Robolectric + JUnit 4 Local JVM Test Suite
- **CI/CD**: GitHub Actions (`package-apk.yml` & `android-ci.yml`)

---

## 🚀 Building & Running Locally

### Prerequisites
- Android Studio Ladybug / Meerkat or newer
- JDK 17+
- Android SDK 36 (Minimum SDK 24)

### Clone & Build
```bash
git clone https://github.com/PUSHPAK-JAISWAL/cybermaze-3d.git
cd cybermaze-3d
```

### Run Tests
```bash
gradle :app:testDebugUnitTest
```

### Build APK
```bash
gradle :app:assembleDebug
```
The compiled APK will be located in `app/build/outputs/apk/debug/app-debug.apk`.

---

## 🤝 Contributing

We welcome community contributions! Please review:
- [Contributing Guidelines](CONTRIBUTING.md)
- [Code of Conduct](CODE_OF_CONDUCT.md)
- [Security Policy](SECURITY.md)
- [Accessibility Statement](ACCESSIBILITY.md)

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

© 2026 **Pushpak M. Jaiswal** ([@PUSHPAK-JAISWAL](https://github.com/PUSHPAK-JAISWAL))
