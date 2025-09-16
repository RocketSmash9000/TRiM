# TRiM Plugin Development Guide

Welcome to the TRiM plugin development guide. This document explains how to build, package, and distribute plugins for the TRiM player, how plugins are loaded, and how you can extend functionality. It references the current codebase so the instructions remain accurate.

Key classes to know:
- `com.github.RocketSmash9000.plugin.TRiMPlugin` – base plugin interface with lifecycle and metadata.
- `com.github.RocketSmash9000.plugin.AbstractTRiMPlugin` – convenient abstract base class that implements metadata and provides no-op lifecycle methods.
- `com.github.RocketSmash9000.plugin.PluginManager` – PF4J-backed loader used by TRiM at runtime.
- `com.github.RocketSmash9000.ui.PluginManagerDialog` – JavaFX dialog to view and enable/disable plugins.
- Example plugin: `com.github.RocketSmash9000.plugin.example.ExamplePlugin`.
 - UI extension point: `com.github.RocketSmash9000.plugin.ui.ToolbarButtonExtension` – let plugins contribute toolbar buttons.

TRiM uses [PF4J](https://pf4j.org/) under the hood for plugin discovery and lifecycle management.


## How plugins are loaded at runtime
- On app start, `com.github.RocketSmash9000.Main` calls `initializePluginManager()` which:
  - Ensures plugin directory exists at: `%APPDATA%/TRiM/plugins` (Windows) or `$HOME/.config/TRiM/plugins` (Linux/macOS), as provided by `AppDirectories.getPluginsDir()`.
  - Constructs `new PluginManager(pluginsDir, false)` and calls `initialize()`.
- `PluginManager.initialize()` then:
  - Uses PF4J `DefaultPluginManager` to `loadPlugins()` from the plugins directory.
  - Starts only the plugins that are enabled in the config (selective start). Disabled plugins remain discovered but stopped.
  - For each started PF4J plugin, TRiM obtains the `TRiMPlugin` instance and calls `onLoad()`; the plugin is tracked in memory.
- Enabling/disabling a plugin from the UI calls `PluginManager.setPluginEnabled(pluginId, enabled)`, which dynamically starts/stops the PF4J plugin and calls `onLoad()`/`onUnload()` accordingly (no app restart required).
- On shutdown, `PluginManager.shutdown()` calls `onUnload()` for each loaded plugin and stops/unloads plugins in PF4J.

Note: Development mode is supported by the underlying PF4J manager, but `Main` currently initializes `PluginManager` with `developmentMode = false`.


## Minimum plugin structure
Every TRiM plugin has two parts:

1) A PF4J Plugin entry class that implements TRiM’s `TRiMPlugin` interface.
2) One or more PF4J `@Extension` classes that implement specific extension points (e.g., `ToolbarButtonExtension`).

Example PF4J Plugin entry class:

```java
package com.example.myplugin;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import com.github.RocketSmash9000.plugin.TRiMPlugin;

public class MyPlugin extends Plugin implements TRiMPlugin {
    public MyPlugin(PluginWrapper wrapper) { super(wrapper); }

    // TRiM lifecycle
    @Override public void onLoad() { /* init resources */ }
    @Override public void onUnload() { /* cleanup */ }

    // TRiM metadata
    @Override public String getPluginId() { return "com.example.myplugin"; }
    @Override public String getDisplayName() { return "My Plugin"; }
    @Override public String getVersion() { return "1.0.0"; }
    @Override public String getMinimumApplicationVersion() { return "1.0.0"; }
    @Override public String getDescription() { return "Adds custom functionality to TRiM"; }
}
```

Example Extension class (adds a toolbar button):

```java
package com.example.myplugin;

import com.github.RocketSmash9000.plugin.ui.ToolbarButtonExtension;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

@Extension
public class MyToolbarButton implements ToolbarButtonExtension, ExtensionPoint {
    @Override public String getText() { return "My Action"; }
    @Override public String getTooltip() { return "Do something cool"; }
    @Override public void onAction() { System.out.println("[MyToolbarButton] Clicked!"); }
}
```

Compare with the example shipped in TRiM: `src/main/java/com/github/RocketSmash9000/plugin/example/ExamplePlugin.java` and its UI extension example.


## Packaging and distribution (PF4J plugin ZIP)
TRiM expects plugins in one of two PF4J-supported layouts:

- A ZIP file placed in the plugins directory, with:
  - `plugin.properties` at the ZIP root, and
  - Your plugin JAR inside `lib/` (e.g., `lib/my-trim-plugin-1.0.0.jar`).

- Or a directory placed under the plugins directory, with the same structure:
  - `%APPDATA%/TRiM/plugins/my-trim-plugin/`
    - `plugin.properties` (file at directory root)
    - `lib/my-trim-plugin-1.0.0.jar`

plugin.properties (at ZIP/directory root):

```properties
plugin.id=com.example.myplugin
plugin.version=1.0.0
plugin.provider=Your Name or Org
plugin.class=com.example.myplugin.MyPlugin
plugin.description=Adds custom functionality to TRiM
```

Notes:
- `plugin.class` must point to your PF4J Plugin entry class (the one that implements `TRiMPlugin`).
- This `plugin.properties` is separate from any resources inside your JAR. TRiM uses PF4J’s descriptor finder that reads the ZIP/directory root.


### Example Maven setup for a plugin
Create a separate Maven project for your plugin. Minimal `pom.xml`:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.example</groupId>
  <artifactId>my-trim-plugin</artifactId>
  <version>1.0.0</version>
  <name>My TRiM Plugin</name>

  <properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <pf4j.version>3.12.0</pf4j.version>
  </properties>

  <dependencies>
    <!-- PF4J API -->
    <dependency>
      <groupId>org.pf4j</groupId>
      <artifactId>pf4j</artifactId>
      <version>${pf4j.version}</version>
      <scope>provided</scope>
    </dependency>

    <!-- If your plugin uses any TRiM public APIs, depend on the published TRiM API as provided. -->
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.11.0</version>
        <configuration>
          <source>21</source>
          <target>21</target>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jar-plugin</artifactId>
        <version>3.3.0</version>
        <configuration>
          <archive>
            <manifestEntries>
              <!-- Optional: metadata -->
            </manifestEntries>
          </archive>
        </configuration>
      </plugin>
      <!-- No fat-jar/assembly needed. Produce a thin JAR; package into a PF4J ZIP manually or via your build system. -->
    </plugins>
  </build>
</project>
```

For PF4J ZIP packaging, place a standalone `plugin.properties` next to your built JAR when creating the ZIP (do not rely solely on a descriptor inside the JAR).

ZIP file layout:
```
plugin.properties
lib/
    my-trim-plugin.jar
```


### Directory structure example
```
my-trim-plugin/
  src/
    main/
      java/
        com/example/myplugin/MyPlugin.java
      resources/
        META-INF/
          plugin.properties
  pom.xml
```


## Installing the plugin
Option A — ZIP install (recommended for distribution):
1. Build your plugin JAR: `mvn -q -DskipTests package`.
2. Create a structure on disk:
   - `plugin.properties` (file)
   - `lib/your-plugin-<version>.jar`
3. Zip these items so that `plugin.properties` is at the ZIP root and JAR is under `lib/`.
4. Copy the ZIP into TRiM’s plugins directory:
   - Windows: `%APPDATA%\TRiM\plugins\`
   - macOS/Linux: `$HOME/.config/TRiM/plugins/`
5. Start TRiM. Enable/disable in the Plugin Manager as needed.

Option B — Directory install (useful during development):
1. Create a folder, e.g., `%APPDATA%\TRiM\plugins\my-trim-plugin\`.
2. Put `plugin.properties` in that folder and the built JAR under `lib/`.
3. Start TRiM and manage the plugin from the Plugin Manager.


## Plugin lifecycle and metadata
- `onLoad()` – Called once the plugin is discovered and the app deems it enabled. Use for initialization.
- `onUnload()` – Called when the plugin is being disabled or the app is shutting down. Clean up resources here.
- `getPluginId()` – Must be globally unique; reverse-DNS style is recommended.
- `getDisplayName()`, `getVersion()`, `getMinimumApplicationVersion()` – Provide metadata for display and compatibility.
- `getDescription()` – Optional default method to describe plugin functionality.

The abstract base `AbstractTRiMPlugin` implements the metadata getters and provides no-op lifecycle methods so you can focus on your logic.


## Extending TRiM functionality
TRiM exposes two kinds of extensibility right now:

- Lifecycle hooks via `TRiMPlugin` (`onLoad`/`onUnload`) for general-purpose behavior.
- A UI extension point via `ToolbarButtonExtension` that lets plugins add buttons to the bottom controls bar.

The app discovers extensions with PF4J `@Extension` and collects them using `PluginManager.getExtensions(Class<T>)`.


### UI Extension: ToolbarButtonExtension
Implement `com.github.RocketSmash9000.plugin.ui.ToolbarButtonExtension` in your plugin and annotate it with `@org.pf4j.Extension` to contribute a button to TRiM’s toolbar. TRiM will create a `javafx.scene.control.Button` for each extension at startup.

Interface shape:
```java
public interface ToolbarButtonExtension {
    String getText();
    default String getTooltip() { return null; }
    default javafx.scene.Node getGraphic() { return null; }
    default String getStyle() { return null; }
    default boolean initiallyDisabled() { return false; }
    void onAction(); // called on JavaFX Application Thread
}
```

Minimal example:

```java
package com.example.myplugin;

import com.github.RocketSmash9000.plugin.ui.ToolbarButtonExtension;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

@Extension
public class MyToolbarButton implements ToolbarButtonExtension, ExtensionPoint {
	@Override
	public String getText() {
		return "My Action";
	}

	@Override
	public String getTooltip() {
		return "Do something cool";
	}

	@Override
	public void onAction() {
		System.out.println("[MyToolbarButton] Clicked!");
		// Your UI-safe logic here
	}
}
```

Example shipped in this repo: `com.github.RocketSmash9000.plugin.example.ExampleToolbarButton`.

Notes:
- TRiM wires these buttons inside `com.github.RocketSmash9000.Main` by calling `pluginManager.getExtensions(ToolbarButtonExtension.class)` and adding them to the controls `HBox`.
- Exceptions thrown in `onAction` are caught and logged so they don’t crash the UI.
- If you supply a `graphic` node, you can leave `getText()` empty or combine both.


## Interacting with TRiM UI (current capabilities and future direction)
- The UI provides a Plugins dialog (`PluginManagerDialog`) showing available plugins and allowing enable/disable via `PluginManagerController`.
- Plugins can currently add buttons to the bottom controls bar via `ToolbarButtonExtension`.
- Future extension points may include menus, panels, and visualizations using similar patterns (`@Extension` + small extension interfaces). When added, they will be documented here and discovered via `PluginManager.getExtensions(...)`.


## Logging and diagnostics
Use standard Java logging or `System.out.println` for quick diagnostics. Log early in `onLoad()` and `onUnload()` so you can confirm activation in TRiM’s console output.

```java
@Override
public void onLoad() {
    System.out.println("[MyPlugin] Loaded v" + getVersion());
}

@Override
public void onUnload() {
    System.out.println("[MyPlugin] Unloaded");
}
```


## Versioning and compatibility
- Follow semantic versioning for your plugin versions.
- Set `getMinimumApplicationVersion()` to the minimum TRiM version you support.
- If TRiM introduces new extension APIs, prefer feature detection when possible and document your minimum requirements in both `plugin.properties` and code.


## Testing your plugin locally
- Place your built JAR in the plugins directory and start TRiM.
- Toggle enable/disable from the Plugins dialog to verify `onLoad()` and `onUnload()` are called.
- For rapid iteration, stop TRiM, replace the JAR, and start again.

Advanced: TRiM’s `PluginManager` supports a PF4J development mode flag, but `Main` currently constructs the manager with `developmentMode = false`. If you fork TRiM for development, you can switch it to `true` to enable PF4J development workflows.


## Example: A minimal plugin that greets

```java
package com.example.hello;

import com.github.RocketSmash9000.plugin.AbstractTRiMPlugin;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

@Extension
public class HelloPlugin extends AbstractTRiMPlugin implements ExtensionPoint {
	public HelloPlugin() {
		super("com.example.hello", "Hello Plugin", "1.0.0", "1.0.0");
	}

	@Override
	public void onLoad() {
		System.out.println("Hello from HelloPlugin!");
	}

	@Override
	public void onUnload() {
		System.out.println("Goodbye from HelloPlugin!");
	}
}
```

`META-INF/plugin.properties`:
```properties
plugin.id=com.example.hello
plugin.version=1.0.0
plugin.description=Prints greetings at load/unload
plugin.provider=Example
plugin.class=com.example.hello.HelloPlugin
```


## Distributing your plugin
- Share your JAR directly or host it in a repository.
- Provide a README with installation steps and the tested TRiM version.
- Consider signing releases and including checksums. *Optional*


## Troubleshooting
- Plugin not appearing in the list:
  - Ensure that the plugin structure is correct.
  - Verify `META-INF/plugin.properties` exists in the JAR and `plugin.class` is correct.
  - Check that your plugin class is public and has a no-arg constructor.
- Plugin loads but does nothing:
  - Confirm you implemented `onLoad()` and see logs in the console.
- ClassNotFound errors:
  - Mark PF4J as `provided` and shade any third-party dependencies that must be bundled, or document them.


## API surface summary (from this codebase)
- `TRiMPlugin` methods: `onLoad()`, `onUnload()`, `getPluginId()`, `getDisplayName()`, `getVersion()`, `getMinimumApplicationVersion()`, optional `getDescription()`.
- `AbstractTRiMPlugin` implements metadata and provides default lifecycle.
- `PluginManager` supports:
  - `initialize()`, `shutdown()`
  - `getPlugin(String id)`, `getPlugins()`
  - `getExtensions(Class<T> type)` – PF4J extensions retrieval hook for future extension points.
  - `isPluginEnabled(TRiMPlugin)`, `setPluginEnabled(String id, boolean enabled)`

This reflects the current state of TRiM’s plugin system in the repository. As new extension points are added (UI, playback hooks, queue manipulation, etc.), this guide can be expanded with concrete examples.
