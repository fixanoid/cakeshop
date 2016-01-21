package com.jpmorgan.ib.caonpd.ethereum.enterprise.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.SystemUtils;

public class FileUtils extends org.apache.commons.io.FileUtils {

    /**
     * Join the given paths and expand any relative locations (. or ..) to their full canonical form
     *
     * @param path
     * @param rel
     * @return
     */
    public static String expandPath(String path, String... rel) {
        return expandPath(Paths.get(path), rel);
    }

    /**
     * Join the given paths and expand any relative locations (. or ..) to their full canonical form
     *
     * @param path
     * @param rel
     * @return
     */
    public static String expandPath(File path, String... rel) {
        return expandPath(Paths.get(path.getPath()), rel);
    }

    /**
     * Join the given paths and expand any relative locations (. or ..) to their full canonical form
     *
     * @param basePath
     * @param rel
     * @return
     */
    public static String expandPath(Path basePath, String... rel) {
        StringBuilder relPathBuilder = new StringBuilder();
        for (String r : rel) {
           relPathBuilder.append(r).append(File.separator);
        }
        String relPath = relPathBuilder.toString();
        if (relPath.startsWith("/")) {
            relPath = relPath.substring(1);
        }
        try {
            return basePath.resolve(relPath).toFile().getCanonicalPath();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Read in the given classpath resource as a string
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static String readClasspathFile(String path) throws IOException {
    	URL url = RpcUtil.class.getClassLoader().getResource(path);
    	File file = org.apache.commons.io.FileUtils.toFile(url);
    	return org.apache.commons.io.FileUtils.readFileToString(file);
    }

    /**
     * Get a {@link Path} to the given classpath resource
     * @param path
     * @return
     * @throws IOException
     */
    public static Path getClasspathPath(String path) {
    	if (SystemUtils.IS_OS_WINDOWS) {
    		// flip slashes so it doesn't get escaped in the resulting URL
    		// like \test%5cenv.properties
    		path = path.replace('\\', '/');
    	}
        URL url = RpcUtil.class.getClassLoader().getResource(path);
        if (url == null) {
            return null;
        }
        String filePath = url.getPath();
        if (SystemUtils.IS_OS_WINDOWS && filePath.matches("/[A-Z]:.*")) {
            // Fixes weird path handling on Windows
            // Caused by: java.nio.file.InvalidPathException: Illegal char <:> at index 2: /D:/Java/bamboo-agent-home/xml-data/build-dir/ETE-WIN-JOB1/target/test-classes/
            filePath = filePath.substring(1);
        }
        return Paths.get(filePath);
    }

    /**
     * Get an InputStream for the given classpath resource
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static InputStream getClasspathStream(String path) throws IOException {
    	URL url = RpcUtil.class.getClassLoader().getResource(path);
    	return url.openStream();
    }

    /**
     * Create a temporary filename (for use as a file or directory)
     *
     * @return
     */
    public static String getTempPath() {
        return getTempPath("ee-");
    }

    /**
     * Create a temporary filename with the given prefix (for use as a file or directory)
     *
     * @param prefix
     * @return
     */
    public static String getTempPath(String prefix) {
        try {
            File temp = File.createTempFile("ee-", "");
            temp.delete();
            return temp.getPath();
        } catch (IOException e) {
        }
        return null;
    }

}
