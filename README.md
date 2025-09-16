# TRiM: True Random Music
A music player that truly shuffles all your tracks so that you never know what's next. Originally made because Spotify's shuffle feature is biased, and the queue doesn't reshuffle once the last song in the queue plays.

## Features
### True (Pseudo)random
TRiM selects a random song from the queue every time the current one ends. Once all songs play, the queue is reset and TRiM picks another random song to play.

### Audio Format Support
TRiM *should* theoretically support all mainstream audio formats, so no matter if it's `mp3`, `wav`, or `ogg`, it will play.

### Modularity
TRiM can load plugins, adding new functionality to the app itself.

---

## Plugin Installation
To install a plugin, there are two methods supported by PF4J.

### ZIP file installation (recommended)
You can install a plugin by placing the ZIP (without decompressing) into `%APPDATA%\TRiM\plugins` (or `$HOME/.config/TRiM/plugins` in Linux)

### Direct installation
This method is slightly more complex, as it isn't just moving a file. You will require the JAR itself and the `plugin.properties`, found inside the `META-INF` folder of the JAR.
1. In the directory mentioned in the method above, create a directory with the name of the plugin (the `plugin.id` parameter inside `plugin.properties`).
2. Inside that directory, create another directory called `lib`.
3. Place the JAR into that folder.

### Verification
Open TRiM and click on the `Plugins` button. If the popup shows the plugin you wanted to install, you installed the plugin correctly.

## Plugin Management
To enable/disable plugins:
1. Open the `Plugins` button found inside the app.
2. Click the checkbox to the left of the plugin you want to enable/disable and repeat for every other plugin you might want to load/unload.
3. Restart the app.

## Plugin Development
If you'd like to create a plugin, please refer to [this guide](https://github.com/RocketSmash9000/TRiM/blob/master/src/main/resources/PLUGIN_DEVELOPMENT.md).

You will need a JAR of TRiM's core to act as a library, or else the plugin won't compile. We don't provide those, so you will need to clone this repository and build the JAR yourself.
