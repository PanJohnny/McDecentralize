package me.panjohnny.mcdec.sync;

import me.panjohnny.mcdec.server.ServerList;
import me.panjohnny.mcdec.server.list.FabricServerList;
import me.panjohnny.mcdec.sync.provider.RCloneSyncProvider;

import java.util.List;

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

    public abstract void syncDown();
    public abstract void syncUp();
}
