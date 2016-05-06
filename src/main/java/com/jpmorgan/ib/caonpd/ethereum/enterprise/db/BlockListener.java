package com.jpmorgan.ib.caonpd.ethereum.enterprise.db;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Block;

public interface BlockListener {

    public void blockCreated(Block block);

    public void shutdown();

}
