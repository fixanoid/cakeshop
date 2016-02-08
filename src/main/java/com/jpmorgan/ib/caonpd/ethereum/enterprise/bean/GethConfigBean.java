package com.jpmorgan.ib.caonpd.ethereum.enterprise.bean;

import static com.jpmorgan.ib.caonpd.ethereum.enterprise.util.FileUtils.*;
import static com.jpmorgan.ib.caonpd.ethereum.enterprise.util.ProcessUtils.*;

import java.io.File;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GethConfigBean {

    private static final Logger LOG = LoggerFactory.getLogger(GethConfigBean.class);

    public static final String startLinuxCommand = "bin/linux/geth";
    public static final String startWinCommand = "bin/win/geth.exe";
    public static final String startMacCommand = "bin/mac/geth";

    @Value("${app.path}")
    private String APP_ROOT;

    @Value("${config.path}")
    private String CONFIG_ROOT;


    private String baseResourcePath;

    private String binPath;

    private String gethPath;

    private String gethPidFilename;

    private String gethPasswordFile;

    private String solcPath;

    @Value("${geth.datadir}")
    private String dataDirPath;

    @Value("${geth.log}")
    private String logDir;

    @Value("${geth.genesis}")
    private String genesisBlockFilename;


    @Value("${geth.url}")
    private String rpcUrl;

    @Value("${geth.rpcport}")
    private String rpcPort;

    @Value("${geth.rpcapi.list}")
    private String rpcApiList;

    @Value("${geth.node.port:30303}")
    private String gethNodePort;

    @Value("${geth.auto.start:false}")
    private Boolean autoStart;

    @Value("${geth.auto.stop:false}")
    private Boolean autoStop;


    // User-configurable settings

    @Value("${geth.networkid}")
    private Integer networkId;

    @Value("${geth.verbosity:}")
    private Integer verbosity;

    @Value("${geth.mining:}")
    private Boolean mining;

    @Value("${geth.identity:}")
    private String identity;

    public GethConfigBean() {
    }

    @PostConstruct
    private void initBean() {
        baseResourcePath = expandPath(APP_ROOT, "geth-resources") + File.separator;

        if (SystemUtils.IS_OS_WINDOWS) {
            LOG.info("Using geth for windows");
            gethPath = baseResourcePath + startWinCommand;
        } else if (SystemUtils.IS_OS_LINUX) {
            LOG.info("Using geth for linux");
            gethPath = baseResourcePath + startLinuxCommand;
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            LOG.info("Using geth for mac");
            gethPath = baseResourcePath + startMacCommand;
        } else {
            LOG.error("Running on unsupported OS! Only Windows, Linux and Mac OS X are currently supported");
            throw new IllegalArgumentException("Running on unsupported OS! Only Windows, Linux and Mac OS X are currently supported");
        }
        ensureFileIsExecutable(gethPath);
        binPath = new File(gethPath).getParent();

        gethPidFilename = expandPath(CONFIG_ROOT, "meth.pid");

        genesisBlockFilename = expandPath(baseResourcePath, genesisBlockFilename);
        if (SystemUtils.IS_OS_WINDOWS) {
            genesisBlockFilename = genesisBlockFilename.replaceAll(File.separator + File.separator, "/");
            if (genesisBlockFilename.startsWith("/")) {
                // fix filename like /C:/foo/bar/.../genesis_block.json
                genesisBlockFilename = genesisBlockFilename.substring(1);
            }
        }

        String genesisDir = new File(genesisBlockFilename).getParent();
        gethPasswordFile = expandPath(genesisDir, "geth_pass.txt");

        String solcDir = expandPath(genesisDir, "..", "solc", "node_modules", ".bin");
        ensureNodeBins(binPath, solcDir);

        solcPath = solcDir + File.separator + "solc";

        // Clean up data dir path for default config (not an absolute path)
        if (dataDirPath != null && dataDirPath.startsWith("/.")) {
            dataDirPath = expandPath(System.getProperty("user.home"), dataDirPath);
        }

        //RpcUtil.puts(this);
    }

    /**
     * Make sure all node bins are executable, both for win & mac/linux
     * @param nodePath
     * @param solcPath
     */
    private void ensureNodeBins(String nodePath, String solcPath) {
        ensureFileIsExecutable(nodePath + File.separator + "node");
        ensureFileIsExecutable(nodePath + File.separator + "node.exe");
        ensureFileIsExecutable(solcPath + File.separator + "solc");
        ensureFileIsExecutable(solcPath + File.separator + "solc.cmd");
    }

    public String getGethPath() {
        return gethPath;
    }

    public void setGethPath(String gethPath) {
        this.gethPath = gethPath;
    }

    public String getGethPidFilename() {
        return gethPidFilename;
    }

    public void setGethPidFilename(String gethPidFilename) {
        this.gethPidFilename = gethPidFilename;
    }

    public String getDataDirPath() {
        return dataDirPath;
    }

    public void setDataDirPath(String dataDirPath) {
        this.dataDirPath = dataDirPath;
    }

    public String getLogDir() {
        return logDir;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    public String getGenesisBlockFilename() {
        return genesisBlockFilename;
    }

    public void setGenesisBlockFilename(String genesisBlockFilename) {
        this.genesisBlockFilename = genesisBlockFilename;
    }

    public String getRpcUrl() {
        return rpcUrl;
    }

    public void setRpcUrl(String rpcUrl) {
        this.rpcUrl = rpcUrl;
    }

    public String getRpcPort() {
        return rpcPort;
    }

    public void setRpcPort(String rpcPort) {
        this.rpcPort = rpcPort;
    }

    public String getRpcApiList() {
        return rpcApiList;
    }

    public void setRpcApiList(String rpcApiList) {
        this.rpcApiList = rpcApiList;
    }

    public String getGethNodePort() {
        return gethNodePort;
    }

    public void setGethNodePort(String gethNodePort) {
        this.gethNodePort = gethNodePort;
    }

    public Boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(Boolean autoStart) {
        this.autoStart = autoStart;
    }

    public Boolean isAutoStop() {
        return autoStop;
    }

    public void setAutoStop(Boolean autoStop) {
        this.autoStop = autoStop;
    }

    public Integer getNetworkId() {
        return networkId;
    }

    public void setNetworkId(Integer networkId) {
        this.networkId = networkId;
    }

    public Integer getVerbosity() {
        return verbosity;
    }

    public void setVerbosity(Integer verbosity) {
        this.verbosity = verbosity;
    }

    public Boolean isMining() {
        return mining;
    }

    public void setMining(Boolean mining) {
        this.mining = mining;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getBinPath() {
        return binPath;
    }

    public void setBinPath(String binPath) {
        this.binPath = binPath;
    }

    public String getGethPasswordFile() {
        return gethPasswordFile;
    }

    public void setGethPasswordFile(String gethPasswordFile) {
        this.gethPasswordFile = gethPasswordFile;
    }

    public String getSolcPath() {
        return solcPath;
    }

    public void setSolcPath(String solcPath) {
        this.solcPath = solcPath;
    }

}
