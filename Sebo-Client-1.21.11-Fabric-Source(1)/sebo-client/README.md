# Sebo Client 0.1.0 — Minecraft Java 1.21.11 / Fabric

This is an original source project, not a compiled or game-tested mod. Java 21 is available in the authoring environment, but Gradle and network dependency downloads are not, so the Minecraft integration has not been compiled. Use in single-player or on servers that explicitly permit these features.

## Build on Windows
1. Install JDK 21 and Gradle 9.2.1 from https://gradle.org/releases/ (add Gradle's bin folder to PATH). The project does not include a Gradle wrapper.
2. Extract the whole ZIP, then double-click `build-windows.bat`, or open PowerShell in the extracted folder and run `gradle build --no-daemon`.
3. If the build succeeds, copy `build/libs/sebo-client-0.1.0.jar` to `.minecraft/mods`. Install Fabric Loader for 1.21.11 and matching Fabric API too. Start the Fabric profile.
4. Press Right Shift to open the ClickGUI. Escape or Right Shift closes it.

Alternative: a GitHub Actions workflow is included. Upload the extracted project (including `.github/workflows/build.yml`) to your own repository, run the Build Fabric mod workflow, and download the artifact if the build succeeds. This is optional and has not been executed here.

## Menu
- Modules: toggle Block ESP, Amethyst Scanner, Base Scanner, Chest ESP, Spawner ESP, Ore ESP, Portal Scanner, Tracers, Coordinates HUD and FPS HUD.
- Blocks: search any registered block ID (including modded blocks), click Apply search, then select multiple entries. Color cycles through six colors for each block. Enable Block ESP on the Modules tab.
- Scanner: Live, Seed and Hybrid; radius 1–4 chunks (maximum 9x9 = 81); numeric seed field; capture reference; rescan.
- Settings: scan budget and distance labels. Settings are stored in `config/sebo-client/settings.json`.

## Live / Seed / Hybrid
Live scans only chunks currently loaded in the client. It uses section palette filtering and small main-thread scan batches rather than reading mutable Minecraft world objects from background threads. Chunk positions are centered on the player's current chunk. Enable at least 4 chunks of render distance; actual loaded area may be smaller. Scans refresh over time, so detections can briefly be stale. Each chunk stores up to 256 matching blocks and renders up to 300 nearest matches total. Common blocks may exceed the cap. The ESP is a projected screen-space outline, not a full 3D wireframe; near-camera targets can be skipped. Tracers and labels have additional render caps.

Seed mode in this version is reference-based, NOT automatic seed-to-block generation. A seed by itself will not populate results. In a fresh single-player world created with the same seed, version, world type and generation settings as your target world, travel to the target coordinates. Enable all the scanner modules/blocks you want to capture, wait for chunks to load, open Scanner and click Capture same-seed reference. The client reads the integrated server's seed and saves the scanned reference chunks. Wait until the jobs counter reaches zero; avoid moving or changing targets while capture is in progress. Repeat for additional areas or blocks. Return to your target world, enter the matching numeric seed and choose Seed or Hybrid. Gold outlines indicate reference positions; live detections replace reference detections at the same position in Hybrid. References can be incomplete, capped or differ from modified terrain, older generated chunks, different world settings or datapacks. Reference storage is keyed by seed and dimension; separate configurations with identical seed and dimension are not distinguished. Delete the matching JSON under `config/sebo-client/references/` to clear a bad reference.

Base Scanner is a heuristic for blocks often found in constructed areas. It cannot identify owners or distinguish all natural structures from player structures. It does not find unloaded live data. The Amethyst Scanner highlights amethyst-related blocks, not a calculated geode center.

## Extending the client
`Modules.java` registers scanner targets, colors and module categories; `Config.java` manages persisted settings; `Scanner.java` does incremental scans; `ReferenceStore.java` stores seed reference captures; `Overlay.java` renders projected outlines and HUD; `ClickGui.java` renders settings. Add new block scanner modules via `Modules.add(...)`. Non-scanner modules need their own event or rendering logic. Fullbright, zoom, entity ESP, dragging panels and automatic seed generation are not implemented in this first version.

## Validation checklist after build
- Run the Fabric client, create a test world, press Right Shift and toggle modules.
- Place a selected block and verify it is detected after scan refresh; deselect it and confirm it disappears.
- Walk across chunk boundaries and switch dimensions; confirm results are refreshed and old hits are removed.
- Capture a reference in a fresh world, change a block, switch to Hybrid and verify gold vs live outline colors.
- Restart to verify saved settings. If the build or startup fails, inspect the Gradle output or `.minecraft/logs/latest.log`.
