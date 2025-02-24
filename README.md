
# IdleRestart Plugin Documentation

## Overview

The `IdleRestart` plugin is designed for Paper Minecraft servers to automatically restart the server after a specified period of inactivity. This plugin helps maintain server performance and ensures that the server is available during peak hours. It includes configurable options for idle time, player count, broadcast messages, and sounds.

## Features

- Automatically restarts the server after a configurable period of inactivity.
- Configurable idle time and player count thresholds.
- Broadcasts messages with sounds when a restart is scheduled.
- Allows administrators to force a restart within a specified number of minutes.
- Delays restart when a player joins the server.

## Installation

1. **Download the Plugin**: Obtain the `IdleRestart.jar` file from the releases section or build it from the source.
2. **Place the JAR File**: Move the `IdleRestart.jar` file into the `plugins` directory of your Paper server.
3. **Start/Restart the Server**: Start or restart your Paper server to load the plugin.
4. **Configure the Plugin**: Edit the `config.yml` file in the `plugins/IdleRestart` directory to customize the plugin settings.

## Configuration

The `config.yml` file contains the following configurable options:

```yaml
# IdleRestart Configuration

# Number of minutes to wait before restarting the server when idle
idle-minutes: 10

# Number of players below which the server is considered idle
number-of-players: 0

# Whether to broadcast a message with a sound when a restart is scheduled
broadcast-restart: true

# Sound to play when a restart is broadcasted
restart-sound: ENTITY_PLAYER_LEVELUP

# Delay in minutes to restart when a player joins
join-delay-minutes: 5

# Prefix for all plugin messages
idle-prefix: "[&8[&6IR🔙&8]&r]"
```

### Configuration Options

- **`idle-minutes`**: The number of minutes the server must be idle before a restart is triggered.
- **`number-of-players`**: The number of players below which the server is considered idle.
- **`broadcast-restart`**: Whether to broadcast a message with a sound when a restart is scheduled.
- **`restart-sound`**: The sound to play when a restart is broadcasted.
- **`join-delay-minutes`**: The delay in minutes before restarting the server when a player joins.
- **`idle-prefix`**: The prefix to include in all messages sent by the plugin.

## Commands

### `/idlerestart <idleMinutes> <numberOfPlayers>`

- **Description**: Configures the idle time and player count for the idle restart feature.
- **Usage**: `/idlerestart 15 0`
- **Permission**: `idlerestart.use`

### `/idlerestart --force <minutes>`

- **Description**: Forces a server restart in the specified number of minutes, bypassing the idle check.
- **Usage**: `/idlerestart --force 5`
- **Permission**: `idlerestart.use`

### `/idlerestartstatus`

- **Description**: Checks the status of the pending restart.
- **Usage**: `/idlerestartstatus`
- **Permission**: `idlerestart.status`

## Permissions

- **`idlerestart.use`**: Allows using the `/idlerestart` command to configure idle restarts or force a restart.
- **`idlerestart.status`**: Allows using the `/idlerestartstatus` command to check the status of a pending restart.

## Example Usage

1. **Configure Idle Restart**:
  - Use the command `/idlerestart 15 0` to set the idle time to 15 minutes and consider the server idle when there are 0 players online.
2. **Force Immediate Restart**:
  - Use the command `/idlerestart --force 5` to force a server restart in 5 minutes, regardless of the current player count.
3. **Check Restart Status**:
  - Use the command `/idlerestartstatus` to check if a restart is pending and how long it has been pending.

## Support
For support or feature requests, please open an issue on the GitHub repository.
