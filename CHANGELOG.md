# Changelog

## Version semantics
- fix: 1.0.0 → 1.0.1 — Bug fixes.
- feat: 1.0.0 → 1.1.0 — Backward-compatible new features.
- feat!: 1.0.0 → 2.0.0 — Breaking changes.

Examples are illustrative; use actual project versions.

## [1.2.0]
### Added
- Signed, optimized release build (R8) and tag-triggered release workflow publishing a GitHub Release with release and debug APKs.
- Launcher icon (adaptive + monochrome) and splash screen with loading indicator.

## [1.1.1] — Unreleased (proposed, not yet verified by CI)
### Fixed
- Cropped images showed the wrong region (oversized image was centered instead of top-aligned), so crops looked misplaced/overlapping → image is now top-aligned and clipped correctly.
- Crop handles could be dragged on top of each other → kept at least 48dp apart.
### Changed
- Debug builds signed with a fixed keystore; first install of this version still needs one uninstall of earlier builds.

## [1.1.0] — Unreleased (proposed, not yet verified by CI)
### Added
- Light / Dark / System theme choice (saved on device).
### Changed
- Package and applicationId com.stitch.app → com.stitchpix.app (installs as a separate app from earlier builds).
- Grid tiles: rounded corners, wider borders, wider spacing; thumbnails decoded at small size for smoother scrolling.
- Editors: preview zoomed out; horizontal editor centered.

## [1.0.2] — Unreleased (proposed, not yet verified by CI)
### Fixed
- JVM signature clash in MainViewModel (`tab` property setter vs `setTab()`) → removed `setTab()`; tabs set the property directly.

## [1.0.1] — Unreleased (proposed, not yet verified by CI)
### Fixed
- Compile error in MediaRepository (Media class used as a value) → uses an import alias.
- Compile error in SelectionScreen (AnimatedVisibility called in ColumnScope) → moved to its own composable.
### Changed
- App renamed Stitch → Stitchpix.

## [1.0.0] — Unreleased (proposed, not built or tested)
### Added
- Gallery selection screen, vertical/horizontal stitch editors, per-image crop, save dialog (JPG/PNG/PDF), GitHub Actions debug build.
