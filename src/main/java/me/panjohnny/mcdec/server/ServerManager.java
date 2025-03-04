package me.panjohnny.mcdec.server;

import me.panjohnny.mcdec.config.Configurator;
import me.panjohnny.mcdec.McDecentralize;
import me.panjohnny.mcdec.server.list.OtherServerList;
import me.panjohnny.mcdec.server.list.FabricServerList;
import me.panjohnny.mcdec.util.TerminalWrapper;
import me.panjohnny.mcdec.util.WebUtil;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ServerManager {
    public static List<ServerList> SERVER_LISTS = List.of(
            new FabricServerList(),
            new OtherServerList()
    );
    private final Configurator config;
    private final TerminalWrapper terminal;

    public ServerManager(Configurator config) throws IOException {
        this.config = config;
        this.terminal = TerminalWrapper.getInstance();
    }

    public boolean isServerSelected() {
        return config.getProperties().containsKey("server");
    }

    public void select() {
        ServerList serverList = terminal.askOptions("Please select a server technology: ", SERVER_LISTS, ServerList::getName);
        config.setProperty("server", serverList.getName());

        terminal.println("Server technology selected: " + serverList.getName(), AttributedStyle.DEFAULT);
        serverList.load();

        ServerList.ServerVersion[] mcVersions = serverList.getMinecraftVersions();
        if (mcVersions.length == 0) {
            var serverJarURL = serverList.getServerJarURL(null, null);
            if (!serverJarURL.isBlank()) {
                config.setProperty("server_jar_url", serverJarURL);
            }
        } else {
            ServerList.ServerVersion minecraftVersion = terminal.askOptions("Please select a minecraft version: ", List.of(mcVersions), ServerList.ServerVersion::version);

            ServerList.ServerVersion[] serverVersions = serverList.getServerVersions(minecraftVersion.version());
            ServerList.ServerVersion serverVersion = terminal.askOptionsOrDefault("Please select a server version, leave blank for LTS stable: ", List.of(serverVersions), ServerList.ServerVersion::version, serverVersions[0]);

            var serverJarURL = serverList.getServerJarURL(minecraftVersion.version(), serverVersion.version());

            config.setProperty("minecraft_version", minecraftVersion.version());
            config.setProperty("server_version", serverVersion.version());
            config.setProperty("server_jar_url", serverJarURL);
        }

        config.setProperty("server_jar_path", askJarName());
        config.setProperty("server_extension_terminology", serverList.getExtensionTerminology());
        config.setProperty("server_extension_folder", serverList.getExtensionFolder());

        try {
            config.save();
        } catch (IOException e) {
            terminal.println("Failed to save configuration.", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            throw new RuntimeException(e);
        }
    }

    private String askJarName() {
        var res = terminal.askDefault("What should be the name of the server jar? (default server.jar): ", "server.jar");
        if (!res.endsWith(".jar")) {
            terminal.println("The jar file must end with .jar", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            askJarName();
        } else if (res.contains("\\") || res.contains("/")) {
            terminal.println("The jar file must not be placed in other folder", AttributedStyle.DEFAULT);
            return askJarName();
        }

        return res;
    }

    private void print(String message, AttributedStyle style) {
        terminal.println(new AttributedString(message, style).toAnsi());
    }

    public boolean isConfigured() {
        return config.getProperties().containsKey("server")
                && config.getProperties().containsKey("server_jar_path");
    }

    public void downloadIfNotPresent(Runnable configure, boolean reinstall) throws IOException, URISyntaxException {
        terminal.println("Downloading server jar...", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));

        var serverJarURL = config.getProperty("server_jar_url");
        var serverJarPath = config.getProperty("server_jar_path");

        if (serverJarPath == null || serverJarURL.isBlank()) {
            terminal.println("Downloading nothing as this configuration does not provide server jar url");
            return;
        }

        if (WebUtil.downloadIfNotPresent(serverJarURL, serverJarPath)) {
            terminal.println("Server jar downloaded.", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
            configure.run();
        } else {
            terminal.println("Server jar already present.");
            if (reinstall) {
                terminal.println("Reinstalling server jar...", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
                WebUtil.download(serverJarURL, serverJarPath);
                terminal.println("Server jar reinstalled.", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
                configure.run();
            }
        }
    }

    public void eula() {
        terminal.println("Checking EULA...", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
        terminal.println();
        Path path = Path.of(McDecentralize.relativePath, "eula.txt");

        if (config.getProperty("eula").equals("true")) {
            try {
                Files.writeString(path, "eula=true");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            terminal.println("EULA already accepted.");
            return;
        }

        terminal.println("By continuing, you agree to the Minecraft EULA.", AttributedStyle.BOLD.foreground(AttributedStyle.YELLOW));
        terminal.println("https://aka.ms/MinecraftEULA");
        if (!terminal.confirm("Do you agree to the EULA? (y/n) ")) {
            terminal.println("EULA rejected.", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
        } else {
            terminal.println("EULA accepted.", AttributedStyle.DEFAULT);
            // Write to eula.txt
            try {
                Files.writeString(path, "eula=true");
                config.setProperty("eula", "true");
            } catch (IOException e) {
                terminal.println("Failed to write to eula.txt.", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
                e.printStackTrace(terminal.getWriter());
            }
        }
    }

    public void downloadMods(boolean reinstall) {
        try {
            if (!reinstall && !terminal.confirm("Do you want to download %s? (y/n) ".formatted(config.getPropertyOrDefault("server_extension_terminology", "mods")))) {
                return;
            }
            terminal.println("Downloading %s...".formatted(config.getPropertyOrDefault("server_extension_terminology", "mods")), AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));

            String mods = Files.readString(Path.of(McDecentralize.relativePath, "mods.txt"));
            String[] modLinks = mods.split("\n");
            for (String modLink : modLinks) {
                if (modLink.isBlank()) {
                    continue;
                }
                terminal.print(" > MOD DL: ", AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE));
                terminal.print(modLink);
                if (WebUtil.downloadModIfNotPresent(modLink)) {
                    terminal.print(" - Done");
                } else {
                    terminal.print(" - Already present");
                }
                terminal.println();
            }
            terminal.println("%s downloaded.".formatted(config.getPropertyOrDefault("server_extension_terminology", "Mods")), AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
        } catch (IOException | URISyntaxException e) {
            terminal.println("Failed to download %s.".formatted(config.getPropertyOrDefault("server_extension_terminology", "mods")), AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            e.printStackTrace(terminal.getWriter());
        }

    }
}