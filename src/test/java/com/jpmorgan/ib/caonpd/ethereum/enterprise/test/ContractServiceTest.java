package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.testng.Assert.*;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction.Status;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

public class ContractServiceTest extends BaseGethRpcTest {

	@Autowired
	private ContractService contractService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private GethHttpService geth;

	@Test
    public void testCreate() throws IOException {

        String abi = readTestFile("contracts/simplestorage.abi.txt");
        String code = readTestFile("contracts/simplestorage.sol");

        TransactionResult result = contractService.create(abi, code, ContractService.CodeType.solidity);
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
	    String abi = readTestFile("contracts/simplestorage.abi.txt");

	    BigInteger val = (BigInteger) contractService.read(contractAddress, abi, "get", null);
	    assertEquals(val.intValue(), 100);
	}

	public void testTransactByABI() throws InterruptedException, IOException {

	    String contractAddress = createContract();

	    String abi = readTestFile("contracts/simplestorage.abi.txt");

	    // 100 to start
	    BigInteger val = (BigInteger) contractService.read(contractAddress, abi, "get", null);
	    assertEquals(val.intValue(), 100);

	    // modify value
	    TransactionResult tr = contractService.transact(contractAddress, abi, "set", new Object[]{ 200 });
	    Transaction tx = waitForTx(tr);

	    // should now be 200
	    BigInteger val2 = (BigInteger) contractService.read(contractAddress, abi, "get", null);
	    assertEquals(val2.intValue(), 200);
	}

	private String createContract() throws IOException, InterruptedException {

		String abi = readTestFile("contracts/simplestorage.abi.txt");
		String code = readTestFile("contracts/simplestorage.sol");

		TransactionResult result = contractService.create(abi, code, ContractService.CodeType.solidity);
		assertNotNull(result);
		assertNotNull(result.getId());
		assertTrue(!result.getId().isEmpty());

		Map<String, Object> res = geth.executeGethCall("miner_start", new Object[]{ });

		Transaction tx = waitForTx(result);
		return tx.getContractAddress();
	}

    private Transaction waitForTx(TransactionResult result) throws APIException, InterruptedException {
        Transaction tx = null;
		while (true) {
			tx = transactionService.get(result.getId());
			if (tx.getStatus() == Status.committed) {
				break;
			}
			TimeUnit.MILLISECONDS.sleep(50);
		}
        return tx;
    }

}
