# Accessibility Statement for CyberMaze 3D

**CyberMaze 3D** is committed to providing an enjoyable, uplifting, and accessible mobile gaming experience for everyone, regardless of physical or cognitive abilities.

## Accessibility Principles & Features

1. **Touch Target Sizing**:
   - All interactive controls, virtual directional buttons, and quick actions strictly maintain a minimum touch target size of **48dp x 48dp** complying with Android Accessibility Standards (WCAG 2.1 AA).

2. **Sensory Comfort & Stress-Free Play**:
   - Bright, vibrant, cheerful color scheme designed to prevent eye strain and claustrophobic gameplay.
   - High contrast ratios ($\ge 4.5:1$) between background surfaces and informative text.
   - Haptic vibration feedback accompanies important in-game events (core pickup, ambushes) with a 1-tap disable toggle in Settings.

3. **Motion & Sensor Flexibility**:
   - Real-world physical movement (walking, elevation gain) can be recorded via device sensors.
   - For users unable to walk or climb stairs, or when stationary indoors, the **Motion Lab Simulator & Calibration Dock** allows direct virtual simulation (+45 steps, +4.2m climb, -2.5m drop) so 100% of the game's procedural synthesis remains fully accessible to all players.

4. **Screen Reader Semantics**:
   - Meaningful `contentDescription` on all navigational and gameplay icons.
   - Distinctive typography using readable monospace headers and scalable SP units supporting system font scaling.

## Supported Environments
- Android 7.0 (Nougat, API 24) through Android 15+
- Mobile phones (16:9, 18:9, 19.5:9, 21:9), foldables, and tablets with adaptive navigation rails.

## Known Limitations & Roadmap
- The 3D isometric canvas viewport currently uses visual indicators; we are actively exploring screen-reader spoken grid coordinates for turn-by-turn navigation.

## Reporting Barriers & Feedback
If you encounter accessibility barriers or have suggestions for improvements, please reach out to us:
- **Maintainer**: Pushpak M. Jaiswal
- **Email**: [pushpakmjaiswal@gmail.com](mailto:pushpakmjaiswal@gmail.com)
- **GitHub Issues**: [github.com/PUSHPAK-JAISWAL/cybermaze-3d/issues](https://github.com/PUSHPAK-JAISWAL/cybermaze-3d/issues)
