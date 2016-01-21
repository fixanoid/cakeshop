package com.jpmorgan.ib.caonpd.ethereum.enterprise.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.SystemUtils;

public class FileUtils {

    public static String expandPath(String path, String... rel) {
        return expandPath(Paths.get(path), rel);
    }

    public static String expandPath(File path, String... rel) {
        return expandPath(Paths.get(path.getPath()), rel);
    }

    public static String expandPath(Path basePath, String... rel) {
        StringBuilder relPath = new StringBuilder();
        for (String r : rel) {
           relPath.append(r).append(File.separator);
        }
        try {
            return basePath.resolve(relPath.toString()).toFile().getCanonicalPath();
        } catch (IOException e) {
            return null;
        }
    }

    public static String readClasspathFile(String path) throws IOException {
    	URL url = RpcUtil.class.getClassLoader().getResource(path);
    	File file = org.apache.commons.io.FileUtils.toFile(url);
    	return org.apache.commons.io.FileUtils.readFileToString(file);
    }

    public static Path getClasspathPath(String path) throws IOException {
        URL url = RpcUtil.class.getClassLoader().getResource(path);
        String filePath = url.getPath();
        if (SystemUtils.IS_OS_WINDOWS && filePath.matches("/[A-Z]:.*")) {
            // Fixes weird path handling on Windows
            // Caused by: java.nio.file.InvalidPathException: Illegal char <:> at index 2: /D:/Java/bamboo-agent-home/xml-data/build-dir/ETE-WIN-JOB1/target/test-classes/
            filePath = filePath.substring(1);
        }
        return Paths.get(filePath);
    }

    public static InputStream getClasspathStream(String path) throws IOException {
    	URL url = RpcUtil.class.getClassLoader().getResource(path);
    	return url.openStream();
    }

}
