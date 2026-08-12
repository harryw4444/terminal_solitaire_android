# 🃏 Terminal Solitaire (Android)

A Solitaire Android Game heavily inspired by [Brian Strauch's Solitaire-TUI](https://github.com/brianstrauch/solitaire-tui) —
Klondike solitaire rendered in a terminal/ASCII style, built with Kotlin and a custom `Canvas`-drawn `GameView`.

## ♦️ Features

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

## ♥️ Releases

Prebuilt APKs are published under [Releases](../../releases) — grab the latest one there rather
than building from source if you just want to install the game.

## ♣️ Credit

This project is heavily inspired by [brianstrauch/solitaire-tui](https://github.com/brianstrauch/solitaire-tui) (licensed under Apache License 2.0), reimagined as a native Android app. All credit for the original game design and terminal aesthetic goes to Brian Strauch.

## 📱 Screenshots
<img width="25%" alt="Screenshot_20260812-225915" src="https://github.com/user-attachments/assets/de2fe042-666b-46b5-bc48-3bffc87a4db4" />
<img width="25%" alt="Screenshot_20260812-222150" src="https://github.com/user-attachments/assets/30aa9e3d-6066-4425-a6db-6f598393c8d5" />





