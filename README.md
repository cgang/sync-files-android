# File Hub Android Client

[![License](https://img.shields.io/badge/license-Apache--2.0-blue)](LICENSE)

> ⚠️ **WORK IN PROGRESS** - This project is currently under active development and not yet ready for production use.

Android client implementation for File Hub personal file synchronization service. This repository contains comprehensive implementation guides for Android clients that connect to the File Hub server.

## 📌 Project Overview

File Hub is a personal file backup and synchronization service with WebDAV support and an optimized mobile sync protocol. This client repository provides:

- **Android Client**: Kotlin-based implementation with Jetpack Compose UI, MVVM architecture, and WorkManager for background sync
- **Comprehensive Documentation**: Detailed implementation guides, API references, and protocol specifications

## 🔍 Key Features

### Sync Protocol
- **Version-based sync**: Monotonically increasing version numbers for accurate change detection
- **Change log**: Tracks all operations (create, modify, delete, move, copy)
- **Version vectors**: Detects conflicts from multiple users
- **Chunked uploads**: Resumable uploads for large files (>10MB)
- **Conditional downloads**: ETag-based caching to avoid redundant transfers

### Authentication Methods
- **Session-based** (recommended for mobile): Cookie-based authentication, 24-hour expiration
- **HTTP Basic**: Simple username/password with Base64 encoding
- **HTTP Digest**: Challenge-response authentication that never transmits passwords

### Storage and Persistence
- **File storage**: App-specific directories for downloaded files
- **Secure credentials**: Android Keystore for credential storage

### Performance Optimizations
- **Pagination**: Directory listings support offset/limit pagination
- **Bandwidth efficient**: Transfers only changed data via sync protocol
- **Background sync**: Periodic automatic synchronization
- **Network monitoring**: Adaptive behavior based on network conditions

## 🏗️ Architecture

### Architecture (MVVM + Clean Architecture)
```
┌─────────────────────────────────────────────────────┐
│                   Presentation Layer                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │   Views      │  │  ViewModels  │  │   Use Cases  │ │
│  │  (Compose)   │  │              │  │              │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────┘
                          │
┌────────────────────────▼─────────────────────────────┐
│                    Domain Layer                      │
│  ┌──────────────┐  ┌───────────────────────────────┐ │
│  │    Models    │  │         Repository Interface   │ │
│  │  (Entities)  │  │                               │ │
│  └──────────────┘  └───────────────────────────────┘ │
└─────────────────────────────────────────────────────┘
                          │
┌────────────────────────▼─────────────────────────────┐
│                      Data Layer                       │
│  ┌──────────────┐  ┌──────────────┐ │
│  │   Remote     │  │   Cache      │ │
│  │  (API)       │  │  (DataStore) │ │
│  └──────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────┘
```

## 🛠️ Technology Stack

### Android
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM + Clean Architecture
- **Dependency Injection**: Hilt
- **Networking**: OkHttp
- **Concurrency**: Coroutines + Flow
- **Background Tasks**: WorkManager
- **Secure Storage**: Android Keystore

## 📖 Getting Started

### Requirements

- Android Studio Hedgehog (2023.1.1) or later
- Min SDK: Android 7.0 (API 24)
- Target SDK: Android 14 (API 34)

## 🔗 Related Projects

- **[File Hub Server](https://github.com/cgang/file-hub)** - The server implementation
  - Go backend
  - WebDAV and Sync API support
  - Embedded Svelte web UI

## 🤝 Contributing

Interested in contributing? Please follow these guidelines:

1. Follow the established coding patterns in your platform
2. Ensure documentation is updated with any API or protocol changes
3. Test thoroughly on actual devices, not just emulators
4. Adhere to Material Design 3 guidelines

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🤖 AI Assistant

This project has received assistance from Chinese AI models (Qwen, DeepSeek, GLM) during development. The AI assistants have helped with code generation, documentation, refactoring, and bug fixes as part of the development workflow.
