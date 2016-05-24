package com.jpmorgan.ib.caonpd.cakeshop.db;

import com.jpmorgan.ib.caonpd.cakeshop.model.Block;

public interface BlockListener {

    public void blockCreated(Block block);

    public void shutdown();

}
