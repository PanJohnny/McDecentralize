package me.panjohnny.mcdec.sync.provider;

import me.panjohnny.mcdec.McDecentralize;
import me.panjohnny.mcdec.sync.SyncProvider;
import me.panjohnny.mcdec.util.TerminalWrapper;
import org.jline.utils.AttributedStyle;

import java.io.File;

public class RCloneSyncProvider extends SyncProvider {
    private final TerminalWrapper terminal = TerminalWrapper.getInstance();
    public RCloneSyncProvider(String remoteName, String remotePath) {
        super(remoteName, remotePath);
    }

    @Override
    public void syncDown() {
        terminal.println("Syncing down from RClone...", AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));
        terminal.flush();
        ProcessBuilder processBuilder = new ProcessBuilder("rclone", "sync", remoteName + ":" + remotePath, ".");
        for (String s : SyncProvider.EXCLUDE) {
            processBuilder.command().add("--exclude");
            processBuilder.command().add(s);
        }
        processBuilder.inheritIO();
        processBuilder.directory(new File(McDecentralize.relativePath));
        try {
            Process process = processBuilder.start();
            process.waitFor();
            terminal.println();
            terminal.println("Synced down from RClone.", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
        } catch (Exception e) {
            terminal.println("Failed to sync down from RClone.", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            e.printStackTrace(terminal.getWriter());
        }
    }

    @Override
    public void syncUp() {
        terminal.println("Syncing up with RClone...", AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));
        terminal.flush();
        ProcessBuilder processBuilder = new ProcessBuilder("rclone", "sync", ".", remoteName + ":" + remotePath);
        for (String s : SyncProvider.EXCLUDE) {
            processBuilder.command().add("--exclude");
            processBuilder.command().add(s);
        }
        processBuilder.inheritIO();
        processBuilder.directory(new File(McDecentralize.relativePath));
        try {
            Process process = processBuilder.start();
            process.waitFor();
            terminal.println();
            terminal.println("Synced up with RClone.", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
        } catch (Exception e) {
            terminal.println("Failed to sync up with RClone.", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            e.printStackTrace(terminal.getWriter());
        }
    }
}
