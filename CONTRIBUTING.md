# Contributing to CyberMaze 3D

Thank you for your interest in contributing to **CyberMaze 3D**! We welcome bug reports, feature suggestions, documentation enhancements, and pull requests.

## How to Contribute

### 1. Reporting Bugs
- Check existing issues before opening a new one.
- Use the **Bug Report** template.
- Provide your device model, Android OS version, and reproducible steps or logs.

### 2. Suggesting Features
- Open a **Feature Request** issue to propose gameplay enhancements, sensor mechanics, or AI integrations.
- Explain the motivation and expected user experience.

### 3. Submitting Pull Requests
1. Fork the repository on GitHub.
2. Clone your fork:
   ```bash
   git clone https://github.com/YOUR_USERNAME/cybermaze-3d.git
   cd cybermaze-3d
   ```
3. Create a feature branch:
   ```bash
   git checkout -b feature/your-feature-name
   ```
4. Follow Kotlin and Jetpack Compose best practices:
   - Ensure clean architecture and proper state management (`StateFlow`).
   - Keep interactive touch targets $\ge 48$dp.
   - Run local unit and Robolectric tests:
     ```bash
     gradle :app:testDebugUnitTest
     ```
5. Commit with descriptive messages:
   ```bash
   git commit -m "feat(sensor): enhance elevation filtering for barometer"
   ```
6. Push to your fork and submit a Pull Request to `main`.
7. Ensure CI checks pass on `.github/workflows/android-ci.yml`.

## Maintainer Contact

- **Author & Maintainer**: Pushpak M. Jaiswal
- **GitHub**: [@PUSHPAK-JAISWAL](https://github.com/PUSHPAK-JAISWAL)
- **Email**: [pushpakmjaiswal@gmail.com](mailto:pushpakmjaiswal@gmail.com)
