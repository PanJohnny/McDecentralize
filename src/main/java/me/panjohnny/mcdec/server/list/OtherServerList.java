package me.panjohnny.mcdec.server.list;

import me.panjohnny.mcdec.server.ServerList;
import me.panjohnny.mcdec.util.TerminalWrapper;

public class OtherServerList extends ServerList {
    private TerminalWrapper terminal;
    @Override
    public void load() {
        terminal = TerminalWrapper.getInstance();
    }

    @Override
    public String getName() {
        return "other";
    }

    @Override
    public ServerVersion[] getMinecraftVersions() {
        return new ServerVersion[0];
    }

    @Override
    public ServerVersion[] getServerVersions(String minecraftVersion) {
        return new ServerVersion[0];
    }

    @Override
    public String getServerJarURL(String minecraftVersion, String serverVersion) {
        return terminal.ask("Enter a server jar url (from where the server will be downloaded), or keep blank to disable automatically downloading server: ");
    }

    @Override
    public String getExtensionTerminology() {
        return terminal.ask("PLURAL: How do you call the extensions for this server type (mods, plugins, ...): ", "mods", "plugins");
    }

    @Override
    public String getExtensionFolder() {
        return terminal.ask("What is the name of the folder where the extensions are saved (mods, plugins, ...): ", "mods", "plugins");
    }
}
