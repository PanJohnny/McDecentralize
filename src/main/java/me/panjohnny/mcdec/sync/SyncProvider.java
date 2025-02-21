package me.panjohnny.mcdec.sync;

import me.panjohnny.mcdec.Configurator;

import java.util.Optional;
import java.util.Properties;

public abstract class SyncProvider {
    public static final String[] EXCLUDE = new String[] {
            ".fabric/*",
            "libraries/*",
            "mods/*",
            "logs/*",
            "eula.txt",
            "server.jar",
            "*.log",
            "*.jar",
    };

    protected String remoteName;
    protected String remotePath;
    public SyncProvider(String remoteName, String remotePath) {
        this.remoteName = remoteName;
        this.remotePath = remotePath;
    }

    public abstract void syncDown(Configurator configurator);
    public abstract void syncUp(Configurator configurator);

    public abstract void configure(Configurator configurator);

    /**
     * Changes the properties for sharing with others. If the properties are not changed, return an empty optional.
     */
    public abstract Optional<Properties> changeForShared(Properties properties);
}
