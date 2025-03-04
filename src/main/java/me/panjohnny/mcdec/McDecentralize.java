package me.panjohnny.mcdec;

import me.panjohnny.mcdec.commands.*;
import me.panjohnny.mcdec.config.ConfigKeyRegistry;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "mcdec", mixinStandardHelpOptions = true, version = "1.2", subcommands = {Init.class, Sync.class, Run.class, Share.class, Config.class})
public class McDecentralize implements Callable<Integer> {
    public static final String ART = """
            _  _ ____ ___  ____ ____ ____ _  _ ___ ____ ____ _    _ ___  ____
            |\\/| |    |  \\ |___ |    |___ |\\ |  |  |__/ |__| |    |   /  |___
            |  | |___ |__/ |___ |___ |___ | \\|  |  |  \\ |  | |___ |  /__ |___
            """;

    public static String relativePath = ".";

    public static String path(String path) {
        if (!relativePath.isBlank()) {
            return relativePath + "/" + path;
        } else {
            return path;
        }
    }

    public static void main(String[] args) {
        ConfigKeyRegistry.loadDefaults();
        int exitCode = new CommandLine(new McDecentralize()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        System.out.println(ART);
        System.out.println();
        new CommandLine(this).usage(System.out);
        return 0;
    }
}