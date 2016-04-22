package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.testng.Assert.*;

import com.google.common.collect.Lists;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.GethConfigBean;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.test.config.TempFileManager;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.test.config.TestAppConfig;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.util.FileUtils;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.util.ProcessUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;

@ContextConfiguration(classes = {TestAppConfig.class})
public abstract class BaseGethRpcTest extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(BaseGethRpcTest.class);

    static {
        System.setProperty("eth.environment", "test");
    }

	@Autowired
	private ContractService contractService;

	@Autowired
	private TransactionService transactionService;

    @Autowired
    protected GethHttpService geth;

    @Value("${geth.datadir}")
    private String ethDataDir;

    @Value("${config.path}")
    private String CONFIG_ROOT;

    @Autowired
    private GethConfigBean gethConfig;

    @Autowired
    @Qualifier("hsql")
    private DataSource embeddedDb;

    public boolean runGeth() {
        return true;
    }

    @AfterSuite
    public void stopSolc() throws IOException {
        List<String> args = Lists.newArrayList(
                gethConfig.getNodePath(),
                gethConfig.getSolcPath(),
                "--stop-ipc");

        ProcessBuilder builder = ProcessUtils.createProcessBuilder(gethConfig, args);
        builder.start();
    }

    @AfterSuite
    public void cleanupTempPaths() {
        try {
            if (CONFIG_ROOT != null) {
                FileUtils.deleteDirectory(new File(CONFIG_ROOT));
            }
            TempFileManager.cleanupTempPaths();
        } catch (IOException e) {
        }
    }

    @BeforeClass
    public void startGeth() throws IOException {
        if (!runGeth()) {
            return;
        }
        LOG.info("Starting Ethereum at test startup");
        assertTrue(_startGeth());
    }

    private boolean _startGeth() throws IOException {
        this.ethDataDir = FileUtils.getTempPath(); // ignore the path configured in properties and use a temp dir
        ethDataDir = ethDataDir.replaceAll(File.separator + File.separator, "/");
        TempFileManager.tempFiles.add(ethDataDir);
        new File(ethDataDir).mkdirs(); // make the dir so we can skip legalese on startup
        gethConfig.setDataDirPath(ethDataDir);

        gethConfig.setGenesisBlockFilename(getTestFile("genesis_block.json").getAbsolutePath());
        gethConfig.setKeystorePath(getTestFile("keystore").getAbsolutePath());

        return geth.start("--jpmtest", "--nokdf"); // , "--verbosity", "6"
    }

    /**
     * Stop geth & delete data dir
     */
    @AfterClass
    public void stopGeth() {
        if (!runGeth()) {
            return;
        }
        LOG.info("Stopping Ethereum at test teardown");
        _stopGeth();
    }

    private void _stopGeth() {
        geth.stop();
        try {
            FileUtils.deleteDirectory(new File(ethDataDir));
        } catch (IOException e) {
            logger.warn(e);
        }
        ((EmbeddedDatabase) embeddedDb).shutdown();
    }

    protected File getTestFile(String path) {
    	URL url = this.getClass().getClassLoader().getResource(path);
    	File file = FileUtils.toFile(url);
    	return file;
    }

    /**
     * Read the given test resource file
     *
     * @param path
     * @return
     * @throws IOException
     */
    protected String readTestFile(String path) throws IOException {
    	return FileUtils.readFileToString(getTestFile(path));
    }

    /**
     * Deploy SimpleStorage sample to the chain and return its address
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    protected String createContract() throws IOException, InterruptedException {
    	String code = readTestFile("contracts/simplestorage.sol");
    	return createContract(code, null);
    }

    /**
     * Deploy the given contract code to the chain
     *
     * @param code
     * @return
     * @throws APIException
     * @throws InterruptedException
     */
    protected String createContract(String code, Object[] args) throws APIException, InterruptedException {
        TransactionResult result = contractService.create(null, code, ContractService.CodeType.solidity, args, null);
    	assertNotNull(result);
    	assertNotNull(result.getId());
    	assertTrue(!result.getId().isEmpty());

    	// make sure mining is enabled
    	Map<String, Object> res = geth.executeGethCall("miner_start", new Object[]{ });

    	Transaction tx = transactionService.waitForTx(result, 50, TimeUnit.MILLISECONDS);
    	return tx.getContractAddress();
    }

}
