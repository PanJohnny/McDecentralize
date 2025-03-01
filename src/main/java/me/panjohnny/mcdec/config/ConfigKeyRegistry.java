package me.panjohnny.mcdec.config;

import me.panjohnny.mcdec.util.TerminalWrapper;
import org.jline.utils.AttributedStyle;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ConfigKeyRegistry {
    private static final List<ConfigKey> keys = new LinkedList<>();

    public static void register(ConfigKey key) {
        keys.add(key);
    }

    public static void register(String name, String description, String defaultValue, ConfigSection section) {
        register(new ConfigKey(name, defaultValue, description, section));
    }

    public static void register(String name, String description, String defaultValue, ConfigSection section, boolean required) {
        register(new ConfigKey(name, defaultValue, description, section, required));
    }

    public static void register(String name, String description, ConfigSection section) {
        register(new ConfigKey(name, description, section));
    }

    public static List<ConfigKey> getKeys() {
        return List.copyOf(keys);
    }

    public static void loadDefaults() {
        /*
        SERVER
         */
        register("server", "Server technology", null, ConfigSection.SERVER, true);
        register("server_jar_path", "The path of the server jar", "server.jar", ConfigSection.SERVER, true);
        register("minecraft_version", "The minecraft version", ConfigSection.SERVER);
        register("server_version", "The server loader (technology) version", ConfigSection.SERVER);
        register("server_jar_url", "The URL from where to download the server jar", ConfigSection.SERVER);
        register("server_extension_terminology", "The way that the modifications for this server are called", "mods", ConfigSection.SERVER);
        register("server_extension_folder", "The directory name of the server extension", "mods", ConfigSection.SERVER);

        /*
        SYNC
         */
        register("sync_provider", "The synchronization provider", ConfigSection.SYNC);
        register("remote_name", "The name of the remote", ConfigSection.SYNC);
        register("remote_path", "The path of the remote folder", ConfigSection.SYNC);

        /*
        RCLONE
         */
        register("rclone_flags", "Flags to be added to the rclone command", ConfigSection.RCLONE);
        register("rclone_is_drive", "If rclone remote is google drive, enables setting the folder as a 'Shared with me' folder when sharing the configuration", ConfigSection.RCLONE);
        register("rclone_drive_share_with_me", "If the remote is in the 'Shared with me' folder", ConfigSection.RCLONE);

        /*
        RUN
         */
        register("server_command", "Custom command to be used for running the server. Disables all of the other configuration fields in this section if used", ConfigSection.RUN);
        register("java_path", "The java executable to be used", "java", ConfigSection.RUN);
        register("java_xmx", "JVM maximum heap size", ConfigSection.RUN);
        register("java_xms", "JVM initial heap size", ConfigSection.RUN);
        register("server_nogui", "Disables server GUI (adds --nogui flag)", ConfigSection.RUN);
        register("server_flags", "Flags to be also used when running the server", ConfigSection.RUN);
    }

    public static void printAll() {
        var iterator = keys.iterator();
        TerminalWrapper terminal = TerminalWrapper.getInstance();
        // Section
        ConfigSection section = null;
        while (iterator.hasNext()) {
            var key = iterator.next();
            if (key.section() != section) {
                section = key.section();
                terminal.println();
                terminal.println(section.getName(), AttributedStyle.DEFAULT.background(AttributedStyle.CYAN));
            }
            terminal.println(key.toString());
        }
    }

    public static Optional<ConfigKey> getByName(String key) {
        return keys.stream().filter(l->l.key().equals(key)).findFirst();
    }
}
