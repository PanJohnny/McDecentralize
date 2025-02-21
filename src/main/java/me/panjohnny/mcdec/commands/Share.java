package me.panjohnny.mcdec.commands;

import me.panjohnny.mcdec.Configurator;
import me.panjohnny.mcdec.McDecentralize;
import me.panjohnny.mcdec.server.ServerExecutor;
import me.panjohnny.mcdec.sync.SyncProvider;
import me.panjohnny.mcdec.sync.SyncProviders;
import me.panjohnny.mcdec.util.TerminalWrapper;
import org.jline.utils.AttributedStyle;
import picocli.CommandLine;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.zip.GZIPOutputStream;

@CommandLine.Command(name = "share", description = "Share the configuration with your friends.")
public class Share implements Callable<Integer> {
    @CommandLine.Option(names = {"-d", "--dir"}, description = "The directory to sync relative to the current directory.")
    public String dir;


    @Override
    public Integer call() throws Exception {
        TerminalWrapper terminal = TerminalWrapper.getInstance();

        terminal.println(McDecentralize.ART);
        terminal.println();

        Configurator configurator = Configurator.loadIfPresent(dir);
        if (configurator == null) {
            terminal.println("No configuration file found.", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            terminal.flush();
            return 1;
        }

        Properties copy = (Properties) configurator.getProperties().clone();
        terminal.println("Removing run configuration is useful if the server is not running on your machine.");
        if (terminal.confirm("Do you want to remove the run configuration? (y/n) ")) {
            copy.remove("server_command");
            copy.remove("server_flags");
            copy.remove("server_jar_path");
            copy.remove("server_nogui");
            copy.remove("java_path");
            copy.remove("java_xmx");
            copy.remove("java_xms");
        }

        terminal.println();
        // Load the mods file, gzip it and set the mods property to the base64 encoded string
        File modsFile = new File(McDecentralize.path("mods.txt"));
        if (modsFile.exists()) {
            terminal.println("Loading mods file...");
            String mods = Files.readString(modsFile.toPath());

            terminal.println("Compressing mods file...");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(baos);
            gzip.write(mods.getBytes());
            gzip.close();
            var base64 = Base64.getEncoder().encode(baos.toByteArray());
            copy.setProperty("mods", new String(base64));
            terminal.println("Mods file loaded and compressed.", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
        }

        terminal.println();
        terminal.println("Configuring sync provider...");
        SyncProvider provider = SyncProviders.createFromConfig(configurator);
        if (provider != null) {
            Optional<Properties> changed = provider.changeForShared(copy);
            if (changed.isPresent()) {
                copy = changed.get();
                copy.remove("remote_name");
            } else {
                terminal.println("Nothing changed", AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT));
            }
        } else {
            terminal.println("WARNING: No sync provider selected. How would the world be shared?", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
        }

        terminal.println();
        terminal.println("Done.", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
        terminal.flush();

        terminal.println("Configuration:");
        copy.forEach((k, v) -> terminal.println(k + "=" + v));
        terminal.flush();

        if (terminal.confirm("Do you want to open the configuration in a text editor? (y/n) ")) {
            File f = Files.createTempFile("decentralize.share.tmp.", ".properties").toFile();
            OutputStream os = Files.newOutputStream(f.toPath());
            copy.store(os, "Decentralize configuration");
            Desktop.getDesktop().open(f);

            f.deleteOnExit();
        }

        terminal.println();
        terminal.println("How to share:", AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN).bold());
        terminal.println("1. Copy the configuration above.");
        terminal.println("2. Send it to your friend.");
        terminal.println("3. Your friend should create a new directory and create a new file named 'decentralize.properties'.");
        terminal.println("4. Your friend should paste the configuration into the file.");
        terminal.println("5. Your friend should run 'mcdec init' to download the server and mods.");
        terminal.println("  IMPORTANT: Your friend should say that he wants to synchronize down from remote. Please note, that they have to have the same sync provider as you setup on their system.", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
        terminal.println("6. Done! Your friend should now be able to run the server ('mcdec run', or running it manually). Enjoy!");
        terminal.flush();

        Thread.sleep(1000);

        return 0;
    }
}