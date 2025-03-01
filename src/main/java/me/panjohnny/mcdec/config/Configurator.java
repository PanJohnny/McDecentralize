package me.panjohnny.mcdec.config;

import me.panjohnny.mcdec.McDecentralize;
import me.panjohnny.mcdec.util.TerminalWrapper;
import org.jline.utils.AttributedStyle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

public class Configurator {
    private String path;
    private Properties properties;
    public Configurator() {
        path = "decentralize.properties";
    }

    public Configurator(String folder) {
        this.path = folder + "/decentralize.properties";

        // Check if folder exists
        File file = new File(folder);
        var terminal = TerminalWrapper.getInstance();
        if (!file.exists()) {
            if (file.mkdirs()) {
                terminal.println("Created directory: " + folder, AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
            } else {
                terminal.println("Failed to create directory: " + folder, AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
                System.exit(1);
            }
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean fileExists() {
        return new File(path).exists();
    }

    public boolean isWritable() {
        return new File(path).canWrite();
    }

    public boolean isReadable() {
        return new File(path).canRead();
    }

    public Optional<String> getRejectReason() {
        if (!fileExists()) {
            return Optional.of("File does not exist.");
        }
        if (!isReadable()) {
            return Optional.of("File is not readable.");
        }
        if (!isWritable()) {
            return Optional.of("File is not writable.");
        }
        return Optional.empty();
    }

    public void save() throws IOException {
        if (!fileExists()) {
            properties = new Properties();
        }

        properties.store(new FileOutputStream(path), "MCDecentralize configuration file");
    }

    public void load() throws IOException {
        properties = new Properties();
        properties.load(new FileInputStream(path));
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public String getProperty(String key) {
        return properties.getProperty(key, "");
    }

    public Properties getProperties() {
        return properties;
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public void removeProperty(String key) {
        properties.remove(key);
    }

    public String getPropertyOrDefault(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static Configurator loadIfPresent(String dir) throws IOException {
        TerminalWrapper terminal = TerminalWrapper.getInstance();
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
            return configurator;
        } else {
            terminal.println("Config file not found.", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            terminal.flush();
            return null;
        }
    }
}
