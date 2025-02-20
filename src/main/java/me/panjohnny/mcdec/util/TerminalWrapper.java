package me.panjohnny.mcdec.util;

import org.jline.builtins.Completers;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class TerminalWrapper {
    private static TerminalWrapper instance;
    private final Terminal terminal;
    private final LineReader reader;
    private final PrintWriter writer;
    private final OptionsHolder options;
    private TerminalWrapper() throws IOException {
        this.terminal = TerminalBuilder.terminal();
        this.options = new OptionsHolder();
        this.reader = LineReaderBuilder.builder().terminal(terminal).completer(new StringsCompleter(options)).build();
        this.writer = terminal.writer();
    }

    public static TerminalWrapper getInstance() {
        if (instance == null) {
            try {
                instance = new TerminalWrapper();
            } catch (IOException e) {
                System.err.println("Failed to create a terminal.");
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    public String ask(String question) {
        return reader.readLine(question);
    }

    public <T> T askOptions(String question, Collection<T> options, Function<T, String> mapper) {
        this.options.clear();
        this.options.add(options.stream().map(mapper).toArray(String[]::new));
        var answer = ask(question);
        return options.stream().filter(option -> mapper.apply(option).equals(answer)).findFirst().orElseGet(() -> {
            println("Invalid option. Try again.", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            return askOptions(question, options, mapper);
        });
    }

    public <T> T askOptionsOrDefault(String question, Collection<T> options, Function<T, String> mapper, T defaultValue) {
        this.options.clear();
        this.options.add(options.stream().map(mapper).toArray(String[]::new));
        var answer = ask(question);
        if (answer.isBlank()) {
            return defaultValue;
        }
        return options.stream().filter(option -> mapper.apply(option).equals(answer)).findFirst().orElseGet(() -> {
            println("Invalid option. Try again.", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            return askOptions(question, options, mapper);
        });
    }

    public String askDefault(String question, String defaultValue, String... suggestions) {
        question += " (default \"" + defaultValue + "\") ";
        var answer = ask(question, suggestions);
        if (answer.isBlank()) {
            return defaultValue;
        }
        return answer;
    }

    public String ask(String question, String... suggestions) {
        options.clear();
        options.add(suggestions);
        return reader.readLine(question);
    }


    public boolean confirm(String question) {
        return reader.readLine(question).equalsIgnoreCase("y");
    }

    public void println(String message) {
        writer.println(message);
    }

    public void println() {
        writer.println();
    }

    public void println(String message, AttributedStyle style) {
        writer.println(new AttributedString(message, style).toAnsi());
    }

    public void print(String message, AttributedStyle style) {
        writer.print(new AttributedString(message, style).toAnsi());
    }

    public void clear() {
        terminal.puts(InfoCmp.Capability.clear_screen);
    }

    public void print(String message) {
        writer.print(message);
    }

    public void pause() {
        reader.readLine("Press enter to continue...");
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public LineReader getReader() {
        return reader;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    private static class OptionsHolder implements Supplier<Collection<String>> {
        private final List<String> options;
        public OptionsHolder() {
            this.options = new ArrayList<>();
        }

        @Override
        public Collection<String> get() {
            return options;
        }

        public void add(String... options) {
            this.options.addAll(Arrays.asList(options));
        }

        public void clear() {
            this.options.clear();
        }
    }

    public void flush() {
        writer.flush();
    }
}
