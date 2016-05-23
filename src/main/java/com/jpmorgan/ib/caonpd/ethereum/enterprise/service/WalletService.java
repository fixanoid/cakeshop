package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Account;

import java.util.List;


public interface WalletService {

    /**
     * List accounts in the wallet
     *
     * @return
     * @throws APIException
     */
    public List<Account> list() throws APIException;

    /**
     * Create new account (no passphrase for now)
     *
     * @return
     * @throws APIException
     */
    public Account create() throws APIException;

}
