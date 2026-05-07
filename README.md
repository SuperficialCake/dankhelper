# Dank Helper

[Join Our Discord](https://discord.gg/VhGJcRsdam)

## Disclaimer

This project is a community driven tool and is not officially endorsed by or affiliated with DankPrison.com or Dank420Girl.

## Features

This mod adds a HUD to show helpful stats related to DankPrison. The hud tracks things, such as, Sunrise time, Income Per Minute, Total Artifacts, and more!

![Preview of In-Game UI](./previews/HUD%20Previeww.png)

Your sessions are saved in `.minecraft/dankhelper/sessions` and your frenzies in `.minecraft/dankhelper/frenzies/<frenzy_type>`.

You can view these CSVs in-game using `PG Down`

![Preview of Trends Sscreen](./previews/Trends%20Screen%20Preview.png)

You can also send your rewards to a discord webhook, just like the #rewards channel in the DankPrison discord!

![Preview of Discord Webhook](./previews/Webhook%20Preview.png)

### Default Keybinds

The mod has a few keybinds:

> END to manually end a session & reset the HUD.
> 
> H to show or hide the HUD
> 
> Backslash to open the config
> 
> Comma to move the UI
> 
> PG_DOWN to open the Trends screen

## How It Works

The mod listens to your chat for your mining summaries, make sure you have them toggled on! 
It then parses the message, and starts averaging your stats for the duration you are logged in, or manually end a session via the keybind.
SessionBM are tracked by hooking into the scoreboard, so if your DBM aren't displayed, the mod cannot track your session BM.

The mod also listens for any message starting with `(*) Increased Fortune` to track your fortune gains. Because of the way the messages are sent and listened to
there is no way a player can mess with your fortune count or your summaries.

## Installation

Install Fabric loader for version 1.21.11

Install [FabricAPI](https://modrinth.com/mod/fabric-api/versions?g=1.21.11)

Install [Fabric Lang Kotlin](https://modrinth.com/mod/fabric-language-kotlin)

Install [Cloth Config](https://modrinth.com/mod/cloth-config/versions?g=1.21.11&l=fabric)

Install [Mod Menu](https://modrinth.com/mod/modmenu/versions?g=1.21.11)

Download the latest jar from the [Releases Page](https://github.com/SuperficialCake/dankhelper/releases), or compile from source.

## Compilation Instructions

Download source for your Minecraft version

run `./gradle build`

JAR is output to `./build/libs/`

## Contribution

Feel free to contribute to the project. Please make an issue for any major changes.

## Contributors

- [YouAreACatgirl](https://github.com/tomdkap)
- [PepeNeutron](https://github.com/pepeneutron)


## License
[GNU GPL v3](https://choosealicense.com/licenses/gpl-3.0/)
