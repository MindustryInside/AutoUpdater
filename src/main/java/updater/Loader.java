package updater;

import arc.files.Fi;
import arc.util.*;
import mindustry.gen.Player;
import mindustry.mod.Mod;

import static mindustry.Vars.*;

public class Loader extends Mod{
    public static final String latestVersionUrl = "https://api.github.com/repos/Anuken/Mindustry/releases";
    public static Fi buildDirectory;

    public static Updater updater;

    @Override
    public void init(){
        buildDirectory = dataDirectory.child("release_builds/");

        if(mobile){
            Log.warn("AutoUpdater are not support on mobile version");
        }else{
            updater = new Updater();
        }
    }

    @Override
    public void registerServerCommands(CommandHandler handler){
        handler.register("upd", "Check updates", args -> {
            updater.checkUpdate(b -> Log.info(b ? "Update found" : "No, Update not found"));
        });
    }

    @Override
    public void registerClientCommands(CommandHandler handler){
        handler.<Player>register("upd", "Check updates", (args, player)-> {
            updater.checkUpdate(b -> player.sendMessage(b ? "[accent]Update found" : "[scarlet]No, Update not found"));
        });
    }
}
