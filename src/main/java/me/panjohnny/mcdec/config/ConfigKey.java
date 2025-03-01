package me.panjohnny.mcdec.config;

import java.util.Objects;

public record ConfigKey(String key, String defaultValue, String description, ConfigSection section, boolean required) {
    public ConfigKey {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key cannot be null or blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be null or blank");
        }
        if (section == null) {
            section = ConfigSection.OTHER;
        }
    }

    public ConfigKey(String key, String defaultValue, String description, ConfigSection section) {
        this(key, defaultValue, description, section, false);
    }

    public ConfigKey(String key, String description, ConfigSection section) {
        this(key, null, description, section, false);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ConfigKey configKey = (ConfigKey) o;
        return Objects.equals(key, configKey.key) && Objects.equals(description, configKey.description) && Objects.equals(defaultValue, configKey.defaultValue) && section == configKey.section && required == configKey.required();
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, defaultValue, description, section, required);
    }

    public boolean hasDefault() {
        return defaultValue != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(key);
        sb.append(" – ");
        sb.append(description);
        if (hasDefault()) {
            sb.append(" (default value: '");
            sb.append(defaultValue);
            sb.append("') ");
        }
        if (required) {
            sb.append(" >>> required <<<");
        }
        return sb.toString();
    }
}
