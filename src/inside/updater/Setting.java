package inside.updater;

import arc.Core;

public enum Setting{

    enable("Enable auto-updating.", false),
    unstable("Allow updating to unstable versions.", false);

    public static final Setting[] all = values();

    public final Object defaultValue;
    public final String description;

    Setting(String description, Object defaultValue){
        this.description = description;
        this.defaultValue = defaultValue;
    }

    public boolean isNum(){
        return defaultValue instanceof Integer;
    }

    public boolean isBool(){
        return defaultValue instanceof Boolean;
    }

    public boolean isString(){
        return defaultValue instanceof String;
    }

    public Object get(){
        return Core.settings.get(name(), defaultValue);
    }

    public boolean bool(){
        return Core.settings.getBool(name(), (Boolean)defaultValue);
    }

    public int num(){
        return Core.settings.getInt(name(), (Integer)defaultValue);
    }

    public String string(){
        return Core.settings.getString(name(), (String)defaultValue);
    }

    public void set(Object value){
        Core.settings.put(name(), value);
    }
}
