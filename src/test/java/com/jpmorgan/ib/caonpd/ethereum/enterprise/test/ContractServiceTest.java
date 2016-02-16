package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static com.jpmorgan.ib.caonpd.ethereum.enterprise.test.Assert.*;
import static org.testng.Assert.*;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.ContractABI;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService.CodeType;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

public class ContractServiceTest extends BaseGethRpcTest {

	@Autowired
	ContractService contractService;

	@Autowired
	TransactionService transactionService;

	@Autowired
	GethHttpService geth;

	@Test
	public void testCompile() throws IOException {
	    long time = System.currentTimeMillis() / 1000;
        String code = readTestFile("contracts/simplestorage.sol");

        List<Contract> contracts = contractService.compile(code, CodeType.solidity, true);
        assertNotNull(contracts);
        assertEquals(1, contracts.size());

        Contract c = contracts.get(0);
        //RpcUtil.puts(c);

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

        TransactionResult result = contractService.create(code, ContractService.CodeType.solidity, null, null);
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

        TransactionResult result = contractService.create(code, CodeType.solidity, null, c.getBinary());
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
	}

	@Test
	public void testReadByABI() throws InterruptedException, IOException {
	    String contractAddress = createContract();
	    ContractABI abi = new ContractABI(readTestFile("contracts/simplestorage.abi.txt"));

	    BigInteger val = (BigInteger) contractService.read(contractAddress, abi, "get", null);
	    assertEquals(val.intValue(), 100);
	}

	@Test
	public void testConstructorArg() throws InterruptedException, IOException {
    	String code = readTestFile("contracts/simplestorage2.sol");

    	// create with constructor val 500
    	String contractAddress = createContract(code, new Object[] { 500 });

	    ContractABI abi = new ContractABI(readTestFile("contracts/simplestorage2.abi.txt"));

	    BigInteger val = (BigInteger) contractService.read(contractAddress, abi, "get", null);
	    assertEquals(val.intValue(), 500);


	    String owner = (String) contractService.read(contractAddress, abi, "owner", null);
	    assertEquals(owner, "0x2e219248f44546d966808cdd20cb6c36df6efa82");
	}

	@Test
	public void testRead2ByABI() throws InterruptedException, IOException {
	    String contractAddress = createContract();

	    String code = readTestFile("contracts/simplestorage.sol");
	    String json = readTestFile("contracts/simplestorage.abi.txt");
	    ContractABI abi = new ContractABI(json);

	    String addr = "0x81635fe3d9cecbcf44aa58e967af1ab7ceefb817";
	    String str = "foobar47";




	    Object[] res = (Object[]) contractService.read(
	            contractAddress, abi,
	            "echo_2",
	            new Object[] { addr, str });

	    String hexAddr = (String) res[0];
	    assertEquals(hexAddr, addr);
	    assertEquals(res[1], str);




	    Object[] res2 = (Object[]) contractService.read(
	            contractAddress, abi,
	            "echo_contract",
	            new Object[] { contractAddress, "SimpleStorage", json, code, "solidity" });

	    assertEquals(res2[0], contractAddress);
	    assertEquals(res2[1], "SimpleStorage");
	    assertEquals(res2[2], json);
	}

	@Test
	public void testTransactByABI() throws InterruptedException, IOException {

	    String contractAddress = createContract();

	    ContractABI abi = new ContractABI(readTestFile("contracts/simplestorage.abi.txt"));

	    // 100 to start
	    BigInteger val = (BigInteger) contractService.read(contractAddress, abi, "get", null);
	    assertEquals(val.intValue(), 100);

	    // modify value
	    TransactionResult tr = contractService.transact(contractAddress, abi, "set", new Object[]{ 200 });
	    Transaction tx = transactionService.waitForTx(tr, 50, TimeUnit.MILLISECONDS);

	    // should now be 200
	    BigInteger val2 = (BigInteger) contractService.read(contractAddress, abi, "get", null);
	    assertEquals(val2.intValue(), 200);
	}

}
