package me.panjohnny.mcdec.commands;

import me.panjohnny.mcdec.Configurator;
import me.panjohnny.mcdec.McDecentralize;
import me.panjohnny.mcdec.server.ServerExecutor;
import me.panjohnny.mcdec.util.TerminalWrapper;
import org.jline.utils.AttributedStyle;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "run", description = "Runs the server based on the configuration.")
public class Run implements Callable<Integer> {
    @CommandLine.Option(names = {"-d", "--dir"}, description = "The directory to sync relative to the current directory.")
    public String dir = ".";


    @Override
    public Integer call() throws Exception {
        TerminalWrapper terminal = TerminalWrapper.getInstance();
        Configurator configurator = Configurator.loadIfPresent(dir);

        terminal.println(McDecentralize.ART);
        terminal.println();

        if (configurator == null) {
            terminal.println("No configuration file found.", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            terminal.flush();
            return 1;
        }

        terminal.println("Running server...");
        terminal.flush();
        if (!ServerExecutor.execute(configurator)) {
            terminal.println("Failed to run the server.", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            terminal.flush();
            return 1;
        } else {
            terminal.println();
            terminal.println("Server has stopped.", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
            terminal.flush();
        }

        return 0;
    }
}
