/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

}
