# nomotion

Client-only Fabric mod for Minecraft 1.21.5+ that implements frame accumulation motion blur ("Option A") without Satin API.

## Features

- Post-process blur each frame via `GameRenderer.render` mixin.
- Triple framebuffer pipeline:
	- `historyA` (previous frame)
	- `historyB` (current captured frame)
	- `output` (blur result)
- Resize-safe framebuffer recreation.
- JSON config at `config/nomotion.json`.
- Mod Menu integration with a config UI.

## Build

```bash
./gradlew build
```

## Config

`config/nomotion.json` contains:

- `enabled`
- `strength` (`0..1`)
- `useDeltaTimeScaling`
- `ignoreGuiScreens`
- `historyResetOnWorldChange`