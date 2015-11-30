package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.testng.Assert.*;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;

public class ContractServiceTest extends BaseGethRpcTest {

	@Autowired
	private ContractService contractService;

	@Test
	public void testCreate() throws IOException {

			String abi = readTestFile("contracts/simplestorage.abi.txt");
			String code = readTestFile("contracts/simplestorage.sol");

			TransactionResult result = contractService.create(abi, code, ContractService.CodeType.solidity);
			assertNotNull(result);
			assertNotNull(result.getId());
			assertTrue(!result.getId().isEmpty());

	}

}
