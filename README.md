# AutoUpdater
A small jar-mod for Mindustry client/server.

## Installation
Download the latest release (`.jar`) [here](https://github.com/MindustryInside/AutoUpdater/releases/latest). <br>
Put `.jar` to one of these paths:
* Windows: `%AppData%/Mindustry/mods`
* Linux: `~/.local/share/Mindustry/mods` 
* Server: `config/mods`

## Building
* Windows: `gradlew dist`
* Linux: `./gradlew dist`
After building, the `.jar` file should be located in build/libs/InsideBot.jar folder.

If the terminal returns Permission denied or Command not found, run `chmod +x ./gradlew`.

## Note
I can't compile a mobile version and test his.
