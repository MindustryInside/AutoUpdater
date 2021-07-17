package inside.updater;

import arc.*;
import arc.files.Fi;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.*;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.io.SaveIO;
import mindustry.mod.Mod;
import mindustry.net.Administration;

import static mindustry.Vars.*;

public class Loader extends Mod{
    public static final String releasesUrl = "https://api.github.com/repos/Anuken/Mindustry/releases";

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
            ui.settings.game.checkPref("auto-updater.unstable", false, b -> {
                // needed how update state, because button color in the main menu is based on this state
                updater.updateAvailable = false;
            });

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

        // based on 'config' command
        handler.register("updater", "[name] [value...]", "Configure auto-updater settings.", args -> {
            if(args.length == 0){
                Log.info("All update settings:");
                for(Setting s : Setting.all){
                    Log.info("&lk| @: @", s.name(), "&lc&fi" + s.get());
                    Log.info("&lk| | &lw" + s.description);
                    Log.info("&lk|");
                }
                return;
            }

            try{
                Setting s = Setting.valueOf(args[0]);
                if(args.length == 1){
                    Log.info("'@' is currently @.", s.name(), s.get());
                }else{
                    if(s.isBool()){
                        s.set(args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("on"));
                    }else if(s.isNum()){
                        try{
                            s.set(Integer.parseInt(args[1]));
                        }catch(NumberFormatException e){
                            Log.err("Not a valid number: @", args[1]);
                            return;
                        }
                    }else if(s.isString()){
                        s.set(args[1].replace("\\n", "\n"));
                    }

                    Log.info("@ set to @.", s.name(), s.get());
                    Core.settings.forceSave();
                }
            }catch(IllegalArgumentException e){
                Log.err("Unknown setting: '@'. Run the command with no arguments to get a list of valid settings.", args[0]);
            }
        });
    }
}
