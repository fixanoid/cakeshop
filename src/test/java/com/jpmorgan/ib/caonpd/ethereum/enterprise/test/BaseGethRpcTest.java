package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.testng.Assert.*;

import com.google.common.collect.Lists;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.config.TestAppConfig;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
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

    public boolean runGeth() {
        return true;
    }

    @AfterSuite
    public void cleanupTempConfigPath() {
        try {
            FileUtils.deleteDirectory(new File(CONFIG_ROOT));
        } catch (IOException e) {
        }
    }

    @BeforeClass
    public void startGeth() {
        if (!runGeth()) {
            return;
        }
        LOG.info("Starting Ethereum at test startup");
        String genesisDir = System.getProperty("user.dir") + "/geth-resources/genesis/genesis_block.json";
        String command = System.getProperty("user.dir") + "/geth-resources/";
        String eth_datadir = System.getProperty("user.home") + File.separator + ethDataDir;
        eth_datadir = eth_datadir.replaceAll(File.separator + File.separator, "/");
        new File(eth_datadir).mkdirs();

        Boolean started = geth.startGeth(command, genesisDir, eth_datadir, Lists.newArrayList("--jpmtest")); // , "--verbosity", "6"
        assertTrue(started);
    }

    @AfterClass
    public void stopGeth() {
        if (!runGeth()) {
            return;
        }
        LOG.info("Stopping Ethereum at test teardown");
        geth.stopGeth();
        String eth_datadir = System.getProperty("user.home") + ethDataDir;
        try {
            FileUtils.deleteDirectory(new File(eth_datadir));
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
