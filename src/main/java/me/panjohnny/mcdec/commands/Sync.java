package me.panjohnny.mcdec.commands;

import me.panjohnny.mcdec.Configurator;
import me.panjohnny.mcdec.McDecentralize;
import me.panjohnny.mcdec.sync.SyncProviders;
import me.panjohnny.mcdec.util.TerminalWrapper;
import org.jline.utils.AttributedStyle;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "sync", description = "Sync the server with the remote.")
public class Sync implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "The mode of the sync. Can be 'up' or 'down'.")
    public String mode;

    @CommandLine.Option(names = {"-d", "--dir"}, description = "The directory to sync relative to the current directory.")
    public String dir;


    @Override
    public Integer call() throws Exception {
        var terminal = TerminalWrapper.getInstance();

        terminal.println(McDecentralize.ART);
        terminal.println();

        terminal.println("Syncing server...", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold());

        if (dir != null) {
            McDecentralize.relativePath = dir;
        }

        // Open config
        Configurator configurator;
        if (McDecentralize.relativePath.isBlank()) {
            configurator = new Configurator();
        } else {
            configurator = new Configurator(McDecentralize.relativePath);
        }

        // Check if config file exists
        if (configurator.fileExists()) {
            configurator.load();
            terminal.println("Config file detected.", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
            terminal.flush();
        } else {
            terminal.println("Config file not found.", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            terminal.flush();
            return 1;
        }

        if (configurator.hasProperty("sync_provider") && configurator.hasProperty("remote_name") && configurator.hasProperty("remote_path")) {
            var syncProvider = SyncProviders.create(configurator.getProperty("sync_provider"), configurator.getProperty("remote_name"), configurator.getProperty("remote_path"));
            if (mode.equals("up")) {
                terminal.flush();
                syncProvider.syncUp();
            } else if (mode.equals("down")) {
                terminal.flush();
                syncProvider.syncDown();
            } else {
                terminal.println("Invalid mode.", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
                return 1;
            }
        } else {
            terminal.println("No sync provider selected.", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            terminal.flush();
            return 1;
        }
        return 0;
    }
}
