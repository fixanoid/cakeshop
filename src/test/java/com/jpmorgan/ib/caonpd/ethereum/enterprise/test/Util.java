package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.testng.Assert.*;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

public class Util extends BaseGethRpcTest {

	@Autowired
	ContractService contractService;

	@Autowired
	TransactionService transactionService;

	@Autowired
	GethHttpService geth;

	/**
	 * It turns out that the compiled binary code (using solc) differs from the
	 * binary code as deployed to the chain and retrieved using eth_getCode. This
	 * helper method assists in retrieving the deployed version for use in, e.g.,
	 * the genesis block file.
	 *
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testGetDeployedBinaryForContract() throws IOException, InterruptedException {

	    String code = readTestFile("contracts/ContractRegistry.sol");

		String contractAddress = createContract(code);

		Contract contract = contractService.get(contractAddress);
		assertNotNull(contract);
		assertNotNull(contract.getBinary(), "Binary code should be present");

		System.out.println("BINARY CODE:");
		System.out.println(contract.getBinary());
	}

}
