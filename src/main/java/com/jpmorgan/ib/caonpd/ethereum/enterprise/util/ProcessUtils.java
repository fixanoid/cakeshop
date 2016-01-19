package com.jpmorgan.ib.caonpd.ethereum.enterprise.util;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessUtils.class);

    /**
     * Check if the given PID is running (supports both Unix and Windows systems)
     *
     * @param pid
     * @param searchString String to search for in process list (only on Linux/Mac)
     * @return
     */
    public static boolean isProcessRunning(String pid, String searchString) {
        if (SystemUtils.IS_OS_WINDOWS) {
            return isProcessRunningWin(pid);
        } else {
            return isProcessRunningNix(pid, searchString);
        }
    }

    public static boolean isProcessRunningNix(String pid, String searchString) {
        if (StringUtils.isEmpty(pid)) {
            return false;
        }

        try {
            Process proc = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "ps aux | grep " + pid});
            try (InputStream stream = proc.getInputStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(searchString)) {
                        return true;
                    }
                }
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return false;
    }

    public static boolean isProcessRunningWin(String pid) {
        try {
            Process proc = Runtime.getRuntime().exec(new String[]{"cmd", "/c", "tasklist /FI \"PID eq " + pid + "\""});
            try (InputStream stream = proc.getInputStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                //Parsing the input stream.
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(" " + pid + " ")) {
                        return true;
                    }
                }
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return false;
    }

    public static boolean killProcess(String pid, String exeName) throws InterruptedException, IOException {
        if (SystemUtils.IS_OS_WINDOWS) {
            return killProcessWin(exeName);
        }
        return killProcessNix(pid);
    }

    public static boolean killProcessWin(String exeName) throws InterruptedException, IOException {
        Runtime.getRuntime().exec("taskkill /F /IM " + exeName).waitFor();
        return true;
    }

    public static boolean killProcessNix(String pid) throws InterruptedException, IOException {
        Runtime.getRuntime().exec("kill " + pid).waitFor();

        // wait for process to actually stop
        while (true) {
            Process exec = Runtime.getRuntime().exec("kill -0 " + pid);
            exec.waitFor();
            if (exec.exitValue() != 0) {
                break;
            }
            TimeUnit.MILLISECONDS.sleep(5);
        }

        return true;
    }

    public static Integer getProcessPid(Process process) {
        if (SystemUtils.IS_OS_WINDOWS) {
            return getWinPID(process);
        }
        return getUnixPID(process);
    }

    public static Integer getUnixPID(Process process) {
        if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
            try {
                Class<? extends Process> cl = process.getClass();
                Field field = cl.getDeclaredField("pid");
                field.setAccessible(true);
                Object pidObject = field.get(process);
                return (Integer) pidObject;

            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                LOG.error("Cannot get UNIX pid: " + ex.getMessage());
            }
        }

        return null;
    }

    public static Integer getWinPID(Process proc) {
        if (proc.getClass().getName().equals("java.lang.Win32Process")
                || proc.getClass().getName().equals("java.lang.ProcessImpl")) {

            try {
                Field f = proc.getClass().getDeclaredField("handle");
                f.setAccessible(true);
                long handl = f.getLong(proc);
                Kernel32 kernel = Kernel32.INSTANCE;
                WinNT.HANDLE handle = new WinNT.HANDLE();
                handle.setPointer(Pointer.createConstant(handl));
                return kernel.GetProcessId(handle);

            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                LOG.error("Cannot get Windows pid: " + e.getMessage());
            }
        }

        return null;
    }

    public static String readPidFromFile(String pidFilename) {
        File pidFile = new File(pidFilename);
        String pid = null;
        try {
            try (FileReader reader = new FileReader(pidFile); BufferedReader br = new BufferedReader(reader)) {
                String s;
                while ((s = br.readLine()) != null) {
                    pid = s;
                    break;
                }
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return pid;
    }

    public static void writePidToFile(Integer pid, String pidFilename) throws IOException {
        LOG.info("Creating pid file: " + pidFilename);
        File pidFile = new File(pidFilename);
        if (!pidFile.exists()) {
            pidFile.createNewFile();
        }
        try (FileWriter writer = new FileWriter(pidFile)) {
            writer.write(String.valueOf(pid));
            writer.flush();
        }
    }

    /**
     * Ensure that given file, if it exists, is executable
     *
     * @param filename
     */
    public static void ensureFileIsExecutable(String filename) {
        File file = new File(filename);
        if (file.exists() && !file.canExecute()) {
            file.setExecutable(true);
        }
    }


}
