package me.panjohnny.mcdec;

import me.panjohnny.mcdec.commands.Init;
import me.panjohnny.mcdec.commands.Run;
import me.panjohnny.mcdec.commands.Share;
import me.panjohnny.mcdec.commands.Sync;
import picocli.CommandLine;

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

    public static void main(String[] args) {
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