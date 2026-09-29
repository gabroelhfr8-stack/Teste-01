# Selarium Build Notes

## Build Stack

- Minecraft: 1.20.1
- Forge: 47.4.20
- ForgeGradle: 6.0.53
- Gradle wrapper: 8.8
- Java toolchain: 17
- Mod id: `selarium`

## Expected Commands

Use these commands from the project root:

```powershell
.\gradlew.bat --version
.\gradlew.bat help --no-daemon
.\gradlew.bat --refresh-dependencies help --no-daemon
.\gradlew.bat clean build --no-daemon
.\gradlew.bat runServer --no-daemon
```

The Alpha jar should be generated under:

```text
build/libs/
```

## Gradle Configuration

`settings.gradle` uses the standard ForgeGradle plugin resolution flow:

- `gradlePluginPortal()`
- Forge Maven: `https://maven.minecraftforge.net/`
- `mavenCentral()`

It does not use a manual `resolutionStrategy` for ForgeGradle. The previous manual mapping to `net.minecraftforge.gradle:ForgeGradle` was removed because it bypassed the normal Gradle plugin marker resolution.

Do not use `RepositoriesMode.FAIL_ON_PROJECT_REPOS` in this project. ForgeGradle adds internal temporary repositories during userdev setup, such as bundled repositories used to resolve patched Minecraft artifacts. A strict settings-only repository mode blocks those repositories and causes the plugin to fail while applying. Leaving `repositoriesMode` unset keeps the normal project/plugin repository behavior that ForgeGradle expects; `RepositoriesMode.PREFER_PROJECT` is also compatible if the mode ever needs to be explicit.

`build.gradle` keeps a fixed ForgeGradle version:

```gradle
id 'net.minecraftforge.gradle' version '6.0.53'
```

The fixed version is intentional for Alpha reproducibility. The `[6.0,6.2)` range was tested during troubleshooting, but it failed at the same plugin-resolution boundary in the sandbox environment and was not kept.

## Wrapper Notes

The Gradle wrapper should stay clean and generated. Do not commit local environment hacks into `gradlew.bat`.

If plugin resolution fails because a local shell is using the wrong Gradle cache, set `GRADLE_USER_HOME` manually in that terminal session instead of editing the wrapper:

```cmd
set GRADLE_USER_HOME=%USERPROFILE%\.gradle
.\gradlew.bat clean build --no-daemon
```

## Local Cleanup

Ignored local build/cache folders:

- `.gradle/`
- `.gradle-sandbox/`
- `build/`
- `run/`
- `out/`

The `.gradle-sandbox/` folder was created only during sandbox troubleshooting and should not be used for normal builds. It can be deleted safely from the project root when the environment allows deletion.

Do not delete the global Gradle cache under the user's home unless intentionally repairing a local Gradle installation.

## Known Resolution Issues

Two different failure modes were observed:

1. Plugin resolution failure:

```text
Plugin [id: 'net.minecraftforge.gradle', version: '6.0.53'] was not found
```

This happened when Gradle used an empty sandbox `user.home` cache and could not reach the Forge Maven/plugin repositories.

2. Native services failure:

```text
Could not initialize native services.
Failed to load native library 'native-platform.dll' for Windows 11 amd64.
```

This happened only inside the Codex/sandbox environment after trying to force Gradle to use the real `%USERPROFILE%\.gradle` cache. It indicates an environment permission issue, not a Selarium source-code issue.

In a normal local user shell, `USERPROFILE`, Java `user.home`, and Gradle cache permissions should all point to the same user context.

For Alpha validation, run the build outside the Codex sandbox in a normal terminal:

```powershell
cd "C:\Users\guilh\OneDrive\Documentos\Selarium"
.\gradlew.bat clean build --no-daemon
.\gradlew.bat runServer --no-daemon
```
