package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Block;

public interface BlockService {

    public Block get(String hash, Long number, String tag) throws APIException;

}
