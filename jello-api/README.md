# Jello API

A lightweight Fabric client-side library for Minecraft 1.21.5+ that handles the lifecycle boilerplate for post-processing shaders.

- **Lazy initialization** – shaders are created on first use, not at startup
- **Auto reload** – shaders reinitialize on resource reload (F3+T) and world renderer reload
- **Resolution awareness** – shader targets resize automatically when the window resizes
- **Uniform helpers** – push float/int uniforms each frame with no boilerplate

---

## Download

Pre-built JARs are in the [`lib builds/`](../lib%20builds/) folder at the root of this repo:

| File | Minecraft version |
|---|---|
| `jello-api-1.0.0.jar` | 1.21.5+ |

---

## Adding Jello API to your mod

### Option A — Jar-in-jar (recommended, zero extra install for users)

Place `jello-api-1.0.0.jar` somewhere in your project (e.g. `libs/jello-api-1.0.0.jar`).

In your `build.gradle`:

```groovy
repositories {
    // no extra repo needed – we reference the file directly
}

dependencies {
    // compiles against the API
    modImplementation files('libs/jello-api-1.0.0.jar')
    // bundles jello-api inside your mod JAR (players don't need to install separately)
    include files('libs/jello-api-1.0.0.jar')
}
```

In your `fabric.mod.json`, declare the embedded JAR and the dependency:

```json
{
  "depends": {
    "jello-api": "*"
  },
  "jars": [
    { "file": "META-INF/jars/jello-api-1.0.0.jar" }
  ]
}
```

Then copy the JAR into `src/main/resources/META-INF/jars/` so Fabric Loom picks it up.

### Option B — Require users to install separately

```groovy
dependencies {
    modCompileOnly files('libs/jello-api-1.0.0.jar')
}
```

```json
{
  "depends": {
    "jello-api": "*"
  }
}
```

Players must also have `jello-api-1.0.0.jar` in their mods folder.

---

## Quick Start

```java
import com.jellomakker.jello.api.event.ShaderEffectRenderCallback;
import com.jellomakker.jello.api.managed.ManagedShaderEffect;
import com.jellomakker.jello.api.managed.ShaderEffectManager;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Identifier;

public class MyModClient implements ClientModInitializer {

    // Declare once – automatically reloads on F3+T and window resize
    private static final ManagedShaderEffect BLUR =
        ShaderEffectManager.getInstance()
            .manage(Identifier.of("mymod", "shaders/post/blur.json"));

    private static boolean enabled = true;

    @Override
    public void onInitializeClient() {
        // Register the render callback – fires at the correct point each frame
        ShaderEffectRenderCallback.EVENT.register(tickDelta -> {
            if (enabled) {
                // Push uniforms before rendering
                BLUR.setUniformValue("Radius", 4.0f);
                BLUR.render(tickDelta);
            }
        });
    }
}
```

---

## Shader JSON

Place your post-effect JSON at:

```
src/main/resources/assets/<namespace>/shaders/post/<name>.json
```

It uses the standard Minecraft `PostEffectPipeline` format. Example — a simple gaussian blur:

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

The built-in `blur` program (from Minecraft's own assets) accepts:
- `BlurDir` – vec2 blur direction (e.g. `[1,0]` for horizontal, `[0,1]` for vertical)
- `Radius` – float blur radius in pixels

You can also write custom GLSL and reference it with `"name": "mymod:my_shader"`.

---

## API Reference

### `ShaderEffectManager`

Central registry. All managed effects automatically reinitialize on resource reload and resolution change.

```java
// Get the singleton
ShaderEffectManager manager = ShaderEffectManager.getInstance();

// Register a managed shader
ManagedShaderEffect effect = manager.manage(Identifier.of("mymod", "shaders/post/myeffect.json"));

// Permanently remove a shader (frees GL resources)
manager.dispose(effect);
```

### `ManagedShaderEffect`

Handle to a lazily-loaded, auto-reloading shader.

```java
// Render this frame
effect.render(tickDelta);

// Push uniforms before rendering
effect.setUniformValue("Radius", 3.0f);          // float
effect.setUniformValue("Direction", 1.0f, 0.0f); // vec2
effect.setUniformValue("Tint", 1f, 0f, 0f, 1f);  // vec4
effect.setUniformValue("SampleCount", 8);         // int

// Check status
boolean ready   = effect.isInitialized();
boolean crashed = effect.isErrored();

// Force reload (e.g. after changing settings)
effect.release(); // next render() call will reinitialize it
```

### Events

#### `ShaderEffectRenderCallback`

Fires at the correct point in the frame to apply post-processing (after entity outlines, before HUD). **Always render your `ManagedShaderEffect` here.**

```java
ShaderEffectRenderCallback.EVENT.register(tickDelta -> {
    myEffect.render(tickDelta);
});
```

#### `PostWorldRenderCallback`

Fires after the world is rendered (blocks, entities, particles, sky) but before the hand and HUD. Use this for custom world-space overlays — **do not** call `ManagedShaderEffect.render()` here.

```java
PostWorldRenderCallback.EVENT.register((matrices, camera, tickDelta) -> {
    // draw custom world-space elements
});
```

#### `ResolutionChangeCallback`

Fires whenever the window is resized. Managed effects automatically handle this; register here only if you have your own resolution-dependent resources.

```java
ResolutionChangeCallback.EVENT.register((width, height) -> {
    // recreate FBOs or update resolution uniforms
});
```

### `GlHelper`

Utility class for 1.21.5+'s GPU abstraction layer.

```java
// Sample the main scene texture in a custom shader
GpuTexture sceneColor = GlHelper.getMainColorGpuTexture();

// Current framebuffer size
int w = GlHelper.getFramebufferWidth();
int h = GlHelper.getFramebufferHeight();
```

---

## Building from source

Requires JDK 21 and Gradle 9+.

```bash
cd jello-api-src
gradle build
# Output: build/libs/jello-api-1.0.0.jar
```

---

## License

MIT — see [LICENSE](LICENSE).
