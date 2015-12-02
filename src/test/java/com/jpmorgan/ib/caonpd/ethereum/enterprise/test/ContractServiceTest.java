package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.testng.Assert.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction.Status;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;

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

		String abi = readTestFile("contracts/simplestorage.abi.txt");
		String code = readTestFile("contracts/simplestorage.sol");

		TransactionResult result = contractService.create(abi, code, ContractService.CodeType.solidity);
		assertNotNull(result);
		assertNotNull(result.getId());
		assertTrue(!result.getId().isEmpty());

		Map<String, Object> res = geth.executeGethCall("miner_start", new Object[]{ });

		Transaction tx = null;
		while (true) {
			tx = transactionService.get(result.getId());
			if (tx.getStatus() == Status.committed) {
				break;
			}
			TimeUnit.MILLISECONDS.sleep(50);
		}

		Contract contract = contractService.get(tx.getContractAddress());
		assertNotNull(contract);
		assertNotNull(contract.getBinary(), "Binary code should be present");
	}

}
