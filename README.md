# SPCK Framework

This is a WIP project.

## Goal

SPCK framework provides a basic toolset (or starter kit) for gamedevs who write their 
games using their own engines and custom codes, but are not yet setup up cross-platform, future proof
architecture.

## Capabilities

* Linux Support (OpenGL)
* Windows Support (OpenGL)
* MacOS Support (OpenGL)
* Basic Input System

## Usage

## TODOs

More or less in priority order:

* Vulkan backend
* Write the demo shaders (basic lights (ambient, point, spot, etc) support)
* Tools for shader compilation
* GUI system (Canvases, texts, buttons, etc.)
* Batch rendering (maybe with ECS)
* Camera system
* Basic 2D toolset
* Basic 3D toolset
* Installer
* Physics system
* ... ?

## Usage

```bash
# Build
./gradlew build
```

```bash
# Run
./gradlew run
```

```bash
# Package
./gradlew jlink
```

```bash
# Run packed app 
./desktop/build/image/bin/app
```

## License

TBD