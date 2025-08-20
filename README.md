# IdleRestart Plugin Documentation

## Overview

The `IdleRestart` plugin is designed for **PaperMC servers** and **Velocity proxies** to automatically restart after a specified period of inactivity. This plugin helps maintain performance and ensures availability.

This new version introduces scheduled restarts, Discord webhook notifications, and a developer API for advanced integration on both platforms.

## Features

- Automatically restarts the server/proxy after a configurable period of inactivity.
- **NEW**: Schedule automatic restarts at specific times of the day with timezone support.
- **NEW**: Send notifications to a Discord webhook when a restart is scheduled.
- Configurable idle time and player count thresholds.
- Broadcasts messages when a restart is scheduled (with sounds on Paper).
- Allows administrators to force a restart or reload the configuration.
- Delays restart if a player joins the server/proxy.
- **NEW**: A developer API for other plugins to interact with IdleRestart.

## Installation

1.  **Download the Plugin**: Obtain the `IdleRestart.jar` file from the releases section. This single JAR works for both Paper and Velocity.
2.  **Place the JAR File**:
    *   **For Paper servers**: Move `IdleRestart.jar` into the `plugins` directory.
    *   **For Velocity proxies**: Move `IdleRestart.jar` into the `plugins` directory.
3.  **Start/Restart**: Start or restart your server/proxy to load the plugin.
4.  **Configure**:
    *   **For Paper**: Edit `config.yml` in `plugins/IdleRestart/`.
    *   **For Velocity**: Edit `idlerestart.toml` in `plugins/idlerestart/`.

## Configuration

### Paper (`plugins/IdleRestart/config.yml`)

```yaml
# IdleRestart Configuration for Paper
idle-minutes: 10
number-of-players: 0
broadcast-restart: true
restart-sound: ENTITY_PLAYER_LEVELUP
join-delay-minutes: 5
idle-prefix: "&8&l[&6&lIR🕙&8&l]&r"

# Scheduled Restarts
scheduled-restarts:
  enabled: false
  times: ["04:00", "16:00"]
  timezone: "UTC"

# Discord Webhook
discord:
  enabled: false
  webhook-url: ""
  server-name: "My Server"
```

### Velocity (`plugins/idlerestart/idlerestart.toml`)

```toml
# IdleRestart Configuration for Velocity
idle-minutes = 10
number-of-players = 0
broadcast-restart = true
restart-sound = "ENTITY_PLAYER_LEVELUP" # Ignored by Velocity
join-delay-minutes = 5
idle-prefix = "[&8[&6IR🔙&8]&r]"

# Scheduled Restarts
[scheduled-restarts]
enabled = false
times = ["04:00", "16:00"]
timezone = "UTC"

# Discord Webhook
[discord]
enabled = false
webhook-url = ""
server-name = "My Proxy"
```

### Configuration Options

-   `idle-minutes`: Minutes the server/proxy must be idle before a restart.
-   `number-of-players`: Player count threshold for idle status.
-   `broadcast-restart`: Whether to broadcast a message on restart.
-   `restart-sound`: Sound to play on broadcast (Paper only).
-   `join-delay-minutes`: Delay before re-initiating idle check after a player cancels a restart by joining.
-   `idle-prefix`: Prefix for plugin messages (supports `&` color codes).
-   `scheduled-restarts.enabled`: Enable/disable scheduled restarts.
-   `scheduled-restarts.times`: List of times ("HH:mm") to schedule restarts.
-   `scheduled-restarts.timezone`: Timezone for scheduled restarts (e.g., "UTC", "America/New_York").
-   `discord.enabled`: Enable/disable Discord webhook notifications.
-   `discord.webhook-url`: URL for the Discord webhook.
-   `discord.server-name`: Name for your server/proxy in Discord notifications.

## Commands

| Command                                      | Description                                       | Platform | Permission            |
| -------------------------------------------- | ------------------------------------------------- | -------- | --------------------- |
| `/idlerestart <minutes> <players>`           | Sets the idle time and player threshold.          | Paper    | `idlerestart.use`     |
| `/vidlerestart <minutes> <players>`          | Sets the idle time and player threshold.          | Velocity | `idlerestart.use`     |
| `/idlerestart --force <minutes>`             | Forces a server restart in `<minutes>`.           | Paper    | `idlerestart.use`     |
| `/vidlerestart --force <minutes>`            | Forces a proxy restart in `<minutes>`.            | Velocity | `idlerestart.use`     |
| `/idlerestart reload`                        | Reloads the plugin's configuration.               | Paper    | `idlerestart.reload`  |
| `/vidlerestart reload`                       | Reloads the plugin's configuration.               | Velocity | `idlerestart.reload`  |
| `/idlerestartstatus`                         | Checks the status of a pending restart.           | Paper    | `idlerestart.status`  |
| `/vidlerestartstatus`                        | Checks the status of a pending restart.           | Velocity | `idlerestart.status`  |

## Permissions

-   **`idlerestart.use`**: Allows using the main configuration and force commands.
-   **`idlerestart.status`**: Allows checking the status of a pending restart.
-   **`idlerestart.reload`**: Allows reloading the plugin's configuration.

## For Developers

IdleRestart provides a simple yet powerful API for integration.

### Dependency

**Maven:**
```xml
<dependency>
    <groupId>me.benrobson</groupId>
    <artifactId>IdleRestart</artifactId>
    <version>1.0.0</version> <!-- Use the latest version -->
    <scope>provided</scope>
</dependency>
```

**Paper (`plugin.yml`):**
```yaml
depend: [IdleRestart]
```

**Velocity (`velocity-plugin.json`):**
```json
"dependencies": [
  {
    "id": "idlerestart",
    "optional": false
  }
]
```

### Accessing the API

**Paper/Bukkit:**
```java
import me.benrobson.idlerestart.api.IdleRestartAPI;
import org.bukkit.plugin.RegisteredServiceProvider;

// In your onEnable method:
RegisteredServiceProvider<IdleRestartAPI> provider = getServer().getServicesManager().getRegistration(IdleRestartAPI.class);
if (provider != null) {
    IdleRestartAPI api = provider.getProvider();
    // Use the API
}
```

**Velocity:**
The API is not available via a service manager on Velocity in this version. You can interact with the plugin via its commands or by depending on it and accessing the main instance (not recommended).

### API Methods

-   `void scheduleRestart(int minutes, String reason)`
-   `void forceRestart(int minutes)`
-   `void cancelRestart(String reason)`
-   `boolean isRestartPending()`
-   `long getRestartInitiatedTimeMillis()`
-   `long getActualRestartTimeMillis()`

### API Events (Paper/Bukkit Only)

-   `RestartScheduledEvent`
-   `RestartCancelledEvent`
-   `RestartForcedEvent`

**Example Listener:**
```java
import me.benrobson.idlerestart.api.events.RestartScheduledEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MyListener implements Listener {
    @EventHandler
    public void onRestartScheduled(RestartScheduledEvent event) {
        System.out.println("Server is restarting in " + event.getMinutes() + " minutes. Reason: " + event.getReason());
    }
}
```

## Support

For support or feature requests, please open an issue on the GitHub repository.
