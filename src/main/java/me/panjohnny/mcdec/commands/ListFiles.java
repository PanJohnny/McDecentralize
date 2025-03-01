package me.panjohnny.mcdec.commands;

import me.panjohnny.mcdec.config.Configurator;
import me.panjohnny.mcdec.McDecentralize;
import me.panjohnny.mcdec.p2p.ServerFileManager;
import me.panjohnny.mcdec.util.TerminalWrapper;
import org.jline.utils.AttributedStyle;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "list", description = "Lists the file in the directory that will be synced.")
public class ListFiles implements Callable<Integer> {
    @CommandLine.Option(names = {"-d", "--dir"}, description = "The directory to sync relative to the current directory.")
    public String dir = ".";

    @Override
    public Integer call() throws Exception {
        Configurator configurator = Configurator.loadIfPresent(dir);
        TerminalWrapper terminal = TerminalWrapper.getInstance();
        if (configurator == null) {
            terminal.println("There is no configurator defined.", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            return 1;
        }

        terminal.println("Files detected: ", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
        ServerFileManager serverFileManager = new ServerFileManager(Path.of(McDecentralize.relativePath));
        serverFileManager.loadFiles();

        serverFileManager.getFiles().forEach(file -> terminal.println(file.toString()));
        terminal.flush();

        terminal.println();
        terminal.println("Files changed from the last run: ", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
        serverFileManager.computeChecksums();
        serverFileManager.getChangedFiles().forEach(file -> terminal.println(file.toString()));
        terminal.flush();
        return 0;
    }
}
