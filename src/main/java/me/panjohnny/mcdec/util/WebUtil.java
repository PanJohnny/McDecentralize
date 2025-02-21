package me.panjohnny.mcdec.util;

import me.panjohnny.mcdec.McDecentralize;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class WebUtil {
    public static InputStream get(String url) throws URISyntaxException, IOException {
        var connection = new URI(url).toURL().openConnection();
        connection.setRequestProperty("User-Agent", "MCDecentralize");
        return connection.getInputStream();
    }

    public static boolean downloadIfNotPresent(String url, String path) throws IOException, URISyntaxException {
        if (!Path.of(McDecentralize.relativePath, path).toFile().exists()) {
            download(url, path);
            return true;
        }
        return false;
    }

    public static void download(String url, String path) throws IOException, URISyntaxException {
        try (var inputStream = get(url); var outputStream = new FileOutputStream(McDecentralize.path(path))) {
            inputStream.transferTo(outputStream);
        }
    }

    public static boolean downloadModIfNotPresent(String url) throws IOException, URISyntaxException {
        String[] parts = url.split("/");
        String fileName = parts[parts.length - 1];
        return downloadIfNotPresent(url, "mods/" + fileName);
    }

    public static void downloadMod(String url) throws IOException, URISyntaxException {
        String[] parts = url.split("/");
        String fileName = parts[parts.length - 1];
        download(url, "mods/" + fileName);
    }
}
