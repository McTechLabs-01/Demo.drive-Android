# DEMO.DRIVE 1.0 — Codex Edition

Custom native Android project: Java, Android `GLSurfaceView`, and OpenGL ES 2.0. No Unity, Godot, web runtime, game engine, or remote service is used. The game is landscape-only and targets Android 6.0+.

## Build

Requirements: JDK 17 or newer, Android SDK Platform 35, Android Build Tools 35.x, and Gradle 8.13. The project pins Android Gradle Plugin 8.13.2. Set `ANDROID_HOME` or `ANDROID_SDK_ROOT` to the SDK directory, accept the Android SDK licenses, then run:

```powershell
gradle wrapper --gradle-version 8.13
.\gradlew.bat --offline assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. To install on a connected device with USB debugging enabled, run `adb install -r app/build/outputs/apk/debug/app-debug.apk`.

## Current playable slice

- Native landscape launch, custom OpenGL ES scene, procedural proving-ground environment, and D-01 blue coupe.
- Four independently spring/damper-simulated wheel contacts, rear-wheel drive force, steering and lateral tire forces limited by normal load, drag, braking, reverse, and simple perimeter collision response.
- Third-person chase and first-person views, touch steering/throttle/brake/reverse/handbrake, lights, signal indicator, pause, speed/RPM/condition HUD.
- Main menu routes to free drive, garage paint selector, map, challenges, settings/quality selector, and exit.

## Known limits

This is an early playable prototype, not a finished realistic simulator. The procedural scene uses simple shaded box meshes; it has no authored detailed car mesh, textured materials, true wheel geometry, shadows/reflections, audio, traffic, full collision geometry, panel deformation, challenge timing/checkpoints, city map, persistent save/settings, controller/keyboard input, or Android device performance tuning. Graphics quality currently changes the displayed preset selection only. The terrain/suspension and tire model are intentionally lightweight approximations; no soft-body damage is implemented. The APK has not been device-tested.

## Source layout

- `app/src/main/java/com/outfitstudios/demodrive/`: native Android shell, HUD, renderer, and vehicle model.
- `app/src/main/AndroidManifest.xml`: Android app identity and landscape launcher activity.
- `app/build.gradle`, root Gradle files: reproducible build configuration.
