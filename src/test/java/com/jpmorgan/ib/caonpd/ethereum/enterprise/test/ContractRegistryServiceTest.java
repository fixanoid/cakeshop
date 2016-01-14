package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.testng.Assert.*;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractRegistryService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService.CodeType;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

public class ContractRegistryServiceTest extends BaseGethRpcTest {

    @Autowired
    private ContractRegistryService contractRegistry;

    @Autowired
    private ContractService contractService;

	@Autowired
	private TransactionService transactionService;

	@Test
	public void testRegisterAndGet() throws IOException, InterruptedException {

	    String addr = createContract();
	    String abi = readTestFile("contracts/simplestorage.abi.txt");
	    String code = readTestFile("contracts/simplestorage.sol");

	    Long createdDate = (System.currentTimeMillis() / 1000);

	    TransactionResult tr = contractRegistry.register(addr, "SimpleStorage", abi, code, CodeType.solidity, createdDate);
	    assertNotNull(tr);
	    assertNotNull(tr.getId());
	    assertTrue(!tr.getId().isEmpty());

	    Transaction tx = transactionService.waitForTx(tr, 50, TimeUnit.MILLISECONDS);
	    assertNotNull(tx);

	    Contract contract = contractRegistry.getById(addr);
	    assertNotNull(contract);
	    assertEquals(contract.getAddress(), addr);
	    assertEquals(contract.getABI(), abi);
	    assertEquals(contract.getCode(), code);
	    assertEquals(contract.getCodeType(), CodeType.solidity);
	    assertNotNull(contract.getCreatedDate());
	    assertEquals(contract.getCreatedDate(), createdDate);

	    BigInteger val = (BigInteger) contractService.read(addr, "get", null);
	    assertEquals(val.intValue(), 100);
	}

	@Test
	public void testList() throws IOException, InterruptedException {

	    Long createdDate = (System.currentTimeMillis() / 1000);

	    String addr = createContract();
	    String abi = readTestFile("contracts/simplestorage.abi.txt");
	    String code = readTestFile("contracts/simplestorage.sol");

	    System.out.println("putting addr into registry " + addr);

	    TransactionResult tr = contractRegistry.register(addr, "SimpleStorage", abi, code, CodeType.solidity, createdDate);
	    assertNotNull(tr);
	    assertNotNull(tr.getId());
	    assertTrue(!tr.getId().isEmpty());

	    Transaction tx = transactionService.waitForTx(tr, 50, TimeUnit.MILLISECONDS);
	    assertNotNull(tx);


	    List<Contract> list = contractRegistry.list();
	    assertNotNull(list);
	    assertTrue(!list.isEmpty());
	    assertTrue(list.size() > 0);

	    Contract c = list.get(0);
	    assertEquals(c.getAddress(), addr);
	    assertEquals(c.getABI(), abi);
	    assertEquals(c.getCode(), code);
	    assertTrue(c.getCreatedDate() >= createdDate);

	}

}
