package com.jpmorgan.ib.caonpd.cakeshop.config;

import com.google.common.collect.Lists;
import com.jcabi.manifests.Manifests;
import com.jpmorgan.ib.caonpd.cakeshop.bean.GethConfigBean;
import com.jpmorgan.ib.caonpd.cakeshop.service.GethHttpService;
import com.jpmorgan.ib.caonpd.cakeshop.util.FileUtils;
import com.jpmorgan.ib.caonpd.cakeshop.util.ProcessUtils;
import com.jpmorgan.ib.caonpd.cakeshop.util.StreamGobbler;
import com.jpmorgan.ib.caonpd.cakeshop.util.StringUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Order(999999)
@Service(value="appStartup")
public class AppStartup implements ApplicationListener<ContextRefreshedEvent> {

    class Error {
        public long ts;
        public Object err;
        public Error(Object err) {
            this.ts = System.currentTimeMillis();
            this.err = err;
        }
    }

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AppStartup.class);

    @Value("${config.path}")
    private String CONFIG_ROOT;

    @Autowired
    private GethHttpService geth;

    @Autowired
    private GethConfigBean gethConfig;

    private boolean autoStartFired;

    private boolean healthy;

    private String solcVer;

    private String gethVer;

    private final List<Error> errors;

    public AppStartup() {
        errors = new ArrayList<>();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (autoStartFired) {
            return;
        }
        autoStartFired = true;

        healthy = testSystemHealth();
        if (healthy) {
            printWelcomeMessage();
            if (gethConfig.isAutoStart()) {
                healthy = autoStartGeth();
                if (!healthy) {
                    addError("GETH FAILED TO START");
                }
            }
        }

        if (!healthy) {
            System.out.println(event.getApplicationContext());
            printErrorReport();
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("PRINTING DEBUG INFO");
                LOG.debug("\n" + getDebugInfo(null));
            }
        }

        LOG.info("Cakeshop version: " + Manifests.read("Cakeshop-Version"));
        LOG.info("Cakeshop build id: " + Manifests.read("Cakeshop-Build"));
    }

    private void printErrorReport() {
        System.out.println();
        System.out.println();
        System.out.println(StringUtils.repeat("*", 80));


        System.out.println("PRINTING DEBUG INFO");
        System.out.println("-------------------");
        System.out.println();
        System.out.println(getDebugInfo(null));

        System.out.println();
        System.out.println();
        System.out.println("PRINTING ERROR MESSAGES");
        System.out.println("-----------------------");
        System.out.println();
        System.out.println(getErrorInfo());


        System.out.println();
        System.out.println(StringUtils.repeat("*", 80));
        System.out.println();
        System.out.println();
    }

    private void printWelcomeMessage() {
    }

    public boolean autoStartGeth() {
        LOG.info("Autostarting geth node");
        return geth.start();
    }

    public String getDebugInfo(ServletContext servletContext) {
        StringBuilder out = new StringBuilder();

        out.append("java.vendor: " + SystemUtils.JAVA_VENDOR + "\n");
        out.append("java.version: " + System.getProperty("java.version") + "\n");
        out.append("java.home: " + SystemUtils.JAVA_HOME + "\n");
        out.append("java.io.tmpdir: " + SystemUtils.JAVA_IO_TMPDIR + "\n");
        out.append("\n");

        out.append("servlet.container: ");
        if (servletContext != null) {
           out.append(servletContext.getServerInfo());
        }
        out.append("\n\n");

        out.append("os.name: " + SystemUtils.OS_NAME + "\n");
        out.append("os.version: " + SystemUtils.OS_VERSION + "\n");
        out.append("os.arch: " + SystemUtils.OS_ARCH + "\n");
        out.append("\n");

        out.append(getLinuxInfo());

        out.append("user.dir: " + SystemUtils.getUserDir() + "\n");
        out.append("user.home: " + SystemUtils.getUserHome() + "\n");
        out.append("\n");

        out.append("app.root: " + FileUtils.getClasspathPath("") + "\n");
        out.append("eth.env: " + System.getProperty("eth.environment") + "\n");
        out.append("eth.config.dir: " + CONFIG_ROOT + "\n");
        out.append("\n");

        out.append("geth.path: " + gethConfig.getGethPath() + "\n");
        out.append("geth.data.dir: " + gethConfig.getDataDirPath() + "\n");
        out.append("geth.version: ");
        if (StringUtils.isNotBlank(gethVer)) {
            out.append(gethVer);
        } else {
            out.append("!!! unable to read geth version !!!");
        }
        out.append("\n\n");

        out.append("solc.path: " + gethConfig.getSolcPath() + "\n");
        out.append("solc.version: ");
        if (StringUtils.isNotBlank(solcVer)) {
            out.append(solcVer);
        } else {
            out.append("!!! unable to read solc version !!!");
        }

        return out.toString();
    }

    private String getLinuxInfo() {

        if (!SystemUtils.IS_OS_LINUX) {
            return "";
        }

        // lists all the files ending with -release in the etc folder
        File dir = new File("/etc/");
        List<File> files = new ArrayList<>();

        if (dir.exists()) {
            files.addAll(Lists.newArrayList(dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith("release");
                }
            })));
        }

        // looks for the version file (not all linux distros)
        File fileVersion = new File("/proc/version");
        if (fileVersion.exists()) {
            files.add(fileVersion);
        }

        if (files.isEmpty()) {
            return null;
        }

        StringBuilder str = new StringBuilder();

        str.append("\n\n");
        str.append("Linux release info:");

        // prints all the version-related files
        for (File f : files) {
            try {
                String ver = FileUtils.readFileToString(f);
                if (!StringUtils.isBlank(ver)) {
                    str.append(ver);
                }
            } catch (IOException e) {
            }
        }
        str.append("\n\n");
        return str.toString();
    }

    public String getErrorInfo() {
        if (errors.isEmpty()) {
            return "(no errors logged)";
        }

        FastDateFormat tsFormatter = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss,S");
        StringBuilder out = new StringBuilder();
        for (Error error : errors) {
            out.append("[" + tsFormatter.format(error.ts) + "] ");
            if (error.err instanceof String) {
                out.append(error.err);
            } else if (error.err instanceof Exception) {
                out.append(ExceptionUtils.getStackTrace((Throwable) error.err));
            } else {
                out.append(StringUtils.toString(error.err));
            }
            out.append("\n\n");
        }
        return out.toString();
    }

    /**
     * Checks that all dependencies are functioning correctly
     *
     * @return
     */
    private boolean testSystemHealth() {
        boolean healthy = true;

        System.out.println();
        System.out.println();
        System.out.println(StringUtils.repeat("*", 80));
        System.out.println("Running pre-flight checks...");
        System.out.println();

        // test ethereum data dir
        String dataDir = gethConfig.getDataDirPath();
        System.out.println("Testing ethereum data dir path");
        System.out.println(dataDir);
        if (isDirAccesible(dataDir)) {
            System.out.println("OK");
        } else {
            System.out.println("FAILED");
            healthy = false;
        }

        // test config & db data dir
        System.out.println();
        String dbDir = FileUtils.expandPath(CONFIG_ROOT, "db");
        System.out.println("Testing db path");
        System.out.println(dbDir);
        if (isDirAccesible(dbDir)) {
            System.out.println("OK");
        } else {
            System.out.println("FAILED");
            healthy = false;
        }

        // test geth binary
        System.out.println();
        System.out.println("Testing geth server binary");
        String gethOutput = testBinary(gethConfig.getGethPath(), "version");
        if (gethOutput == null || gethOutput.indexOf("Version:") < 0) {
            healthy = false;
            System.out.println("FAILED");
        } else {
            Matcher matcher = Pattern.compile("^Version: (.*)", Pattern.MULTILINE).matcher(gethOutput);
            if (matcher.find()) {
                gethVer = matcher.group(1);
            }
            System.out.println("OK");
        }

        // test solc binary
        System.out.println();
        System.out.println("Testing solc compiler binary");
        String solcOutput = testBinary(gethConfig.getNodePath(), gethConfig.getSolcPath(), "--version");
        if (solcOutput == null || solcOutput.indexOf("Version:") < 0) {
            healthy = false;
            System.out.println("FAILED");
        } else {
            Matcher matcher = Pattern.compile("^Version: (.*)", Pattern.MULTILINE).matcher(solcOutput);
            if (matcher.find()) {
                solcVer = matcher.group(1);
            }
            System.out.println("OK");
        }

        System.out.println();
        if (healthy) {
            System.out.println("ALL TESTS PASSED!");
        } else {
            System.out.println("!!! SYSTEM FAILED SELF-TEST !!!");
            System.out.println("!!!    NOT STARTING GETH    !!!");
        }

        System.out.println(StringUtils.repeat("*", 80));
        System.out.println();

        return healthy;
    }

    private String testBinary(String... args) {

        ProcessUtils.ensureFileIsExecutable(args[0]);
        if (!new File(args[0]).canExecute()) {
            addError("File is not executable: " + args[0]);
            return null;
        }

        ProcessBuilder builder = ProcessUtils.createProcessBuilder(gethConfig, args);
        try {
            Process proc = builder.start();
            StreamGobbler stdout = StreamGobbler.create(proc.getInputStream());
            StreamGobbler stderr = StreamGobbler.create(proc.getErrorStream());
            proc.waitFor();

            if (proc.exitValue() != 0) {
                addError("Process exited with code " + proc.exitValue() + " while running " + args[0]);
                addError(stdout.getString());
                addError(stderr.getString());
                return null;
            }

            return stdout.getString().trim();

        } catch (IOException | InterruptedException e) {
            LOG.error("Failed to run " + args[0], e);
            addError("Failed to run " + args[0]);
            addError(e);
            return null;
        }
    }

    private boolean isDirAccesible(String path) {
        try {
            File dir = new File(path);
            if (dir.exists()) {
                boolean ok = (dir.canRead() && dir.canWrite());
                if (!ok) {
                    addError("Unable to read/write " + path);
                }
                return ok;
            }

            dir.mkdirs();
            if (!dir.exists()) {
                addError("Unable to create path " + path);
                return false;
            }

            boolean ok = (dir.canRead() && dir.canWrite());
            if (!ok) {
                addError("Unable to read/write " + path);
            }
            return ok;
        } catch (Exception ex) {
            LOG.error("Caught ex while testing path " + path, ex);
            addError(ex);
            return false;
        }
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void addError(Object err) {
        this.errors.add(new Error(err));
    }

    public List<Error> getErrors() {
        return errors;
    }

}
