package me.panjohnny.mcdec.commands;

import me.panjohnny.mcdec.McDecentralize;
import me.panjohnny.mcdec.config.ConfigKey;
import me.panjohnny.mcdec.config.ConfigKeyRegistry;
import me.panjohnny.mcdec.config.Configurator;
import me.panjohnny.mcdec.server.ServerList;
import me.panjohnny.mcdec.server.ServerManager;
import me.panjohnny.mcdec.sync.SyncProviders;
import me.panjohnny.mcdec.util.TerminalWrapper;
import org.jline.utils.AttributedStyle;
import picocli.CommandLine;

import java.util.Optional;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "config", description = "List, modify and query the config")
public class Config implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "Config option key (name)", arity = "0..1")
    public String key;

    @CommandLine.Parameters(index = "1", description = "Value to set the config option to", arity = "0..1")
    public String value;

    @CommandLine.Option(names = {"-d", "--dir"}, description = "The directory to sync relative to the current directory.")
    public String dir = ".";

    @Override
    public Integer call() throws Exception {
        TerminalWrapper terminal = TerminalWrapper.getInstance();
        terminal.println(McDecentralize.ART);
        terminal.println();
        if (key == null || key.isBlank()) {
            // Print current synchronization providers and server technologies
            terminal.println("Available server technologies", AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE));
            StringBuilder listTech = new StringBuilder();
            for (ServerList list : ServerManager.SERVER_LISTS) {
                listTech.append(list.getName()).append(", ");
            }
            terminal.println(listTech.substring(0, listTech.length() - 2));
            terminal.println();
            terminal.println("Available synchronization providers", AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE));
            StringBuilder providers = new StringBuilder();
            for (String provider : SyncProviders.getProviders()) {
                providers.append(provider).append(", ");
            }
            terminal.println(providers.substring(0, providers.length() - 2));

            ConfigKeyRegistry.printAll();
        } else if (value == null || value.isBlank()) {
            Optional<ConfigKey> configKeyOptional = ConfigKeyRegistry.getByName(key);
            if (configKeyOptional.isEmpty()) {
                terminal.println("Key not found: " + key, AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            } else {
                ConfigKey ck = configKeyOptional.get();
                terminal.print("[" + ck.section().getName() + "] ");
                terminal.println(ck.toString());
                Configurator configurator = Configurator.loadIfPresent(dir);
                if (configurator == null) {
                    terminal.println("No config found for " + dir, AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
                } else {
                    terminal.println("Value: " + configurator.getPropertyOrDefault(ck.key(), ck.defaultValue()));
                }
            }
        } else {
            Optional<ConfigKey> configKeyOptional = ConfigKeyRegistry.getByName(key);
            if (configKeyOptional.isEmpty()) {
                terminal.println("Key not found: " + key, AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            } else {
                Configurator configurator = Configurator.loadIfPresent(dir);
                if (configurator == null) {
                    terminal.println("No config found for " + dir, AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
                } else {
                    String old = configurator.getProperty(key);
                    configurator.setProperty(key, value);
                    configurator.save();
                    terminal.println("Changed from: " + old);
                    terminal.println("To: " + value, AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
                }
            }
        }
        terminal.flush();
        return 0;
    }
}
