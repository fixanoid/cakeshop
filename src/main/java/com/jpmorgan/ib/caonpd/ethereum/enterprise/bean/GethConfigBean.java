package com.jpmorgan.ib.caonpd.ethereum.enterprise.bean;

import static com.jpmorgan.ib.caonpd.ethereum.enterprise.util.FileUtils.*;
import static com.jpmorgan.ib.caonpd.ethereum.enterprise.util.ProcessUtils.*;
import static org.apache.commons.io.FileUtils.*;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.config.AppConfig;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
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

    private String configFile;

    private String baseResourcePath;

    private String binPath;

    private String gethPath;

    private String gethPidFilename;

    private String gethPasswordFile;

    private String genesisBlockFilename;

    private String keystorePath;

    private String solcPath;

    private Properties props;

    private static final String DEFAULT_NODE_PORT = "30303";

    private final String GETH_DATA_DIR = "geth.datadir";
    private final String GETH_LOG_DIR = "geth.log";
    private final String GETH_RPC_URL = "geth.url";
    private final String GETH_RPC_PORT = "geth.rpcport";
    private final String GETH_RPCAPI_LIST = "geth.rpcapi.list";
    private final String GETH_NODE_PORT = "geth.node.port";
    private final String GETH_AUTO_START = "geth.auto.start";
    private final String GETH_AUTO_STOP = "geth.auto.stop";

    // User-configurable settings
    private final String GETH_NETWORK_ID = "geth.networkid";
    private final String GETH_VERBOSITY = "geth.verbosity";
    private final String GETH_MINING = "geth.mining";
    private final String GETH_IDENTITY = "geth.identity";
    private final String GETH_EXTRA_PARAMS = "geth.params.extra";

    public GethConfigBean() {
    }

    /**
     * Reset back to vendored config file and re-init bean config
     *
     * @throws IOException
     */
    public void initFromVendorConfig() throws IOException {
        final String ENV = System.getProperty("eth.environment");
        File vendorEnvConfigFile = FileUtils.getClasspathPath(ENV + File.separator + AppConfig.CONFIG_FILE).toFile();
        LOG.info("Initializing new config from " + vendorEnvConfigFile.getPath());
        FileUtils.copyFile(vendorEnvConfigFile, new File(configFile));
        initBean();
    }

    @PostConstruct
    private void initBean() throws IOException {

        // load props
        configFile = FileUtils.expandPath(CONFIG_ROOT, "env.properties");
        props = new Properties();
        props.load(new FileInputStream(configFile));



        // setup needed paths

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


        // init genesis block file (using vendor copy if necessary)
        String vendorGenesisDir = expandPath(baseResourcePath, "/genesis");
        String vendorGenesisBlockFile = expandPath(vendorGenesisDir, "genesis_block.json");

        genesisBlockFilename = expandPath(CONFIG_ROOT, "genesis_block.json");
        if (!new File(genesisBlockFilename).exists()) {
            copyFile(new File(vendorGenesisBlockFile), new File(genesisBlockFilename));
        }

        if (SystemUtils.IS_OS_WINDOWS) {
            genesisBlockFilename = genesisBlockFilename.replaceAll(File.separator + File.separator, "/");
            if (genesisBlockFilename.startsWith("/")) {
                // fix filename like /C:/foo/bar/.../genesis_block.json
                genesisBlockFilename = genesisBlockFilename.substring(1);
            }
        }

        // set password file
        gethPasswordFile = expandPath(vendorGenesisDir, "geth_pass.txt");

        // set keystore path
        keystorePath = expandPath(vendorGenesisDir, "keystore");

        // configure solc
        String solcDir = expandPath(vendorGenesisDir, "..", "solc", "node_modules", ".bin");
        ensureNodeBins(binPath, solcDir);
        solcPath = solcDir + File.separator + "solc";

        // Clean up data dir path for default config (not an absolute path)
        if (getDataDirPath() != null && getDataDirPath().startsWith("/.")) {
            setDataDirPath(expandPath(System.getProperty("user.home"), getDataDirPath()));
        }

        String identity = getIdentity();
        if (StringUtils.isBlank(identity)) {
            identity = System.getenv("USER");
            if (StringUtils.isBlank(identity)) {
                identity = System.getenv("USERNAME");
            }
        }
        setIdentity(identity);

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
        return props.getProperty(GETH_DATA_DIR);
    }

    public void setDataDirPath(String dataDirPath) {
        props.setProperty(GETH_DATA_DIR, dataDirPath);
    }

    public String getLogDir() {
        return props.getProperty(GETH_LOG_DIR);
    }

    public void setLogDir(String logDir) {
        props.setProperty(GETH_LOG_DIR, logDir);
    }

    public String getGenesisBlockFilename() {
        return genesisBlockFilename;
    }

    public void setGenesisBlockFilename(String genesisBlockFilename) {
        this.genesisBlockFilename = genesisBlockFilename;
    }

    public String getRpcUrl() {
        return props.getProperty(GETH_RPC_URL);
    }

    public void setRpcUrl(String rpcUrl) {
        props.setProperty(GETH_RPC_URL, rpcUrl);
    }

    public String getRpcPort() {
        return props.getProperty(GETH_RPC_PORT);
    }

    public void setRpcPort(String rpcPort) {
        props.setProperty(GETH_RPC_PORT, rpcPort);
    }

    public String getRpcApiList() {
        return props.getProperty(GETH_RPCAPI_LIST);
    }

    public void setRpcApiList(String rpcApiList) {
        props.setProperty(GETH_RPCAPI_LIST, rpcApiList);
    }

    public String getGethNodePort() {
        return props.getProperty(GETH_NODE_PORT, DEFAULT_NODE_PORT);
    }

    public void setGethNodePort(String gethNodePort) {
        props.setProperty(GETH_NODE_PORT, gethNodePort);
    }

    public Boolean isAutoStart() {
        return Boolean.valueOf(get(GETH_AUTO_START, "false"));
    }

    public void setAutoStart(Boolean autoStart) {
        props.setProperty(GETH_AUTO_START, autoStart.toString());
    }

    public Boolean isAutoStop() {
        return Boolean.valueOf(get(GETH_AUTO_STOP, "false"));
    }

    public void setAutoStop(Boolean autoStop) {
        props.setProperty(GETH_AUTO_STOP, autoStop.toString());
    }

    public Integer getNetworkId() {
        return Integer.valueOf(get(GETH_NETWORK_ID, "1006"));
    }

    public void setNetworkId(Integer networkId) {
        props.setProperty(GETH_NETWORK_ID, networkId.toString());
    }

    public Integer getVerbosity() {
        return Integer.valueOf(get(GETH_VERBOSITY, "3"));
    }

    public void setVerbosity(Integer verbosity) {
        props.setProperty(GETH_VERBOSITY, verbosity.toString());
    }

    public Boolean isMining() {
        return Boolean.valueOf(get(GETH_MINING, "false"));
    }

    public void setMining(Boolean mining) {
        props.setProperty(GETH_MINING, mining.toString());
    }

    public String getIdentity() {
        return props.getProperty(GETH_IDENTITY);
    }

    public void setIdentity(String identity) {
        props.setProperty(GETH_IDENTITY, identity);
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

    public String getKeystorePath() {
        return keystorePath;
    }

    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    public String getSolcPath() {
        return solcPath;
    }

    public void setSolcPath(String solcPath) {
        this.solcPath = solcPath;
    }

    public String getExtraParams() {
        return props.getProperty(GETH_EXTRA_PARAMS);
    }

    public void setExtraParams(String extraParams) {
        props.setProperty(GETH_EXTRA_PARAMS, extraParams);
    }

    public String getGenesisBlock() throws IOException {
        return FileUtils.readFileToString(new File(genesisBlockFilename));
    }

    public void setGenesisBlock(String genesisBlock) throws IOException {
        FileUtils.writeStringToFile(new File(genesisBlockFilename), genesisBlock);
    }

    /**
     * Write the underlying config file to disk (persist all properties)
     * @throws IOException
     */
    public void save() throws IOException {
        props.store(new FileOutputStream(configFile), null);
    }

    /**
     * Write a property directly to the underlying property store
     *
     * @param key
     * @param val
     */
    public void setProperty(String key, String val) {
        props.setProperty(key, val);
    }

    /**
     * Simple wrapper around {@link Properties#getProperty(String)} which handles empty strings
     * and nulls properly
     *
     * @param key
     * @param defaultStr
     * @return
     */
    private String get(String key, String defaultStr) {
        return StringUtils.defaultIfBlank(props.getProperty(key), defaultStr);
    }

}
