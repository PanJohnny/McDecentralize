package me.panjohnny.mcdec.server;

public abstract class ServerList {
    public abstract void load();
    public abstract String getName();
    public abstract ServerVersion[] getMinecraftVersions();
    public abstract ServerVersion[] getServerVersions(String minecraftVersion);
    public abstract String getServerJarURL(String minecraftVersion, String serverVersion);

    /**
     * @return mod, plugin, ...
     */
    public abstract String getExtensionTerminology();

    /**
     * @return mods/, plugins/
     */
    public abstract String getExtensionFolder();

    public record ServerVersion(String version, boolean stable) {

    }
}
