# CodeMP for IntelliJ
This repository contains the code for the CodeMP plugin for IntelliJ-family IDEs, as well as the glue for the JNI boilerplate generators for now.

## Compiling, Testing, Installing
You can run a test using the Gradle `runIde` task. It will open in a new window an IntelliJ IDE with the plugin installed.

With the `build` task you can generate a JAR that IntelliJ can *probably* load (this has not been tested yet).
You can do so by accessing the Settings menu (Ctrl + Alt + S or File > Settings > Plugins), then clicking on the gear icon and then on "Install Plugin from Disk...".

The Rust library, presently, is bundled at build-time into the JAR and will not update automatically.
This may change in future versions.
