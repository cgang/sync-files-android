# AI Agents Context - File Hub Mobile Client

## Table of Contents
- [Project Overview](#project-overview)
- [Supported Platforms](#supported-platforms)
- [Architecture Patterns](#architecture-patterns)
- [Technology Stack](#technology-stack)
- [Coding Standards and Conventions](#coding-standards-and-conventions)
- [Development Workflow](#development-workflow)

## Project Overview

This repository contains Android client that connect to the File Hub server - a personal file backup and synchronization service.


## Supported Platforms

### Android
- **Language**: Kotlin
- **Min SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 34)
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM + Clean Architecture

## Architecture Patterns

### Common Patterns (Both Platforms)

#### Clean Architecture Layers
1. **Presentation Layer**: UI components, ViewModels, State management
2. **Domain Layer**: Business logic, Use cases, Domain models
3. **Data Layer**: Repositories, API clients

#### Repository Pattern
- Single source of truth for each data type
- Abstract interface in domain layer
- Concrete implementation in data layer
- Handles remote (API) data sources

#### Flow State Management
- Use `StateFlow` for UI state
- Use `SharedFlow` for one-time events
- Collect with `collectAsState()` in Composables

## Technology Stack

### Android Dependencies
- **Core**: `androidx.core:core-ktx`, `androidx.lifecycle:lifecycle-runtime-ktx`
- **Compose**: `androidx.compose.material3`, `androidx.navigation:navigation-compose`
- **Networking**: `com.squareup.okhttp3:okhttp` with logging interceptor
- **Serialization**: `org.jetbrains.kotlinx:kotlinx-serialization-json`
- **Background**: `androidx.work:work-runtime-ktx`
- **Concurrency**: `org.jetbrains.kotlinx:kotlinx-coroutines-android`

## Coding Standards and Conventions

### General Guidelines

#### 1. Documentation is the Product
- This repository contains documentation, not source code
- Focus on clear, comprehensive guides
- Include complete code examples
- Explain both "what" and "why"

#### 2. Consistency Between Platforms
- Use consistent terminology across both platforms
- Keep architectural concepts aligned
- Where patterns differ, explain the platform-specific reasoning

#### 3. Security-First Approach
- Always demonstrate secure credential storage (Keystore/Keychain)
- Use HTTPS in all examples
- Validate all inputs
- Handle authentication errors gracefully

#### 4. Error Handling
- Never use empty catch blocks in Android Kotlin code
- Use proper error propagation with context
- Display user-friendly error messages

#### 5. Internationalization Considerations
- Use string resources for all user-facing text
- Consider right-to-left (RTL) layouts
- Use formatting APIs for dates, numbers, file sizes
- Avoid hard-coded strings in code

### Android Standards

#### Kotlin Conventions
- Use Kotlin idiomatic code (apply, let, run, also, takeIf)
- Prefer data classes for models
- Use extension functions for utility functions
- Mark functions as `suspend` for async operations

#### Compose Conventions
- Use `@Composable` annotation for all UI components
- Implement state hoisting for reusable components
- Use Material 3 design tokens
- Follow composition local pattern for theme and navigation

#### Architecture Conventions
- Use Hilt for dependency injection
- Follow single-responsibility principle
- Keep ViewModels focused on UI state
- Place business logic in Use Cases


## Development Workflow

### Code Review Focus

When reviewing or generating code:

1. **Security**: Credentials are never stored in plain text
2. **Error handling**: All potential errors are handled gracefully
3. **Performance**: Use efficient algorithms for large file operations
4. **User experience**: Provide progress feedback for long operations
5. **Compatibility**: Code works across supported platform versions

### Common Pitfalls to Avoid

#### Android
- ❌ Using `GlobalScope` for coroutines (use `viewModelScope` or `lifecycleScope`)
- ❌ Forgetting to collect Flows in the appropriate scope
- ❌ Blocking the main thread with file I/O
- ❌ Not handling network timeouts


### Version Compatibility

#### Android API Levels
- **API 24-27**: Use Support Libraries where necessary
- **API 28+**: Full feature support
- **API 30+**: Scoped storage restrictions apply
- **API 33+**: Notification permission required



## Project Goals

### User Experience Goals
- **Fast**: Sync should be quick and efficient
- **Reliable**: Handle network interruptions gracefully
- **Transparent**: Show users what's happening during sync
- **Secure**: Protect user credentials and data

### Technical Goals
- **Efficient**: Minimize bandwidth usage with sync protocol
- **Robust**: Handle edge cases and errors properly
- **Maintainable**: Clear code structure and documentation
- **User-friendly**: Intuitive UI following Material Design 3

This document provides AI agents with comprehensive context for implementing or improving the File Hub Android client.
