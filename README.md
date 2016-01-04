# MCMultiPart
A universal multipart API for Minecraft 1.8.9.

Suggestions are welcome, but bear in mind this is not the mod that will be adding microblocks, just the library that they will be built on top of.

### Adding MCMultiPart to your workspace

To add MCMultiPart to your dev environment and be able to use it in your mods, you need to add the following lines to the buildscript, replacing `<mcmp_version>` with the version you want to use:

    repositories {
        maven { url "http://maven.amadornes.com/" }
    }
    dependencies {
        deobfCompile "MCMultiPart:MCMultiPart:<mcmp_version>:universal"
    }
