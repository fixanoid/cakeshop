package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.testng.Assert.*;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction.Status;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;

public class TransactionServiceTest extends BaseGethRpcTest {

	@Autowired
	private ContractService contractService;

	@Autowired
	private TransactionService transactionService;

	@Test
	public void testGet() throws IOException {
		String abi = readTestFile("contracts/simplestorage.abi.txt");
		String code = readTestFile("contracts/simplestorage.sol");

		TransactionResult result = contractService.create(abi, code, ContractService.CodeType.solidity);
		assertNotNull(result);
		assertNotNull(result.getId());
		assertTrue(!result.getId().isEmpty());

		Transaction tx = transactionService.get(result.getId());
		assertNotNull(tx);
		assertNotNull(tx.getAddress());
		assertEquals(tx.getAddress(), result.getId());
		assertEquals(tx.getStatus(), Status.pending);
	}

}