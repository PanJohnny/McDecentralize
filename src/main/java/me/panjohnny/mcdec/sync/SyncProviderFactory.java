package me.panjohnny.mcdec.sync;

public interface SyncProviderFactory {
    SyncProvider create(String remoteName, String remotePath);
}
