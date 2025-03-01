package me.panjohnny.mcdec.p2p;

import me.panjohnny.mcdec.sync.SyncProvider;
import me.panjohnny.mcdec.util.HashUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ServerFileManager {
    private final Path root;
    private final List<Path> files;
    private Properties checksums;
    private Properties oldChecksums;
    public ServerFileManager(Path root) {
        this.root = root;
        this.files = new ArrayList<>();
    }

    public void loadFiles() {
        // Goes thru all of the files in the directory and recursively loads them
        loadFiles(root);
    }

    public List<Path> getFiles() {
        return files;
    }

    private void loadFiles(Path path) {
        List<String> blacklist = Arrays.asList(SyncProvider.EXCLUDE);
        File[] fileArray = path.toFile().listFiles();
        if (fileArray == null) return;

        for (File file : fileArray) {
            if (file.isDirectory()) {
                loadFiles(file.toPath());
            } else {
                Path relativePath = root.relativize(file.toPath());
                String normalizedPath = relativePath.toString().replace('\\', '/');
                String fileName = file.getName();

                boolean blacklisted = blacklist.stream().anyMatch(pattern -> {
                    pattern = pattern.replace('\\', '/');

                    if (!pattern.contains("*")) {
                        return normalizedPath.equals(pattern) || fileName.equals(pattern);
                    }

                    if (pattern.startsWith("*.")) {
                        String suffix = pattern.substring(1);
                        return fileName.endsWith(suffix);
                    }

                    if (pattern.endsWith("/*")) {
                        String dirPrefix = pattern.substring(0, pattern.length() - 1);
                        return normalizedPath.startsWith(dirPrefix) &&
                                !normalizedPath.substring(dirPrefix.length()).contains("/");
                    }

                    if (pattern.startsWith("*") && pattern.endsWith("*") && pattern.length() > 2) {
                        String middle = pattern.substring(1, pattern.length() - 1);
                        return normalizedPath.contains(middle);
                    }

                    if (pattern.startsWith("*") && !pattern.endsWith("*")) {
                        String suffix = pattern.substring(1);
                        return normalizedPath.endsWith(suffix);
                    }

                    if (!pattern.startsWith("*") && pattern.endsWith("*")) {
                        String prefix = pattern.substring(0, pattern.length() - 1);
                        return normalizedPath.startsWith(prefix);
                    }

                    return false;
                });

                if (!blacklisted) {
                    files.add(relativePath);
                }
            }
        }
    }

    public void computeChecksums() {
        computeChecksums("checksums");
    }

    public void computeChecksums(String saveTo) {
        checksums = new Properties();
        for (Path file : files) {
            try {
                if (checksums.containsKey(file.toString())) {
                    continue;
                }
                String checksum = HashUtil.computeChecksum(root.resolve(file));
                checksums.setProperty(file.toString(), checksum);
            } catch (Exception e) {
                throw new RuntimeException("Failed to compute checksum", e);
            }
        }

        File dotDecentralize = root.resolve(".decentralize").toFile();

        File storeFile = new File(dotDecentralize, saveTo);

        if (storeFile.exists()) {
            oldChecksums = new Properties();
            try {
                oldChecksums.load(storeFile.toURI().toURL().openStream());
            } catch (Exception e) {
                throw new RuntimeException("Failed to load old checksums", e);
            }
        }

        try {
            if (!dotDecentralize.exists()) {
                if (!dotDecentralize.mkdirs()) {
                    throw new RuntimeException("Failed to create .decentralize directory");
                }
            }
            checksums.store(new FileWriter(storeFile), "Checksums");
        } catch (Exception e) {
            throw new RuntimeException("Failed to save checksums", e);
        }
    }

    public List<Path> getChangedFiles() {
        List<Path> changedFiles = new ArrayList<>();
        if (oldChecksums == null) {
            return changedFiles;
        }
        for (Path file : files) {
            String checksum = checksums.getProperty(file.toString());
            String oldChecksum = oldChecksums.getProperty(file.toString());
            if (oldChecksum == null || !oldChecksum.equals(checksum)) {
                changedFiles.add(file);
            }
        }
        return changedFiles;
    }

    public Properties getChecksums() {
        return checksums;
    }

    public byte[] serializeChecksums() {
        if (checksums == null) {
            throw new IllegalStateException("Checksums not computed");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            checksums.store(baos, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize checksums", e);
        }
        return baos.toByteArray();
    }

    public void compareWithForeignChecksums(byte[] serialized) {
        Properties foreignChecksums = new Properties();
        try {
            foreignChecksums.load(new ByteArrayInputStream(serialized));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load foreign checksums", e);
        }

        oldChecksums = foreignChecksums;
    }
}
