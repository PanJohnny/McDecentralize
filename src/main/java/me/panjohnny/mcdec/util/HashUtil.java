package me.panjohnny.mcdec.util;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
    public static String computeChecksum(Path path) throws IOException, NoSuchAlgorithmException {
        byte[] data = Files.readAllBytes(path);
        byte[] hash = MessageDigest.getInstance("MD5").digest(data);
        return new BigInteger(1, hash).toString(16);
    }
}
