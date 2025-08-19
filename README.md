
# IdleRestart Plugin Documentation

## Overview

The `IdleRestart` plugin is designed for **Paper Minecraft servers** and **Velocity proxies** to automatically restart the server/proxy after a specified period of inactivity. This plugin helps maintain server/proxy performance and ensures availability. It includes configurable options for idle time, player count, broadcast messages, and sounds (sounds are Paper-only).

## Features

- Automatically restarts the server/proxy after a configurable period of inactivity.
- Configurable idle time and player count thresholds.
- Broadcasts messages when a restart is scheduled (with sounds on Paper).
- Allows administrators to force a restart within a specified number of minutes.
- Delays restart when a player joins the server/proxy.
- Supports both Paper (Bukkit-based servers) and Velocity proxies.

## Installation

1.  **Download the Plugin**: Obtain the `IdleRestart.jar` file from the releases section or build it from the source. This single JAR file works for both Paper and Velocity.
2.  **Place the JAR File**:
    *   **For Paper servers**: Move the `IdleRestart.jar` file into the `plugins` directory of your Paper server.
    *   **For Velocity proxies**: Move the `IdleRestart.jar` file into the `plugins` directory of your Velocity proxy.
3.  **Start/Restart the Server/Proxy**: Start or restart your Paper server or Velocity proxy to load the plugin.
4.  **Configure the Plugin**:
    *   **For Paper**: Edit the `config.yml` file in the `plugins/IdleRestart` directory.
    *   **For Velocity**: Edit the `idlerestart.toml` file in the `plugins/idlerestart` directory (note the lowercase plugin ID for the folder).

## Configuration

Configuration options are similar for both platforms but reside in different files.

### Paper (`plugins/IdleRestart/config.yml`)

```yaml
# IdleRestart Configuration for Paper

# Number of minutes to wait before restarting the server when idle
idle-minutes: 10

# Number of players below which the server is considered idle
number-of-players: 0

# Whether to broadcast a message with a sound when a restart is scheduled
broadcast-restart: true

# Sound to play when a restart is broadcasted (Paper only)
restart-sound: "ENTITY_PLAYER_LEVELUP" # Ensure sound names are valid Bukkit Sound enums

# Delay in minutes to restart when a player joins and cancels a pending restart
join-delay-minutes: 5

# Prefix for all plugin messages (uses legacy '&' color codes)
idle-prefix: "[&8[&6IR🔙&8]&r]"
```

### Velocity (`plugins/idlerestart/idlerestart.toml`)

```toml
# IdleRestart Configuration for Velocity

# Number of minutes to wait before restarting the proxy when idle
idle-minutes = 10

# Number of players below which the proxy is considered idle
number-of-players = 0

# Whether to broadcast a message when a restart is scheduled
# Sound is not supported on Velocity.
broadcast-restart = true

# Sound to play (ignored by Velocity, but kept for consistency)
restart-sound = "ENTITY_PLAYER_LEVELUP"

# Delay in minutes to restart when a player joins and cancels a pending restart
join-delay-minutes = 5

# Prefix for all plugin messages (uses legacy '&' color codes)
idle-prefix = "[&8[&6IR🔙&8]&r]"
```

### Configuration Options (Common)

-   **`idle-minutes`**: The number of minutes the server/proxy must be idle before a restart is triggered.
-   **`number-of-players`**: The number of players (on the server for Paper, on the proxy for Velocity) below which it's considered idle.
-   **`broadcast-restart`**: Whether to broadcast a message when a restart is scheduled.
-   **`restart-sound`**: The sound to play when a restart is broadcasted. **Note: This option is only effective on Paper servers. Velocity does not support playing sounds directly.**
-   **`join-delay-minutes`**: The delay in minutes before re-initiating an idle check if a pending restart was cancelled due to a player joining.
-   **`idle-prefix`**: The prefix to include in all messages sent by the plugin (supports standard Minecraft `&` color codes).

## Commands

Commands are similar for both platforms, but Velocity commands are typically prefixed with `v_` or aliased to avoid conflicts if ever run in a mixed environment (though this plugin uses distinct aliases). Permissions remain the same.

### Common Commands (Paper & Velocity)

#### Configure Idle Restart
-   **Description**: Configures the idle time and player count for the idle restart feature.
-   **Paper Usage**: `/idlerestart <idleMinutes> <numberOfPlayers>`
    -   Example: `/idlerestart 15 0`
-   **Velocity Usage**: `/vidlerestart <idleMinutes> <numberOfPlayers>` (alias: `/v_idlerestart`)
    -   Example: `/vidlerestart 15 0`
-   **Permission**: `idlerestart.use`

#### Force Restart
-   **Description**: Forces a server/proxy restart in the specified number of minutes, bypassing the idle check.
-   **Paper Usage**: `/idlerestart --force <minutes>`
    -   Example: `/idlerestart --force 5`
-   **Velocity Usage**: `/vidlerestart --force <minutes>` (alias: `/v_idlerestart --force <minutes>`)
    -   Example: `/vidlerestart --force 5`
-   **Permission**: `idlerestart.use`

#### Check Restart Status
-   **Description**: Checks the status of the pending restart.
-   **Paper Usage**: `/idlerestartstatus`
-   **Velocity Usage**: `/vidlerestartstatus` (alias: `/v_idlerestartstatus`)
-   **Permission**: `idlerestart.status`

## Permissions

-   **`idlerestart.use`**: Allows using the main configuration and force commands.
-   **`idlerestart.status`**: Allows using the status command to check pending restarts.

## Example Usage

1.  **Configure Idle Restart (Paper)**:
    *   Use `/idlerestart 15 0` to set idle time to 15 minutes and 0 players.
2.  **Configure Idle Restart (Velocity)**:
    *   Use `/vidlerestart 15 0` to set idle time to 15 minutes and 0 players across the proxy.
3.  **Force Immediate Restart (Paper)**:
    *   Use `/idlerestart --force 5` to force a server restart in 5 minutes.
4.  **Force Immediate Restart (Velocity)**:
    *   Use `/vidlerestart --force 5` to force a proxy restart in 5 minutes.
5.  **Check Restart Status (Paper/Velocity)**:
    *   Use `/idlerestartstatus` (Paper) or `/vidlerestartstatus` (Velocity) to check pending restarts.

## Support

For support or feature requests, please open an issue on the GitHub repository.
