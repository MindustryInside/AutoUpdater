# AutoUpdater
A small jar-mod for auto updating Mindustry. <br>
*Works on mobile, desktop and server platforms!*

## Installation
Download the latest release (`.jar`) [here](https://github.com/MindustryInside/AutoUpdater/releases/latest). <br>
Put `.jar` to one of these paths:
* Windows: `%AppData%/Mindustry/mods`
* Linux: `~/.local/share/Mindustry/mods` 
* Server: `config/mods`

## Building
* Windows: `gradlew deploy`
* Linux: `./gradlew deploy`
After building, the `.jar` file should be located in build/libs/InsideBot.jar folder.

If the terminal returns Permission denied or Command not found, run `chmod +x ./gradlew`.
