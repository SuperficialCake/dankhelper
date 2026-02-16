# Dank Helper

[Join Our Discord](https://discord.gg/VhGJcRsdam)

## Disclaimer

This project is a community driven tool and is not officially endorsed by or affiliated with DankPrison.com or Dank420Girl.

## Features

This mod adds a HUD to show helpful stats related to DankPrison. The mod will also log your ChampionFrenzies and FishingFrenzies.

![Preview of In-Game UI](./resources/images/preview.png)

Your sessions are saved in `.minecraft/dankhelper/sessions` and your frenzies in `.minecraft/dankhelper/frenzies/<frenzy_type>`.

### Default Keybinds

The mod has a few keybinids:

> END to manually end a session & reset the HUD.
> 
> H to show or hide the HUD
> 
> Backslash to open the config
> 
> Comma to move the UI

## How It Works

The mod listens to your chat for your mining summaries, make sure you have them toggled on! 
It then parses the message, and starts averaging your stats for the duration you are logged in, or manually end a session via the keybind.
SessionBM are tracked by hooking into the scoreboard, so if your DBM aren't displayed, the mod cannot track your session BM.

The mod also listens for any message starting with `(*) Increased Fortune` to track your fortune gains. Because of the way the messages are sent and listened to
there is no way a player can mess with your fortune count or your summaries.

## Installation

Install Fabric loader for version 1.20.1

Install [FabricAPI](https://modrinth.com/mod/fabric-api/versions?g=1.21.1)

Install [Fabric Lang Kotlin](https://modrinth.com/mod/fabric-language-kotlin)

Install [Cloth Config](https://modrinth.com/mod/cloth-config/versions?g=1.21.1&l=fabric)

Install [Mod Menu](https://modrinth.com/mod/modmenu/versions?g=1.21.1)

Download the latest jar from the [Releases Page](https://github.com/SuperficialCake/dankhelper/releases), or compile from source.

## Contribution

Feel free to contribute to the project. Please make an issue for any major changes.

## License
[GNU GPL v3](https://choosealicense.com/licenses/gpl-3.0/)