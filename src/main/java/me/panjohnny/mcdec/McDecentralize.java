package me.panjohnny.mcdec;

import me.panjohnny.mcdec.commands.Init;
import me.panjohnny.mcdec.commands.Run;
import me.panjohnny.mcdec.commands.Share;
import me.panjohnny.mcdec.commands.Sync;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "mcdec", mixinStandardHelpOptions = true, version = "MCDecentralize 1.0", subcommands = {Init.class, Sync.class, Run.class, Share.class})
public class McDecentralize implements Callable<Integer> {
    public static final String ART = """
            _  _ ____ ___  ____ ____ ____ _  _ ___ ____ ____ _    _ ___  ____
            |\\/| |    |  \\ |___ |    |___ |\\ |  |  |__/ |__| |    |   /  |___
            |  | |___ |__/ |___ |___ |___ | \\|  |  |  \\ |  | |___ |  /__ |___
            """;

    public static String relativePath = "";

    public static String path(String path) {
        if (!relativePath.isBlank()) {
            return relativePath + "/" + path;
        } else {
            return path;
        }
    }

    public static Path relativizePath(Path path) {
        if (!relativePath.isBlank()) {
            return Path.of(relativePath).relativize(path);
        } else {
            return path;
        }
    }

    @CommandLine.Option(names={"-c", "--config"}, description = "Explicitly specify the configuration file. Default is decentralize.properties.")
    private File configFile = new File("decentralize.properties");

    public static void main(String[] args) {
        int exitCode = new CommandLine(new McDecentralize()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        System.out.println(ART);
        System.out.println();
        System.out.println(configFile.getAbsoluteFile());
        return 0;
    }
}