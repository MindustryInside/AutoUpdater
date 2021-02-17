# AutoUpdater
A small jar-mod for an auto updating Mindustry. <br>
*Works on desktop and server platforms!*

## Installation
Download the latest release (`.jar`) [here](https://github.com/MindustryInside/AutoUpdater/releases/latest). <br>
Put `.jar` to one of these paths:
* Windows: `%AppData%/Mindustry/mods`
* Linux: `~/.local/share/Mindustry/mods` 
* Server: `config/mods`

If you use for a server, you can also start the server automatically, use [this script](assets/run-jar.sh).
* `chmod +x run-jar.sh`
* `./run-jar.sh <server-filename>.jar`

## Building
* Windows: `gradlew jar`
* Linux/Mac OS: `./gradlew jar`
* Android: `gradlew androidJar` / `./gradlew androidJar`
* Composite: `gradlew deploy` / `./gradlew deploy`

After building, the `.jar` file should be located in `build/libs` folder.

If the terminal returns Permission denied or Command not found, run `chmod +x ./gradlew`.

## Extension
At the moment and most likely will never be implemented for the **Android** platform due to its restrictions
