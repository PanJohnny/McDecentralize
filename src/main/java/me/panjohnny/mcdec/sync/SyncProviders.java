package me.panjohnny.mcdec.sync;

import me.panjohnny.mcdec.sync.provider.RCloneSyncProvider;

import java.util.HashMap;
import java.util.List;

public class SyncProviders {
    private static final HashMap<String, SyncProviderFactory> factories = new HashMap<>();

    static {
        factories.put("rclone", RCloneSyncProvider::new);
    }

    public static SyncProvider create(String provider, String remoteName, String remotePath) {
        return factories.get(provider).create(remoteName, remotePath);
    }

    public static List<String> getProviders() {
        return List.copyOf(factories.keySet());
    }

    public static void register(String name, SyncProviderFactory factory) {
        factories.put(name, factory);
    }
}
