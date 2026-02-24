# Jello Blur / Jello API

This repository contains the source code for the **Jello Blur** Minecraft mod and the **Jello API** shader library it's built on.

## Repository Structure

```
jello-api/             Jello API – standalone shader lifecycle library for Fabric 1.21.5+
jello-blur-1.21.8/    Jello Blur mod – Fabric 1.21.8 (uses Jello API event system)
jello-blur-1.21.11/   Jello Blur mod – Fabric 1.21.11 (uses vanilla ShaderLoader API directly)
```

## Building

### 1. Build and publish Jello API locally

```bash
cd jello-api
gradle publishToMavenLocal
```

### 2. Build Jello Blur

```bash
# For 1.21.8
cd jello-blur-1.21.8
gradle build

# For 1.21.11
cd jello-blur-1.21.11
gradle build
```

Output JARs will be in each project's `build/libs/` folder.

---

See [jello-api/README.md](jello-api/README.md) for documentation on using Jello API in your own mods.
