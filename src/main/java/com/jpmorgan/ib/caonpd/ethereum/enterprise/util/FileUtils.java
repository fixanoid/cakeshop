package com.jpmorgan.ib.caonpd.ethereum.enterprise.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        System.out.println("getClasspathPath " + path);
        URL url = RpcUtil.class.getClassLoader().getResource(path);
        System.out.println(url);
        return Paths.get(url.getPath());
    }

    public static InputStream getClasspathStream(String path) throws IOException {
    	URL url = RpcUtil.class.getClassLoader().getResource(path);
    	return url.openStream();
    }

}
