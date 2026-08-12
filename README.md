# 🃏 Terminal Solitaire (Android)

A Solitaire Android Game heavily inspired by [Brian Strauch's Solitaire-TUI](https://github.com/brianstrauch/solitaire-tui) —
Klondike solitaire rendered in a terminal/ASCII style, built with Kotlin and a custom `Canvas`-drawn `GameView`.

<img width="20%" alt="Screenshot_20260812-222055_cropped" src="https://github.com/user-attachments/assets/34c0d475-a2f5-4d27-997e-46b1c1a19334" />
<img width="20%" alt="Screenshot_20260812-222150_cropped" src="https://github.com/user-attachments/assets/28c9b05c-72a7-48e0-b349-6c610363b81d" />
<img width="20%" alt="Screenshot_20260812-222047_cropped" src="https://github.com/user-attachments/assets/9ce1de81-648d-4884-8dda-576a8b9c3ae0" />

## ♥️ Features

- Classic Klondike solitaire rules
- Terminal-style ASCII card rendering
- Light and dark modes
- Game state is preserved across app switches, backgrounding, and process death
- Resume-in-progress game or start a new one from the main menu

## ♠️ Building

This is a standard Gradle-based Android project.

```bash
./gradlew assembleDebug
```

The debug APK will be output to `app/build/outputs/apk/debug/`.

Open the project folder in Android Studio to build and run directly on a device or emulator
(minimum SDK 21).

## ♦️ Releases

Prebuilt APKs are published under [Releases](../../releases) — grab the latest one there rather
than building from source if you just want to install the game.

## ♣️ Credit

This project is heavily inspired by [brianstrauch/solitaire-tui](https://github.com/brianstrauch/solitaire-tui) (licensed under Apache License 2.0), reimagined as a native Android app. All credit for the original game design and terminal aesthetic goes to Brian Strauch.
