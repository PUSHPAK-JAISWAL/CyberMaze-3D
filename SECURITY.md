# Security Policy

## Supported Versions

We actively provide security patches and updates for the following versions:

| Version | Supported          |
| ------- | ------------------ |
| 1.x.x   | :white_check_mark: |
| < 1.0   | :x:                |

## In-App Update Security & Deterministic Signatures

CyberMaze 3D implements a pre-flight cryptographic verification protocol:
1. **Package Verification**: All in-app updates verify that the package identifier strictly matches `com.aistudio.cybermaze.kxpztr`.
2. **Cryptographic Certificate Check**: The app inspects APK signatures via Android's `SigningInfo` / `GET_SIGNING_CERTIFICATES` API prior to installation handoff.
3. **Deterministic Keystore**: Official builds released through GitHub Actions use a unified cryptographic keystore to protect against tampering and package conflict errors.

## Reporting a Vulnerability

If you discover a security vulnerability in CyberMaze 3D, please report it privately:

- **Maintainer**: Pushpak M. Jaiswal
- **Email**: [pushpakmjaiswal@gmail.com](mailto:pushpakmjaiswal@gmail.com)
- **GitHub**: [@PUSHPAK-JAISWAL](https://github.com/PUSHPAK-JAISWAL)

Please include:
- A description of the issue and potential impact
- Steps to reproduce or proof of concept
- Affected versions or devices

We will acknowledge receipt within 48 hours and coordinate a fix prior to public disclosure.
