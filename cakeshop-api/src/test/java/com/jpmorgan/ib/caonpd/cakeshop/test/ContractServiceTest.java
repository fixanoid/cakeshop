package com.jpmorgan.ib.caonpd.cakeshop.test;

import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Transaction;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.model.Input;
import static com.jpmorgan.ib.caonpd.cakeshop.test.Assert.*;
import static org.testng.Assert.*;

import com.jpmorgan.ib.caonpd.cakeshop.db.BlockScanner;
import com.jpmorgan.ib.caonpd.cakeshop.error.APIException;
import com.jpmorgan.ib.caonpd.cakeshop.model.Contract;
import com.jpmorgan.ib.caonpd.cakeshop.model.ContractABI;
//import com.jpmorgan.ib.caonpd.cakeshop.model.Transaction;
//import com.jpmorgan.ib.caonpd.cakeshop.model.Transaction.Input;
import com.jpmorgan.ib.caonpd.cakeshop.model.TransactionResult;
import com.jpmorgan.ib.caonpd.cakeshop.service.ContractService;
import com.jpmorgan.ib.caonpd.cakeshop.service.ContractService.CodeType;
import com.jpmorgan.ib.caonpd.cakeshop.service.GethHttpService;
import com.jpmorgan.ib.caonpd.cakeshop.service.TransactionService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert.ThrowingRunnable;
import org.testng.annotations.Test;

public class ContractServiceTest extends BaseGethRpcTest {

	@Autowired
	private ContractService contractService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private GethHttpService geth;

	@Autowired
	private BlockScanner blockScanner;

	@Test
	public void testCompile() throws IOException {
	    long time = System.currentTimeMillis() / 1000;
        String code = readTestFile("contracts/simplestorage.sol");

        List<Contract> contracts = contractService.compile(code, CodeType.solidity, true);
        assertNotNull(contracts);
        assertEquals(1, contracts.size());

        Contract c = contracts.get(0);
        //StringUtils.puts(c);

        assertNotNull(c);
        assertNull(c.getAddress()); // only this field should be null
        assertNotEmptyString(c.getABI());
        assertNotEmptyString(c.getBinary());
        assertNotEmptyString(c.getCode());
        assertNotEmptyString(c.getName());
        assertEquals(c.getCodeType(), CodeType.solidity);
        assertNotNull(c.getCreatedDate());
        assertTrue(c.getCreatedDate() >= time);

        assertNotNull(c.getFunctionHashes());
        assertNotNull(c.getGasEstimates());

        Map<String, Object> gasEstimates = c.getGasEstimates();
        List<Long> creation = (List<Long>) gasEstimates.get("creation");
        assertNotNull(creation);
        assertEquals(creation.size(), 2);

        assertNotEmptyString(c.getSolidityInterface());
	}

	@Test
    public void testCreate() throws IOException {
        String code = readTestFile("contracts/simplestorage.sol");

        TransactionResult result = contractService.create(null, code, ContractService.CodeType.solidity, null, null);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertTrue(!result.getId().isEmpty());

    }

	@Test
	public void testCreateWithBinary() throws IOException {
        String code = readTestFile("contracts/simplestorage.sol");
        List<Contract> contracts = contractService.compile(code, CodeType.solidity, true);
        Contract c = contracts.get(0);
        assertNotNull(c);

        TransactionResult result = contractService.create(null, code, CodeType.solidity, null, c.getBinary());
        assertNotNull(result);
        assertNotNull(result.getId());
        assertTrue(!result.getId().isEmpty());
	}

	@Test
	public void testGet() throws IOException, InterruptedException {
		String contractAddress = createContract();

		Contract contract = contractService.get(contractAddress);
		assertNotNull(contract);
		assertNotNull(contract.getBinary(), "Binary code should be present");
		assertNotEquals(contract.getBinary(), "0x", "binary should not be '0x'");
		assertTrue(contract.getBinary().length() > 2);
	}

	@Test
	public void testGetInvalidId() throws APIException {
	    assertThrows(APIException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                contractService.get("0xdeadbeef");
            }
        });

	    assertThrows(APIException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                contractService.get("0x81635fe3d9cecbcf44aa58e967af1ab7ceefb816");
            }
        });
	}

	@Test
	public void testReadByABI() throws InterruptedException, IOException {
	    String contractAddress = createContract();
	    ContractABI abi = ContractABI.fromJson(readTestFile("contracts/simplestorage.abi.txt"));

	    BigInteger val = (BigInteger) contractService.read(contractAddress, abi, null, "get", null, null)[0];
	    assertEquals(val.intValue(), 100);
	}

	@Test
	public void testReadBytesArr() throws InterruptedException, IOException {
	    String addr = createContract(readTestFile("contracts/testbytesarr.sol"), null);
	    ContractABI abi = ContractABI.fromJson(readTestFile("contracts/testbytesarr.abi.txt"));
	    Object[] res = (Object[]) contractService.read(addr, abi, null, "foo", null, null)[0];
	    assertNotNull(res);
	    assertEquals(res.length, 1);
	    assertEquals(new String((byte[]) res[0]).trim(), "foobar");
	}

	@Test
	public void testConstructorArg() throws InterruptedException, IOException {
    	String code = readTestFile("contracts/simplestorage2.sol");

    	// create with constructor val 500
    	String contractAddress = createContract(code, new Object[] { 500 });

	    ContractABI abi = ContractABI.fromJson(readTestFile("contracts/simplestorage2.abi.txt"));

	    BigInteger val = (BigInteger) contractService.read(contractAddress, abi, null, "get", null, null)[0];
	    assertEquals(val.intValue(), 500);


	    String owner = (String) contractService.read(contractAddress, abi, null, "owner", null, null)[0];
	    assertEquals(owner, "0xce731fb824c1dd64d79359e1cfdc5fc742157d3c");
	}

	@Test
	public void testRead2ByABI() throws InterruptedException, IOException {
	    String contractAddress = createContract();

	    String code = readTestFile("contracts/simplestorage.sol");
	    String json = readTestFile("contracts/simplestorage.abi.txt");
	    ContractABI abi = ContractABI.fromJson(json);

	    String addr = "0x81635fe3d9cecbcf44aa58e967af1ab7ceefb817";
	    String str = "foobar47";




	    Object[] res = contractService.read(
	            contractAddress, abi, null,
	            "echo_2",
	            new Object[] { addr, str },
	            null);

	    String hexAddr = (String) res[0];
	    assertEquals(hexAddr, addr);
	    assertEquals(res[1], str);




	    Object[] res2 = contractService.read(
	            contractAddress, abi, null,
	            "echo_contract",
	            new Object[] { contractAddress, "SimpleStorage", json, code, "solidity" },
	            null);

	    assertEquals(res2[0], contractAddress);
	    assertEquals(res2[1], "SimpleStorage");
	    assertEquals(res2[2], json);
	}

	@Test
	public void testTransactByABI() throws InterruptedException, IOException {

	    String contractAddress = createContract();

	    ContractABI abi = ContractABI.fromJson(readTestFile("contracts/simplestorage.abi.txt"));

	    // 100 to start
	    BigInteger val = (BigInteger) contractService.read(contractAddress, abi, null, "get", null, null)[0];
	    assertEquals(val.intValue(), 100);

	    // modify value
	    TransactionResult tr = contractService.transact(contractAddress, abi, null, "set", new Object[]{ 200 });
	    Transaction tx = transactionService.waitForTx(tr, 50, TimeUnit.MILLISECONDS);

	    // should now be 200
	    BigInteger val2 = (BigInteger) contractService.read(contractAddress, abi, null, "get", null, null)[0];
	    assertEquals(val2.intValue(), 200);

	    // read the previous value
	    BigInteger valPrev = (BigInteger) contractService.read(contractAddress, abi, null, "get", null, tx.getBlockNumber().longValue() -1 )[0];
	    assertEquals(valPrev.intValue(), 100);
	}

	@Test
	public void testListTransactions() throws InterruptedException, IOException {

	    String contractAddress = createContract();

	    ContractABI abi = ContractABI.fromJson(readTestFile("contracts/simplestorage.abi.txt"));

	    // 100 to start
	    BigInteger val = (BigInteger) contractService.read(contractAddress, abi, null, "get", null, null)[0];
	    assertEquals(val.intValue(), 100);

	    // modify value
	    TransactionResult tr = contractService.transact(contractAddress, abi, null, "set", new Object[]{ 200 });
	    Transaction tx = transactionService.waitForTx(tr, 50, TimeUnit.MILLISECONDS);

	    ((TestBlockScanner) blockScanner).manualRun();

	    List<Transaction> txns = contractService.listTransactions(contractAddress);

	    System.out.println(txns);

	    assertNotNull(txns);
	    assertTrue(!txns.isEmpty());
	    assertEquals(txns.size(), 2);

	    Transaction txSet = txns.get(0);
	    Input decodedInput = txSet.getDecodedInput();
	    assertNotNull(decodedInput);
	    assertEquals(decodedInput.getMethod(), "set");

	    val = (BigInteger) decodedInput.getArgs()[0];
	    assertEquals(val.intValue(), 200);

	}

}
