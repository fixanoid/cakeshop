package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Block;

public interface BlockListener {

    public void blockCreated(Block block);

}
