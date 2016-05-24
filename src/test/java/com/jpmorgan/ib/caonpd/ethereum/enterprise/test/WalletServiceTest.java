package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.testng.Assert.*;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.dao.WalletDAO;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Account;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.WalletService;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

public class WalletServiceTest extends BaseGethRpcTest {

    @Autowired
    private WalletService wallet;

    @Autowired
    private WalletDAO walletDAO;

    @Test(priority=0)
    public void testList() throws APIException {
        List<Account> accounts = wallet.list();
        assertNotNull(accounts);
        assertEquals(accounts.size(), 3);
        assertTrue(StringUtils.isNotBlank(accounts.get(0).getAddress()));

        assertEquals(walletDAO.list().size(), 3);
    }

    @Test(priority=3)
    public void testCreate() throws APIException {
        List<Account> accounts = wallet.list();
        assertNotNull(accounts);
        assertEquals(accounts.size(), 3);

        // create
        Account acc = wallet.create();
        assertNotNull(acc);
        assertTrue(StringUtils.isNotBlank(acc.getAddress()));

        accounts = wallet.list();
        assertNotNull(accounts);
        assertEquals(accounts.size(), 4);

        assertEquals(walletDAO.list().size(), 4);
    }

}
