package me.panjohnny.mcdec.server;

import me.panjohnny.mcdec.Configurator;
import me.panjohnny.mcdec.McDecentralize;
import me.panjohnny.mcdec.util.TerminalWrapper;
import org.jline.utils.AttributedStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerExecutor {
    public static boolean execute(Configurator configurator) {
        TerminalWrapper terminal = TerminalWrapper.getInstance();

        if (!configurator.hasProperty("server_jar_path")) {
            terminal.println("Missing required configuration properties.", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            return false;
        }

        String javaPath = configurator.getPropertyOrDefault("java_path", "java");
        String serverJarPath = configurator.getPropertyOrDefault("server_jar_path", "server.jar");

        List<String> command = new ArrayList<>();
        if (configurator.hasProperty("server_command")) {
            String customCommand = configurator.getProperty("server_command");
            Collections.addAll(command, customCommand.split(" "));
        } else {
            command.add(javaPath);
            if (configurator.hasProperty("java_xmx")) {
                command.add("-Xmx" + configurator.getProperty("java_xmx"));
            }
            if (configurator.hasProperty("java_xms")) {
                command.add("-Xms" + configurator.getProperty("java_xms"));
            }

            List<String> flags = new ArrayList<>();

            if (configurator.hasProperty("server_nogui") && configurator.getProperty("server_nogui").equals("true")) {
                flags.add("--nogui");
            }
            if (configurator.hasProperty("server_flags")) {
                Collections.addAll(flags, configurator.getProperty("server_flags").split(" "));
            }

            command.add("-jar");
            command.add(serverJarPath);
            command.addAll(flags);
        }

        terminal.print("Command: ", AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE));
        terminal.println(String.join(" ", command), AttributedStyle.DEFAULT);
        terminal.flush();

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(new File(McDecentralize.relativePath));
        processBuilder.inheritIO();

        try {
            Process process = processBuilder.start();
            process.waitFor();
        } catch (Exception e) {
            terminal.println("Failed to start the server.", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            e.printStackTrace(terminal.getWriter());
            return false;
        }

        return true;
    }
}
