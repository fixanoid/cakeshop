package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Block;

public interface BlockService {

    /**
     * Get the block represented by the given identifier (only one is allowed
     * at a time).
     *
     * @param id                Block ID (hash)
     * @param number            Block number
     * @param tag               One of "earliest", "latest" or "pending"
     *
     * @return {@link Block}    Block or null if block does not exist
     *
     * @throws APIException
     */
    public Block get(String id, Long number, String tag) throws APIException;

}
