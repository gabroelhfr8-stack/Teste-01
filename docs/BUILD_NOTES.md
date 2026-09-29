# Selarium Build Notes

## Build Stack

- Minecraft: 1.20.1 (official Mojang mappings)
- Forge: 47.4.20
- ForgeGradle: 6.0.53
- Gradle wrapper: 8.8
- Java toolchain: 17
- Mod id: `selarium`

## Everyday Commands

Run these from the project root (`gradlew.bat` instead of `./gradlew` on Windows):

```bash
./gradlew build                 # compile, run unit checks, produce build/libs/selarium-<version>.jar
./gradlew runClient             # dev client
./gradlew runServer             # dev dedicated server (accept the EULA in run/eula.txt first)
./gradlew runGameTestServer     # headless: runs every @GameTest and exits (no EULA needed)
./gradlew runClient -Psmoketest # scripted client session that takes screenshots (needs an OpenGL context)
```

Optional Gradle properties `-PselariumBuildDir=<dir>` and `-PselariumRunDir=<dir>` move the build and run
directories, which helps on synced folders such as OneDrive.

## Continuous Integration

`.github/workflows/build.yml` runs three jobs on every push:

| Job | What it catches |
|---|---|
| **Validate assets** | `tools/validate_assets.py`: broken JSON, missing textures or parents, models that reference nothing, missing or mismatched translations, registry names without assets, loot tables and recipes pointing at unknown items, unused textures. |
| **Compile and package** | `./gradlew build`, then the GameTests on a real Forge server (registries, wards, recipes, worldgen, loot tables, save/load), then `tools/check_server_log.py`, which fails on real ERROR lines (datapack parse errors, exceptions). |
| **Client smoke test** (required) | Boots the real game under Xvfb with Mesa software OpenGL, creates a flat world, builds a showcase and takes screenshots (`ClientSmokeTest`). A broken model, particle, shader or renderer shows up as a crash or a log error. Screenshots are attached to the run (`client-smoke` artifact) and printed into the log by `tools/dump_smoke.py`. |

The smoke test only proves that the client renders without errors. It is software rendering at 854x480, so judge
looks and performance on a real GPU.

## Tooling

```bash
python3 tools/validate_assets.py [--warnings]   # what the assets job runs
python3 tools/art/build_all.py                  # regenerate every texture, model, blockstate and mcmeta
python3 tools/gen_docs.py                       # regenerate docs/WARDS.md from the code and pt_br.json
```

The art pipeline needs Python 3 with `numpy` and `pillow`. It is deterministic: running it twice gives
byte-identical files, so `git diff` after a run shows exactly what a generator change did. Never edit generated
textures by hand; change the generator in `tools/art/` instead (see `docs/ARCHITECTURE.md`).

`tools/preview/` renders the JSON models (block and item) to a PNG contact sheet with headless Chromium and
three.js, which is the quickest way to review a model change without launching the game:

```bash
cd tools/preview && npm install     # or set NODE_PATH to a global playwright install
node render.cjs --out sheet.png --cell 320 --cols 4 --view gui block/arcane_grinder item/selarium_codex
```

## Gradle Configuration

`settings.gradle` uses the standard ForgeGradle plugin resolution flow: `gradlePluginPortal()`, Forge Maven
(`https://maven.minecraftforge.net/`) and `mavenCentral()`. It does not use a manual `resolutionStrategy` for
ForgeGradle, and it must not use `RepositoriesMode.FAIL_ON_PROJECT_REPOS`: ForgeGradle adds temporary repositories
during userdev setup and a strict mode blocks them.

`build.gradle` pins ForgeGradle to `6.0.53` on purpose, for reproducible builds.

Keep the Gradle wrapper clean and generated; do not commit local environment hacks into it. If plugin resolution
fails because a shell uses the wrong Gradle cache, set `GRADLE_USER_HOME` for that session instead of editing the wrapper.

Ignored local folders: `.gradle/`, `.gradle-sandbox/`, `build/`, `run/`, `out/`.

## Troubleshooting

- `Plugin [id: 'net.minecraftforge.gradle', version: '6.0.53'] was not found`: Gradle could not reach the Forge
  Maven or used an empty cache. Check the network and `GRADLE_USER_HOME`.
- `Could not initialize native services` on Windows: the shell's `USERPROFILE`, Java `user.home` and the Gradle
  cache permissions point to different users. Run from a normal user terminal.
- Client smoke test fails with a GLFW or OpenGL error: the machine has no usable OpenGL 3.2+ context. Run it under
  `xvfb-run` with `LIBGL_ALWAYS_SOFTWARE=1`, as the CI job does.
