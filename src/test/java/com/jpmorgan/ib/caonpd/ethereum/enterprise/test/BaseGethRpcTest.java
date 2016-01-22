package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.testng.Assert.*;

import com.google.common.collect.Lists;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.config.TempFileManager;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.config.TestAppConfig;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

@Test
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

    @Value("${geth.genesis}")
    private String genesis;

    @Value("${geth.datadir}")
    private String ethDataDir;

    @Value("${config.path}")
    private String CONFIG_ROOT;

    private static final String GETH_TEMPLATE = TempFileManager.getTempPath();

    public boolean runGeth() {
        return true;
    }

    /**
     * Initialize geth once for re-use across tests since it can be quite slow
     * @throws Exception
     * @throws IOException
     */
    @BeforeSuite
    public void bootstrapGeth() throws Exception {
        springTestContextPrepareTestInstance();

        assertTrue(_startGeth());
        assertTrue(geth.stopGeth());

        // once geth has bootstrapped, copy it to a new location
        try {
            FileUtils.copyDirectory(new File(ethDataDir), new File(GETH_TEMPLATE));
            FileUtils.deleteDirectory(new File(ethDataDir));
        } catch (IOException e) {
            try {
                _stopGeth();
                FileUtils.deleteDirectory(new File(GETH_TEMPLATE));
            } catch (IOException e1) {
            }
            throw new RuntimeException("Failed to copy geth template", e);
        }
    }

    @AfterSuite
    public void cleanupTempPaths() {
        try {
            FileUtils.deleteDirectory(new File(CONFIG_ROOT));
            FileUtils.deleteDirectory(new File(GETH_TEMPLATE));
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
        String gethDir = FileUtils.expandPath(TestAppConfig.APP_ROOT, "geth-resources") + File.separator;
        String genesisFile = FileUtils.expandPath(gethDir, "/genesis/genesis_block.json");

        this.ethDataDir = FileUtils.getTempPath(); // ignore the path configured in properties and use a temp dir
        ethDataDir = ethDataDir.replaceAll(File.separator + File.separator, "/");
        TempFileManager.tempFiles.add(ethDataDir);

        File gethTemplate = new File(GETH_TEMPLATE);
        if (gethTemplate.exists()) {
            // copy from template
            LOG.debug("Bootstrapping GETH from TEMPLATE!");
            FileUtils.copyDirectory(gethTemplate, new File(ethDataDir));

        } else {
            new File(ethDataDir).mkdirs(); // make the dir so we can skip legalese on startup
        }

        return geth.startGeth(gethDir, genesisFile, ethDataDir, Lists.newArrayList("--jpmtest")); // , "--verbosity", "6"
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
        geth.stopGeth();
        try {
            FileUtils.deleteDirectory(new File(ethDataDir));
        } catch (IOException e) {
            logger.warn(e);
        }
    }

    /**
     * Read the given test resource file
     *
     * @param path
     * @return
     * @throws IOException
     */
    protected String readTestFile(String path) throws IOException {
    	URL url = this.getClass().getClassLoader().getResource(path);
    	File file = FileUtils.toFile(url);
    	return FileUtils.readFileToString(file);
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
    	return createContract(code);
    }

    /**
     * Deploy the given contract code to the chain
     *
     * @param code
     * @return
     * @throws APIException
     * @throws InterruptedException
     */
    protected String createContract(String code) throws APIException, InterruptedException {
        TransactionResult result = contractService.create(code, ContractService.CodeType.solidity);
    	assertNotNull(result);
    	assertNotNull(result.getId());
    	assertTrue(!result.getId().isEmpty());

    	// make sure mining is enabled
    	Map<String, Object> res = geth.executeGethCall("miner_start", new Object[]{ });

    	Transaction tx = transactionService.waitForTx(result, 50, TimeUnit.MILLISECONDS);
    	return tx.getContractAddress();
    }

}
