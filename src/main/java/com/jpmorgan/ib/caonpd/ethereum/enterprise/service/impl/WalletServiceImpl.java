
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.AdminBean;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Account;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Peer;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.WalletService;
import java.util.ArrayList;
import java.util.Iterator;
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

    @Override
    public List<Account> list() throws APIException {

        Object input = null;
        List<String> accountList = null;
        List<Account> accounts = null;
        Account account = null;

        Map<String, Object> data = gethService.executeGethCall(AdminBean.PERSONAL_LIST_ACCOUNTS, new Object[]{});

        if (data != null && data.containsKey("_result")) {
            accountList = (List) data.get("_result");
            if (accountList != null) {
                accounts = new ArrayList<>();
                for (String address : accountList) {
                    account = new Account();
                    account.setAddress(address);
                    accounts.add(account);
                }
            }
        }

        return accounts;
    }

}
