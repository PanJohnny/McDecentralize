package me.panjohnny.mcdec.config;

public enum ConfigSection {
    SERVER("Server"),
    SYNC("Sync"),
    RCLONE("Rclone synchronization provider"),
    RUN("Run"),
    OTHER("Other");

    final String name;
    ConfigSection(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
