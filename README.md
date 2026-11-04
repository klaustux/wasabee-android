# Wasabee Android

Native Android client for [Wasabee](https://wasabee.rocks), the operation planning tool for Ingress agents.

Early stage: you can log in and see your operations.

## Features

- Server selection: America (`am`), Europe (`eu`), Asia/Pacific (`ap`)
- Login with Google (OAuth access token sent to `/aptok`)
- Login with a one-time token copied from wasabee.rocks (`/oneTimeToken`)
- Persistent session (JWT stored in DataStore), automatic logout on `401`
- Operations list from `GET /api/v1/me` with pull-to-refresh

## Tech stack

- Kotlin, Jetpack Compose, Material 3 (dynamic colors on Android 12+)
- Retrofit + kotlinx.serialization, OkHttp
- AndroidX DataStore, Lifecycle ViewModel
- Google Play Services Auth (Identity `AuthorizationClient`)
- minSdk 26, targetSdk 37, Java 17

## Project structure

```
app/src/main/java/rocks/wasabee/android/
├── MainActivity.kt        # theme, screen switching, Google consent flow
├── WasabeeApp.kt          # application, repository wiring
├── data/
│   ├── WasabeeApi.kt      # Retrofit endpoints
│   ├── WasabeeRepository.kt
│   ├── WasabeeServer.kt   # server enum
│   ├── SessionStore.kt    # DataStore session persistence
│   ├── GoogleAuth.kt      # Google access token
│   └── Models.kt          # Me, MeTeam, MeOp
└── ui/
    ├── MainViewModel.kt   # Loading / LoggedOut / LoggedIn state
    ├── LoginScreen.kt
    └── OperationsScreen.kt
```

## Build

```bash
./gradlew assembleDebug
./gradlew installDebug   # install on a connected device/emulator
```

## Google login setup

The server only calls Google's userinfo endpoint with the access token, so any OAuth client
registered in Google Cloud Console for package `rocks.wasabee.android` and your signing
certificate SHA-1 works:

```bash
./gradlew signingReport
```

Without it, use the one-time token login.
