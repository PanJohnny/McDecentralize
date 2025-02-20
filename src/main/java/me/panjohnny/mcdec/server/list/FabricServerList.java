package me.panjohnny.mcdec.server.list;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.panjohnny.mcdec.server.ServerList;
import me.panjohnny.mcdec.util.WebUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

public class FabricServerList extends ServerList {
    public static final String API_URL = "https://meta.fabricmc.net/v2/versions";
    private JsonObject versions;

    @Override
    public void load() {
        try (var inputStream = new InputStreamReader(WebUtil.get(API_URL))) {
            versions = JsonParser.parseReader(inputStream).getAsJsonObject();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "fabric";
    }

    @Override
    public ServerVersion[] getMinecraftVersions() {
        JsonArray gameArray = versions.getAsJsonArray("game");
        return getServerVersions(gameArray);
    }

    private ServerVersion[] getServerVersions(JsonArray gameArray) {
        ServerVersion[] gameVersions = new ServerVersion[gameArray.size()];
        for (int i = 0; i < gameArray.size(); i++) {
            JsonObject gameObject = gameArray.get(i).getAsJsonObject();
            String version = gameObject.get("version").getAsString();
            boolean stable = gameObject.get("stable").getAsBoolean();
            gameVersions[i] = new ServerVersion(version, stable);
        }
        return gameVersions;
    }

    @Override
    public ServerVersion[] getServerVersions(String minecraftVersion) {
        JsonArray loaderArray = versions.getAsJsonArray("loader");
        return getServerVersions(loaderArray);
    }

    @Override
    public String getServerJarURL(String minecraftVersion, String serverVersion) {
        // Get the latest installer version
        JsonArray installerArray = versions.getAsJsonArray("installer");
        String installerVersion = installerArray.get(0).getAsJsonObject().get("version").getAsString();

        return "https://meta.fabricmc.net/v2/versions/loader/%s/%s/%s/server/jar".formatted(minecraftVersion, serverVersion, installerVersion);
    }
}