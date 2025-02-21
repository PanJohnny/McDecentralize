package me.panjohnny.mcdec.commands;

import me.panjohnny.mcdec.Configurator;
import me.panjohnny.mcdec.McDecentralize;
import me.panjohnny.mcdec.sync.SyncProvider;
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

        Configurator configurator = Configurator.loadIfPresent(dir);

        if (configurator == null) {
            terminal.println("No configuration file found.", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            terminal.flush();
            return 1;
        }

        SyncProvider syncProvider = SyncProviders.createFromConfig(configurator);
        if (syncProvider != null) {
            if (mode.equals("up")) {
                terminal.flush();
                syncProvider.syncUp(configurator);
            } else if (mode.equals("down")) {
                terminal.flush();
                syncProvider.syncDown(configurator);
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
