package com.jpmorgan.ib.caonpd.cakeshop.service.impl;

import com.jpmorgan.ib.caonpd.cakeshop.dao.WalletDAO;
import com.jpmorgan.ib.caonpd.cakeshop.error.APIException;
import com.jpmorgan.ib.caonpd.cakeshop.model.Account;
import com.jpmorgan.ib.caonpd.cakeshop.service.GethHttpService;
import com.jpmorgan.ib.caonpd.cakeshop.service.GethRpcConstants;
import com.jpmorgan.ib.caonpd.cakeshop.service.WalletService;
import com.jpmorgan.ib.caonpd.cakeshop.util.AbiUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author sam
 */
@Service
public class WalletServiceImpl implements WalletService, GethRpcConstants {

    private static final String DUMMY_PAYLOAD_HASH = AbiUtils.sha3AsHex("foobar");

    @Autowired
    private GethHttpService gethService;

    @Autowired
    private WalletDAO walletDAO;

    @SuppressWarnings("unchecked")
    @Override
    public List<Account> list() throws APIException {

        List<String> accountList = null;
        List<Account> accounts = null;
        Account account = null;

        Map<String, Object> data = gethService.executeGethCall(PERSONAL_LIST_ACCOUNTS, new Object[]{});

        if (data != null && data.containsKey("_result")) {
            accountList = (List<String>) data.get("_result");
            if (accountList != null) {
                accounts = new ArrayList<>();
                for (String address : accountList) {
                    Map<String, Object> accountData = gethService.executeGethCall(
                            PERSONAL_GET_ACCOUNT_BALANCE, new Object[] { address, "latest" });
                    String strBal = (String)accountData.get("_result");
                    BigInteger bal = AbiUtils.hexToBigInteger(strBal);
                    account = new Account();
                    account.setAddress(address);
                    account.setBalance(bal.toString());
                    accounts.add(account);
                }
            }
        }

        return accounts;
    }

    @Override
    public Account create() throws APIException {
        Map<String, Object> result = gethService.executeGethCall("personal_newAccount", new Object[] { "" });
        String newAddress = (String) result.get("_result");

        Account a = new Account();
        a.setAddress(newAddress);

        walletDAO.save(a);
        return a;
    }

    @Override
    public boolean isUnlocked(String address) throws APIException {
        try {
            Map<String, Object> result = gethService.executeGethCall("eth_sign", new Object[] { address, DUMMY_PAYLOAD_HASH });
            if (StringUtils.isNotBlank((String) result.get("_result"))) {
                return true;
            }
        } catch (APIException e) {
            if (e.getMessage().indexOf("account is locked") < 0) {
                throw e;
            }
        }
        return false;
    }

}
