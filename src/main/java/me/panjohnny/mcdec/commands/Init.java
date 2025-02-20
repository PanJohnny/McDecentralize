package me.panjohnny.mcdec.commands;

import me.panjohnny.mcdec.Configurator;
import me.panjohnny.mcdec.McDecentralize;
import me.panjohnny.mcdec.server.ServerPicker;
import me.panjohnny.mcdec.sync.SyncProvider;
import me.panjohnny.mcdec.sync.SyncProviders;
import me.panjohnny.mcdec.util.TerminalWrapper;
import org.jline.utils.AttributedStyle;
import picocli.CommandLine;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "init", description = "Initialize the server. This command is used after downloading decentralize.properties or when the server is not set up yet.")
public class Init implements Callable<Integer> {

    @CommandLine.Option(names = {"-r", "--reinstall"}, description = "Reinstall the server.")
    public boolean reinstall = false;

    @Override
    public Integer call() throws Exception {
        var terminal = TerminalWrapper.getInstance();

        terminal.println(McDecentralize.ART);
        terminal.println();

        terminal.println("Welcome to the MCDecentralize setup wizard.", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold());
        if (reinstall) {
            terminal.println("Running with the reinstall flag", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
        }
        terminal.println();

        terminal.println("1. Select project directory", AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE).bold());
        String folder = terminal.askDefault("Enter the path to the project directory: ", ".", ".");

        Configurator configurator;
        if (Objects.equals(folder, ".")) {
            configurator = new Configurator();
        } else {
            configurator = new Configurator(folder);
            McDecentralize.relativePath = folder;
        }

        var optional = configurator.getRejectReason();
        if (optional.isPresent() && configurator.fileExists()) {
            System.err.println(optional.get());
            return 1;
        } else if (configurator.fileExists()) {
            terminal.println("Config file detected", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
            configurator.load();
        } else {
            configurator.save();
        }

        terminal.println();

        terminal.println("2. Select server", AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE).bold());

        ServerPicker serverPicker = new ServerPicker(configurator);
        if (!serverPicker.isConfigured()) {
            serverPicker.select();
        } else {
            terminal.println("Server configuration found, skipping step", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
        }

        terminal.println();
        terminal.println("3. Download and setup server", AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE).bold());

        serverPicker.downloadIfNotPresent(() -> {
            terminal.println("Launching server...", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
            terminal.println("This may take a while.", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
            terminal.flush();

            ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", "server.jar", "--nogui", "--initSettings");
            processBuilder.directory(new File(McDecentralize.relativePath));
            processBuilder.inheritIO();
            try {
                Process process = processBuilder.start();
                process.waitFor();
            } catch (Exception e) {
                terminal.println("Failed to launch server.", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
                throw new RuntimeException(e);
            }
        }, reinstall);

        terminal.clear();
        terminal.println("4. Accept EULA", AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE).bold());

        serverPicker.eula();
        configurator.save();

        terminal.println();
        terminal.println("5. Download mods", AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE));
        terminal.println("Mod download links should be placed inside mods.txt file.", AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT));

        File modsFile = new File(McDecentralize.path("mods.txt"));
        if (!modsFile.exists()) {
            if (!modsFile.createNewFile()) {
                terminal.println("Failed to create mods.txt file.", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            } else {
                terminal.println("mods.txt file created.", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
                terminal.println("Please add mod download links to the file.", AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT));
                terminal.println("After adding the links, run the command again.", AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT));
            }
        } else {
            serverPicker.downloadMods(reinstall);
        }

        terminal.println();
        terminal.println("6. Remote synchronization", AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE));
        terminal.println("Remote synchronization serves as a middle man between your server nodes. It serves as a way to synchronize files between servers.", AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT));
        terminal.println();
        if (configurator.hasProperty("sync_provider") && configurator.hasProperty("remote_name") && configurator.hasProperty("remote_path")) {
            terminal.println("Sync provider already configured.", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
            String syncProvider = configurator.getProperty("sync_provider");
            String remoteName = configurator.getProperty("remote_name");
            String remotePath = configurator.getProperty("remote_path");

            if (terminal.confirm("Should I synchronize from the remote now? (y/n) ")) {
                SyncProvider provider = SyncProviders.create(syncProvider, remoteName, remotePath);
                provider.syncDown();
            }
        } else {
            var syncProvider = terminal.askOptions("Please select a sync provider: ", SyncProviders.getProviders(), Object::toString);
            // Ask for remote name and path
            terminal.println("Please enter the remote name and path for the sync provider.", AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT));
            String remoteName = terminal.ask("Remote name: ");
            String remotePath = terminal.ask("Remote path: ");
            configurator.setProperty("sync_provider", syncProvider);
            configurator.setProperty("remote_name", remoteName);
            configurator.setProperty("remote_path", remotePath);

            SyncProvider provider = SyncProviders.create(syncProvider, remoteName, remotePath);
            configurator.save();

            // Ask if the user wants to sync from to the remote now
            if (terminal.confirm("Should I synchronize from the remote now? (y/n) ")) {
                provider.syncDown();
            } else if (terminal.confirm("Should I synchronize to the remote now? (y/n) ")) {
                provider.syncUp();
            }
        }

        configurator.save();
        terminal.println();
        terminal.println("Setup complete.", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
        terminal.pause();
        return 0;
    }
}
