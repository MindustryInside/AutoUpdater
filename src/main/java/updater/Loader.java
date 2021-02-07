package updater;

import arc.Events;
import arc.files.Fi;
import arc.util.*;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import mindustry.mod.Mod;
import mindustry.net.Administration;

import static mindustry.Vars.*;

public class Loader extends Mod{
    public static final String latestVersionUrl = "https://api.github.com/repos/Anuken/Mindustry/releases";

    public static Fi buildDirectory;
    public static Updater updater;

    @Override
    public void init(){
        buildDirectory = dataDirectory.child("release_builds/");

        updater = new Updater();

        Events.on(EventType.ClientLoadEvent.class, event -> {
            if(Administration.Config.autoUpdate.bool() && updater.active()){
                Fi fi = saveDirectory.child("autosave.msav");
                if(fi.exists()){
                    try{
                        SaveIO.load(fi);
                        Log.info("Auto-save loaded.");
                        state.set(GameState.State.playing);
                        netServer.openServer();
                    }catch(Throwable t){
                        Log.err(t);
                    }
                }
            }
        });
    }

    @Override
    public void registerServerCommands(CommandHandler handler){

        handler.register("upd", "Check updates", args -> {
            updater.checkUpdate(b -> Log.info(b ? "Update found" : "No, Update not found"));
        });
    }

    @Override
    public void registerClientCommands(CommandHandler handler){

        if(!headless){
            handler.<Player>register("upd", "Check updates", (args, player) -> {
                updater.checkUpdate(b -> player.sendMessage(b ? "[accent]Update found" : "[scarlet]No, Update not found"));
            });
        }
    }
}
