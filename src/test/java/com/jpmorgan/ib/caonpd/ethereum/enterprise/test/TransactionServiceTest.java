package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.testng.Assert.*;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.ContractABI;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction.Status;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class TransactionServiceTest extends BaseGethRpcTest {

	@Autowired
	private ContractService contractService;

	@Autowired
	private TransactionService transactionService;

	@Test
	public void testGet() throws IOException {
		String code = readTestFile("contracts/simplestorage.sol");

		TransactionResult result = contractService.create(null, code, ContractService.CodeType.solidity, null, null);
		assertNotNull(result);
		assertNotNull(result.getId());
		assertTrue(!result.getId().isEmpty());

		Transaction tx = transactionService.get(result.getId());
		assertNotNull(tx);
		assertNotNull(tx.getId());
		assertEquals(tx.getId(), result.getId());
		assertEquals(tx.getStatus(), Status.committed);
	}

	@Test
	public void testGetBatch() throws IOException, InterruptedException {

		String code = readTestFile("contracts/simplestorage.sol");
		TransactionResult result = contractService.create(null, code, ContractService.CodeType.solidity, null, null);
		TransactionResult result2 = contractService.create(null, code, ContractService.CodeType.solidity, null, null);

    	List<Transaction> txns = transactionService.get(Lists.newArrayList(result.getId(), result2.getId()));
    	assertNotNull(txns);
    	assertEquals(txns.size(), 2);
    	assertEquals(txns.get(0).getId(), result.getId());
    	assertEquals(txns.get(1).getId(), result2.getId());
	}

	@Test
	public void testGetInvalidHash() throws IOException {
		assertNull(transactionService.get("0xasdf"));
	}

	@Test
	public void testGetPendingTx() throws IOException, InterruptedException {
		String code = readTestFile("contracts/simplestorage.sol");
	    ContractABI abi = new ContractABI(readTestFile("contracts/simplestorage.abi.txt"));

		TransactionResult result = contractService.create(null, code, ContractService.CodeType.solidity, null, null);
		assertNotNull(result);
		assertNotNull(result.getId());

		Transaction createTx = transactionService.waitForTx(result, 20, TimeUnit.MILLISECONDS);

		// stop mining and submit tx
    	Map<String, Object> res = geth.executeGethCall("miner_stop", new Object[]{ });
	    TransactionResult tr = contractService.transact(createTx.getContractAddress(), abi, null, "set", new Object[]{ 200 });

		Transaction tx = transactionService.get(tr.getId());
		assertNotNull(tx);
		assertEquals(tx.getId(), tr.getId());
		assertEquals(tx.getStatus(), Status.pending);
	}

}
