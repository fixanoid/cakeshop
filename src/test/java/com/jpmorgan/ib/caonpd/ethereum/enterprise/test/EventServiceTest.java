package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.testng.Assert.*;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Event;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.EventService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

public class EventServiceTest extends BaseGethRpcTest {

	@Autowired
	private ContractService contractService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private EventService eventService;

	@Test
	public void testListForBlock() throws IOException, InterruptedException {
		String addr = createContract();
		TransactionResult tr = contractService.transact(addr, null, "set", new Object[]{ 100 });
		Transaction tx = transactionService.waitForTx(tr, 50, TimeUnit.MILLISECONDS);

		List<Event> events = eventService.listForBlock(tx.getBlockNumber());
		assertNotNull(events);
		assertFalse(events.isEmpty());
		assertEquals(events.size(), 1);

		testEvent(events.get(0));
		testEvent(tx.getLogs().get(0));
	}

    private void testEvent(Event event) {
        Object[] data = event.getData();
		assertNotNull(data);
		assertEquals(data.length, 2);
		assertEquals(data[0], "change val");
		assertEquals(data[1], BigInteger.valueOf(100L));
    }

}
