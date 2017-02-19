# MCMultiPart
A universal multipart API for modern Minecraft.

### Adding MCMultiPart 2 to your workspace

To add MCMultiPart to your dev environment and be able to use it in your mods, you need to add the following lines to the buildscript, replacing `<mcmp_version>` with the version you want to use:

    repositories {
        maven { url "http://maven.amadornes.com/" }
    }
    dependencies {
        deobfCompile "MCMultiPart2:MCMultiPart:<mcmp_version>"
    }

If you need to test a build that is still not released, you can depend on `MCMultiPart-exp` instead of `MCMultiPart`. Be wary, though, that experimental builds are NOT supported. YOU are responsible of your mod if it depends on an experimental build and said build is taken down.

Also, could you PLEASE not use DepLoaders, especially on other people's mavens? Bandwidth costs money and I do NOT want to have to deal with users complaining about experimental builds being taken down.