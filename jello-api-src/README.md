# Jello API

A Fabric 1.21.5+ client-side shader/post-processing library.

## What it does

Jello API handles the lifecycle boilerplate for Minecraft post-processing shaders:
- **Lazy initialization** – shaders are created on first use, not at startup
- **Auto reload** – shaders reinitialize on resource reload (F3+T) and world renderer reload
- **Resolution awareness** – shader targets resize when the window resizes
- **Uniform helpers** – push float/int uniforms each frame without boilerplate

## Quick Start

```java
public class MyMod implements ClientModInitializer {

    // Declare once – will reload automatically
    private static final ManagedShaderEffect BLUR =
        ShaderEffectManager.getInstance()
            .manage(Identifier.of("mymod", "shaders/post/blur.json"));

    @Override
    public void onInitializeClient() {
        // Render at the correct time (after entity outlines, before HUD)
        ShaderEffectRenderCallback.EVENT.register(tickDelta -> {
            if (isEnabled) {
                BLUR.setUniformValue("Radius", 2.0f);
                BLUR.render(tickDelta);
            }
        });

        // React to the world being fully rendered (for custom overlays)
        PostWorldRenderCallback.EVENT.register((matrices, camera, tickDelta) -> {
            // draw custom elements here
        });

        // React to window resize
        ResolutionChangeCallback.EVENT.register((width, height) -> {
            // recreate resolution-dependent resources
        });
    }
}
```

## Shader JSON

Place your post-effect JSON at `assets/<namespace>/shaders/post/<name>.json`.  
It follows the standard Minecraft `PostEffectPipeline` format.

Example – `assets/mymod/shaders/post/blur.json`:
```json
{
    "targets": {
        "swap": {}
    },
    "passes": [
        {
            "name": "blur",
            "intarget": "minecraft:main",
            "outtarget": "swap",
            "uniforms": [
                { "name": "BlurDir", "values": [1.0, 0.0] },
                { "name": "Radius",  "values": [5.0] }
            ]
        },
        {
            "name": "blur",
            "intarget": "swap",
            "outtarget": "minecraft:main",
            "uniforms": [
                { "name": "BlurDir", "values": [0.0, 1.0] },
                { "name": "Radius",  "values": [5.0] }
            ]
        }
    ]
}
```

## API Surface

| Class / Interface | Description |
|---|---|
| `ShaderEffectManager` | Factory for `ManagedShaderEffect` instances |
| `ManagedShaderEffect` | Handle to a lazily-loaded, auto-reloading `PostEffectProcessor` |
| `ShaderEffectRenderCallback` | Event fired at the correct render moment for post effects |
| `PostWorldRenderCallback` | Event fired after world render, before HUD |
| `ResolutionChangeCallback` | Event fired when the window is resized |
| `GlHelper` | Utility helpers (texture access, resolution queries) |

## Using as a dependency

After running `gradle publishToMavenLocal` in this project, add to your mod's `build.gradle`:

```groovy
repositories {
    mavenLocal()
}
dependencies {
    modImplementation "com.jellomakker:jello-api:1.0.0"
    include "com.jellomakker:jello-api:1.0.0"  // jar-in-jar
}
```
