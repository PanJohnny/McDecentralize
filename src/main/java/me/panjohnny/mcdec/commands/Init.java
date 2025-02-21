package me.panjohnny.mcdec.commands;

import me.panjohnny.mcdec.Configurator;
import me.panjohnny.mcdec.McDecentralize;
import me.panjohnny.mcdec.server.ServerManager;
import me.panjohnny.mcdec.sync.SyncProvider;
import me.panjohnny.mcdec.sync.SyncProviders;
import me.panjohnny.mcdec.util.TerminalWrapper;
import org.jline.utils.AttributedStyle;
import picocli.CommandLine;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@CommandLine.Command(name = "init", description = "Initialize the server. This command is used after downloading decentralize.properties or when the server is not set up yet.")
public class Init implements Callable<Integer> {

    @CommandLine.Option(names = {"-rserver", "--reinstall-server"}, description = "Reinstall the server.")
    public boolean reinstall = false;

    @CommandLine.Option(names = {"-rsync", "--reconfigure-sync"}, description = "Reconfigure the sync provider.")
    public boolean reconfigureSync = false;

    @CommandLine.Option(names = {"-rrun", "--reconfigure-run"}, description = "Reconfigure the server execution.")
    public boolean reconfigureRun = false;

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

        ServerManager serverPicker = new ServerManager(configurator);
        if (!serverPicker.isConfigured()) {
            serverPicker.select();
        } else {
            terminal.println("Server configuration found, skipping step", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
        }

        terminal.println();
        terminal.println("3. Download and setup server", AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE).bold());

        serverPicker.downloadIfNotPresent(() -> {
            String[] commands = new String[]{"java", "-jar", "server.jar", "--nogui", "--initSettings"};
            if ((reconfigureRun) || !configurator.hasProperty("java_path") && terminal.confirm("Do you want to change the java runtime? (default: 'java') (y/n) ")) {
                String javaPath = terminal.ask("Please enter the path to the java runtime: ");
                commands[0] = javaPath;
                configurator.setProperty("java_path", javaPath);
            } else if (configurator.hasProperty("java_path")) {
                commands[0] = configurator.getProperty("java_path");
            }

            terminal.println("Launching server...", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
            terminal.println("This may take a while.", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
            terminal.flush();

            ProcessBuilder processBuilder = new ProcessBuilder(commands);
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

        // Check if mods field is set, if yes ungzip it and write it to mods.txt
        if (configurator.hasProperty("mods")) {
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] decoded = decoder.decode(configurator.getProperty("mods"));

            GZIPInputStream gzip = new GZIPInputStream(new java.io.ByteArrayInputStream(decoded));
            GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(McDecentralize.path("mods.txt")));

            // Redirect the output to the file
            gzip.transferTo(out);
            out.close();
            terminal.println("Mods file loaded from share and decompressed.", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
        }

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
        if (!reconfigureSync && configurator.hasProperty("sync_provider") && configurator.hasProperty("remote_name") && configurator.hasProperty("remote_path")) {
            terminal.println("Sync provider already configured.", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
            String syncProvider = configurator.getProperty("sync_provider");
            String remoteName = configurator.getProperty("remote_name");
            String remotePath = configurator.getProperty("remote_path");

            if (terminal.confirm("Should I synchronize from the remote now? (y/n) ")) {
                SyncProvider provider = SyncProviders.create(syncProvider, remoteName, remotePath);
                provider.syncDown(configurator);
            }
        } else {
            if (reconfigureSync) {
                terminal.println("Reconfiguring sync provider...", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
            }
            var syncProvider = terminal.askOptions("Please select a sync provider: ", SyncProviders.getProviders(), Object::toString);
            // Ask for remote name and path
            terminal.println("Please enter the remote name and path for the sync provider.", AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT));
            String remoteName = terminal.ask("Remote name: ");
            String remotePath = terminal.ask("Remote path: ");
            configurator.setProperty("sync_provider", syncProvider);
            configurator.setProperty("remote_name", remoteName);
            configurator.setProperty("remote_path", remotePath);

            SyncProvider provider = SyncProviders.create(syncProvider, remoteName, remotePath);
            provider.configure(configurator);
            configurator.save();

            // Ask if the user wants to sync from to the remote now
            if (terminal.confirm("Should I synchronize from the remote now? (y/n) ")) {
                provider.syncDown(configurator);
            } else if (terminal.confirm("Should I synchronize to the remote now? (y/n) ")) {
                provider.syncUp(configurator);
            }
        }

        terminal.println();
        terminal.println("7. Server execution", AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE));
        terminal.print("You can use ", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
        terminal.print("mcdec run", AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA).bold());
        terminal.print(" to start the server.", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
        terminal.println();
        if (reconfigureRun) {
            terminal.println("Reconfiguring server execution...", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
        }
        if (!reconfigureRun && configurator.hasProperty("server_command")) {
            terminal.println("The server command is set to " + configurator.getProperty("server_command") + ".", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
        } else if (terminal.ask("Do you want to specify your own server execution command? If not setup wizard will be used. (y/n) ").equals("y")) {
            String command = terminal.ask("Please enter the command: ");
            configurator.setProperty("server_command", command);
        } else {
            if (!reconfigureRun && configurator.hasProperty("java_path")) {
                terminal.print("The java runtime path is set to ", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
            } else if (terminal.confirm("Do you want to change the java runtime? (default: 'java') (y/n) ")) {
                String javaPath = terminal.ask("Please enter the path to the java runtime: ");
                configurator.setProperty("java_path", javaPath);
            }

            if (!reconfigureRun && configurator.hasProperty("java_xmx")) {
                terminal.println("The maximum heap size is set to " + configurator.getProperty("java_xmx") + ".", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
            } else {
                String xmx = terminal.askDefault("Please enter the maximum heap size: ", "4g");
                configurator.setProperty("java_xmx", xmx);
            }

            if (!reconfigureRun && !configurator.hasProperty("java_xms")) {
                terminal.println("The initial heap size is set to " + configurator.getProperty("java_xms") + ".", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
            } else {
                String xms = terminal.askDefault("Please enter the initial heap size: ", "2g");
                configurator.setProperty("java_xms", xms);
            }

            // no-gui
            if (!reconfigureRun && configurator.hasProperty("server_nogui")) {
                terminal.println("Server GUI disabled.", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
            } else if (terminal.confirm("Do you want to disable the server GUI? (y/n) ")) {
                configurator.setProperty("server_nogui", "true");
            }

            // other flags
            if (reconfigureRun || (configurator.hasProperty("server_flags") && terminal.confirm("Do you want to change the server flags? (y/n) ")) || terminal.confirm("Do you want to add any flags to the server run command? (y/n) ")) {
                terminal.println("Please enter the flags separated by spaces.");
                String flags = terminal.ask("Flags: ");
                configurator.setProperty("server_flags", flags);
            } else {
                terminal.println("Server flags set: " + configurator.getProperty("server_flags"), AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
            }
        }

        configurator.save();
        terminal.println("Server execution configured.", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
        terminal.println();

        configurator.save();
        terminal.println();
        terminal.println("Setup complete.", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
        terminal.flush();
        return 0;
    }
}
