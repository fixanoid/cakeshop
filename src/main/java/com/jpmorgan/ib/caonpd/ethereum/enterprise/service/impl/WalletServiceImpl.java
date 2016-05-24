package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.AdminBean;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.dao.WalletDAO;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Account;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.WalletService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.util.RpcUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author sam
 */
@Service
public class WalletServiceImpl implements WalletService {

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

        Map<String, Object> data = gethService.executeGethCall(AdminBean.PERSONAL_LIST_ACCOUNTS, new Object[]{});

        if (data != null && data.containsKey("_result")) {
            accountList = (List<String>) data.get("_result");
            if (accountList != null) {
                accounts = new ArrayList<>();
                for (String address : accountList) {
                    Map<String, Object> accountData = gethService.executeGethCall(
                            AdminBean.PERSONAL_GET_ACCOUNT_BALANCE, new Object[] { address, "latest" });
                    String strBal = (String)accountData.get("_result");
                    BigInteger bal = RpcUtil.hexToBigInteger(strBal);
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

}
