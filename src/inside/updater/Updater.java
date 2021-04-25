package inside.updater;

import arc.*;
import arc.files.Fi;
import arc.func.*;
import arc.util.*;
import arc.util.async.*;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.core.Version;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.io.SaveIO;
import mindustry.net.Administration;
import mindustry.net.Packets.KickReason;
import mindustry.ui.Bar;
import mindustry.ui.dialogs.BaseDialog;

import java.io.*;
import java.net.*;

import static mindustry.Vars.*;
import static mindustry.core.Version.*;
import static inside.updater.Loader.*;

public class Updater{
    private static final int updateInterval = 60;

    private final AsyncExecutor executor = new AsyncExecutor(1);
    private boolean checkUpdates = true;
    private boolean updateAvailable;
    private String updateUrl;
    private String updateBuild;

    public boolean active(){
        return !Version.type.equals("bleeding-edge");
    }

    public Updater(){
        if(active()){
            Timer.schedule(() -> {
                if(checkUpdates && (headless || state.isMenu())){
                    checkUpdate(t -> {});
                }
            }, updateInterval, updateInterval);
        }
    }

    public boolean isUpdateAvailable(){
        return updateAvailable;
    }

    public void checkUpdate(Boolc done){
        Core.net.httpGet(latestVersionUrl, res -> {
            if(res.getStatus() == Net.HttpStatus.OK){
                Jval val = Jval.read(res.getResultAsString()).asArray().get(0);
                String version = val.getString("tag_name", "0").substring(1);
                if(isUpdateVersion(version)){
                    Jval asset = val.get("assets").asArray().find(v -> v.getString("name", "")
                            .startsWith(headless ? "server-release" : "Mindustry"));
                    String url = asset.getString("browser_download_url", "");
                    updateAvailable = true;
                    updateBuild = version;
                    updateUrl = url;
                    Core.app.post(() -> {
                        showUpdateDialog();
                        done.get(true);
                    });
                }else{
                    Core.app.post(() -> done.get(false));
                }
            }else{
                Core.app.post(() -> done.get(false));
            }
        }, t -> {});
    }

    private boolean isUpdateVersion(String str){
        if(build <= 0 || str == null || str.isEmpty()) return true;

        int dot = str.indexOf('.');
        if(dot != -1){
            int major = Strings.parseInt(str.substring(0, dot), 0);
            int minor = Strings.parseInt(str.substring(dot + 1), 0);
            return major > build || (major == build && minor > revision);
        }else{
            return Strings.parseInt(str, 0) > build;
        }
    }

    private void showUpdateDialog(){
        if(!updateAvailable) return;

        if(!headless){
            checkUpdates = false;
            ui.showCustomConfirm(Core.bundle.get("auto-updater.update") + " " + updateBuild, "@auto-updater.confirm", "@ok", "@auto-updater.ignore", () -> {
                try{
                    boolean[] cancel = {false};
                    float[] progress = {0};
                    int[] length = {0};
                    Fi file = buildDirectory.child("Mindustry-" + updateBuild + ".jar");
                    Fi fileDest = Fi.get(Vars.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());

                    BaseDialog dialog = new BaseDialog("@auto-updater.updating");
                    download(updateUrl, file, i -> length[0] = i, v -> progress[0] = v, () -> cancel[0], () -> {
                        try{
                            Runtime.getRuntime().exec(OS.isMac ?
                                    new String[]{"java", "-XstartOnFirstThread", "-DlastBuild=" + Version.build, "-jar", file.absolutePath()} :
                                    new String[]{"java", "-DlastBuild=" + Version.build, "-jar", file.absolutePath()}
                            );
                            file.copyTo(fileDest);
                            Core.app.exit();
                        }catch(IOException e){
                            ui.showException(e);
                        }
                    }, e -> {
                        dialog.hide();
                        ui.showException(e);
                    });

                    dialog.cont.add(new Bar(() -> length[0] == 0 ? Core.bundle.get("auto-updater.updating") : (int)(progress[0] * length[0]) / 1024 / 1024 + "/" + length[0] / 1024 / 1024 + " MB", () -> Pal.accent, () -> progress[0])).width(400f).height(70f);
                    dialog.buttons.button("@cancel", Icon.cancel, () -> {
                        cancel[0] = true;
                        dialog.hide();
                    }).size(210f, 64f);
                    dialog.setFillParent(false);
                    dialog.show();
                }catch(Exception e){
                    ui.showException(e);
                }
            }, () -> checkUpdates = false);
        }else{
            Log.info("&lcA new update is available: &lybuild @", updateBuild);
            if(Administration.Config.autoUpdate.bool()){
                Log.info("&lcAuto-downloading next version...");

                try{
                    Fi source = Fi.get(Vars.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                    Fi dest = source.sibling("server-release.jar");

                    download(updateUrl, dest,
                             len -> Core.app.post(() -> Log.info("&ly| Size: @ MB.", Strings.fixed((float)len / 1024 / 1024, 2))),
                             progress -> {},
                             () -> false,
                             () -> Core.app.post(() -> {
                                 if(!state.isMenu()){
                                     Log.info("&lcSaving...");
                                     SaveIO.save(saveDirectory.child("autosave." + saveExtension));
                                     Log.info("&lcAutosaved.");
                                 }

                                 netServer.kickAll(KickReason.serverRestarting);
                                 Threads.sleep(32);

                                 Log.info("&lcVersion downloaded, exiting. Note that if you are not using a auto-restart script, the server will not restart automatically.");
                                 dest.copyTo(source);
                                 dest.delete();
                                 System.exit(2);
                             }), Log::err);
                }catch(Throwable e){
                    Log.err(e);
                }
            }else{
                Log.info("&lcDo @&lc to enable auto-update.", "config autoUpdate true");
            }
            checkUpdates = false;
        }
    }

    private void download(String furl, Fi dest, Intc length, Floatc progressor, Boolp canceled, Runnable done, Cons<Throwable> error){
        executor.submit(() -> {
            try{
                HttpURLConnection con = (HttpURLConnection)new URL(furl).openConnection();
                BufferedInputStream in = new BufferedInputStream(con.getInputStream());
                OutputStream out = dest.write(false, 4096);

                byte[] data = new byte[4096];
                long size = con.getContentLength();
                long counter = 0;
                length.get((int)size);
                int x;
                while((x = in.read(data, 0, data.length)) >= 0 && !canceled.get()){
                    counter += x;
                    progressor.get((float)counter / (float)size);
                    out.write(data, 0, x);
                }
                out.close();
                in.close();
                if(!canceled.get()){
                    done.run();
                }else{
                    dest.delete();
                }
            }catch(Throwable e){
                error.get(e);
            }
        });
    }
}
