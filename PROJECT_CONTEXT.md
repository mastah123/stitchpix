# Project Context — Stitchpix

**Status:** v1.2.0 (signed release build, launcher icon, splash). Not yet verified by CI at this version. Not run on a device by the author.

## Architecture (package com.stitchpix.app)
- `MainActivity` – permissions, routing (selection vs editor). `MainViewModel` – all UI state (tab, sort, selection list, crops, save).
- `data/` – models, `MediaRepository` (MediaStore query; screenshots = path contains "screenshot").
- `stitch/Stitcher` – plan (dimensions) + render (decode, EXIF rotate, crop, scale, draw). `stitch/Exporter` – JPG/PNG/PDF writing via MediaStore.
- `ui/` – SplashScreen (matches the system splash: blue background, icon, spinner), SelectionScreen, EditorScreen, CropImage (crop handles), SaveDialog, AppTheme.

## Decisions
- Release builds: R8 + resource shrinking, signed with a private key from GitHub Secrets (workflow `release.yml`, triggered by `v*` tags). Single universal APK. R8 build is untested on device; debug APK is attached as fallback.
- Debug APKs signed with committed `app/debug.keystore` for stable updates (not for release).
- Selection order = list order, so renumbering is automatic.
- Images scaled to the smallest cross-axis size (no upscaling); crop is fractions, applied at save.
- Quality slider is JPG-only; PNG and PDF are lossless. PDF is one page.
- No Gradle wrapper committed; CI uses setup-gradle 8.7.

## Known issues / untested
- Everything above is unverified on device. Size estimate is a heuristic.
- Very large stitches are capped at ~60 MP and may still hit memory limits.
- EXIF mirror orientations are ignored.

## Next steps
Verify CI build; manual test (selection renumbering, 2+ bar, crop axes, each save format); add unit tests and a Gradle wrapper.
