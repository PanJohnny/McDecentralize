package me.panjohnny.mcdec.sync.provider;

import me.panjohnny.mcdec.config.Configurator;
import me.panjohnny.mcdec.McDecentralize;
import me.panjohnny.mcdec.sync.SyncProvider;
import me.panjohnny.mcdec.util.TerminalWrapper;
import org.jline.utils.AttributedStyle;

import java.io.File;
import java.util.Optional;
import java.util.Properties;

public class RCloneSyncProvider extends SyncProvider {
    private final TerminalWrapper terminal = TerminalWrapper.getInstance();

    public RCloneSyncProvider(String remoteName, String remotePath) {
        super(remoteName, remotePath);
    }

    @Override
    public void syncDown(Configurator configurator) {
        terminal.println("Syncing down from RClone...", AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));
        terminal.flush();
        ProcessBuilder processBuilder = new ProcessBuilder("rclone", "sync", remoteName + ":" + remotePath, ".", "--progress");
        addFlags(configurator, processBuilder);
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

    private void addFlags(Configurator configurator, ProcessBuilder processBuilder) {
        for (String s : SyncProvider.EXCLUDE) {
            processBuilder.command().add("--exclude");
            processBuilder.command().add(s);
        }
        if (configurator.hasProperty("rclone_flags")) {
            String[] flags = configurator.getProperty("rclone_flags").split(" ");
            for (String flag : flags) {
                processBuilder.command().add(flag);
            }
        }

        if (configurator.getProperty("rclone_is_drive").equals("true")) {
            if (configurator.getProperty("rclone_drive_shared_with_me").equals("true")) {
                processBuilder.command().add("--drive-shared-with-me");
            }
        }
        processBuilder.inheritIO();
        processBuilder.directory(new File(McDecentralize.relativePath));
        terminal.print("Command: ", AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE));
        terminal.println(String.join(" ", processBuilder.command()), AttributedStyle.DEFAULT);
        terminal.flush();
    }

    @Override
    public void syncUp(Configurator configurator) {
        terminal.println("Syncing up with RClone...", AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));
        terminal.flush();
        ProcessBuilder processBuilder = new ProcessBuilder("rclone", "sync", ".", remoteName + ":" + remotePath, "--progress");
        addFlags(configurator, processBuilder);

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

    @Override
    public void configure(Configurator configurator) {
        // Configure other settings
        TerminalWrapper terminal = TerminalWrapper.getInstance();
        terminal.println("Configuring RClone sync provider...", AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));

        if (!configurator.hasProperty("rclone_is_drive")) {
            if (terminal.confirm("Is the remote a Google Drive remote? (y/n) ")) {
                configurator.setProperty("rclone_is_drive", "true");
            } else {
                configurator.setProperty("rclone_is_drive", "false");
            }
        }

        if (configurator.getProperty("rclone_is_drive").equals("true") && !configurator.hasProperty("rclone_drive_shared_with_me")) {
            if (terminal.confirm("Is the remote in shared with me folder? (y/n) ")) {
                configurator.setProperty("rclone_drive_shared_with_me", "true");
            } else {
                configurator.setProperty("rclone_drive_shared_with_me", "false");
            }
        }

        if (terminal.ask("Do you want to add any flags to the rclone command? DANGER: This will rewrite any if set. (y/n)").equals("y")) {
            terminal.println("Please enter the flags separated by spaces.");
            terminal.print("Current flags: ");
            if (configurator.hasProperty("rclone_flags")) {
                terminal.print(configurator.getProperty("rclone_flags"), AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT));
            } else {
                terminal.print("None", AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT));
            }
            terminal.println();
            String flags = terminal.ask("Flags: ");
            configurator.setProperty("rclone_flags", flags);
        }
        terminal.println("RClone sync provider configured.", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
    }

    @Override
    public Optional<Properties> changeForShared(Properties properties) {
        if (properties.getProperty("rclone_is_drive").equals("true")) {
            Properties newProperties = new Properties();
            newProperties.putAll(properties);
            newProperties.setProperty("rclone_drive_shared_with_me", "true");
            TerminalWrapper.getInstance().println("Changed rclone_drive_shared_with_me to true.", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
            return Optional.of(newProperties);
        }
        return Optional.empty();
    }
}
