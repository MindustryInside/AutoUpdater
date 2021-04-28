package inside.updater;

import arc.Events;
import arc.files.Fi;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.*;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.io.SaveIO;
import mindustry.mod.Mod;
import mindustry.net.Administration;

import static mindustry.Vars.*;

public class Loader extends Mod{
    public static final String latestVersionUrl = "https://api.github.com/repos/Anuken/Mindustry/releases";

    public static Fi buildDirectory;

    public Updater updater;

    @Override
    public void init(){
        buildDirectory = dataDirectory.child("release_builds/");

        buildDirectory.findAll(fi -> fi.name().startsWith("Mindustry-") && fi.extEquals("jar")).each(Fi::delete);

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

        if(updater.active() && !headless){
            ui.menuGroup.fill(c -> c.bottom().right().button("@auto-updater.check", Icon.refresh, () -> {
                ui.loadfrag.show();
                updater.checkUpdate(result -> {
                    ui.loadfrag.hide();
                    if(!result){
                        ui.showInfo("@auto-updater.noupdates");
                    }
                });
            }).size(200, 60).name("auto-updater.check").update(t -> {
                t.getLabel().setColor(updater.isUpdateAvailable() ? Tmp.c1.set(Color.white).lerp(Pal.accent, Mathf.absin(5f, 1f)) : Color.white);
            }));
        }
    }

    @Override
    public void registerServerCommands(CommandHandler handler){
        handler.register("upd", "Check updates", args -> {
            updater.checkUpdate(b -> Log.info(b ? "" : "Update not found"));
        });
    }
}
