package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

/**
 * Service which tracks the Blockchain in a local database. As blocks are added
 * to the chain, they are added to our database in near-realtime.
 *
 * @author chetan
 *
 */
public interface BlockScanner {

    public void start();

    public void shutdown();

}
