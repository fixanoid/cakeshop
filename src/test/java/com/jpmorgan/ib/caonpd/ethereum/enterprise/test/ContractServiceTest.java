package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.testng.Assert.*;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.ContractABI;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.util.RpcUtil;

import java.io.IOException;
import java.math.BigInteger;
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
    public void testCreate() throws IOException {
        String code = readTestFile("contracts/simplestorage.sol");

        TransactionResult result = contractService.create(code, ContractService.CodeType.solidity);
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

	public void testReadByABI() throws InterruptedException, IOException {
	    String contractAddress = createContract();
	    ContractABI abi = new ContractABI(readTestFile("contracts/simplestorage.abi.txt"));

	    BigInteger val = (BigInteger) contractService.read(contractAddress, abi, "get", null);
	    assertEquals(val.intValue(), 100);
	}

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

	    String hexAddr = RpcUtil.addrToHex((BigInteger) res[0]);
	    assertEquals(hexAddr, addr);
	    assertEquals(res[1], str);




	    Object[] res2 = (Object[]) contractService.read(
	            contractAddress, abi,
	            "echo_contract",
	            new Object[] { contractAddress, "SimpleStorage", json, code, "solidity" });

	    assertEquals(RpcUtil.addrToHex((BigInteger) res2[0]), contractAddress);
	    assertEquals(res2[1], "SimpleStorage");
	    assertEquals(res2[2], json);
	}

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
