# SPCK Framework

This is a WIP project.

## Goals

### Short Term

- App is distributable
- Complete event system
- Window can be resized
- Java code formatter
- Separate 2D renderer - configurable render pipeline
- Texture & atlas loading from files

### Long Term

* Java 15: https://github.com/java9-modularity/gradle-modules-plugin/issues/169
* Vulkan backend
* Write the demo shaders (basic lights (ambient, point, spot, etc) support)
* GUI system (Canvases, texts, buttons, etc.)
* Batch rendering (maybe with ECS)
* Camera system
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