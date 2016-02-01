package com.jpmorgan.ib.caonpd.ethereum.enterprise.test.config;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TempFileManager {

    public static List<String> tempFiles = new ArrayList<String>();

    public static String getTempPath() {
        String t = FileUtils.getTempPath();
        tempFiles.add(t);
        return t;
    }

    public static synchronized void cleanupTempPaths() {
        for (String t : tempFiles) {
            File f = new File(t);
            if (f.exists()) {
                try {
                    FileUtils.deleteDirectory(f);
                } catch (IOException e) {
                }
            }
        }
        tempFiles.clear();
    }

}