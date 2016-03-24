/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;

import java.util.Map;

/**
 *
 * @author I629630
 */
public interface GethHttpService {

    public static final String GETH_API_VERSION = "2.0";
    public static final Long   GETH_REQUEST_ID = 42L; // We don't actually use this, so just use a constant

    /**
     * Call the given Geth RPC method
     *
     * @param funcName             RPC function name
     * @param args                 Optional args
     *
     * @return
     * @throws APIException
     */
    public Map<String, Object> executeGethCall(String funcName, Object[] args) throws APIException;

    /**
     * Start the Geth node
     *
     * @param additionalParams
     * @return
     */
    public Boolean start(String... additionalParams);

    /**
     * Stop the Geth node, if already running
     *
     * @return
     */
    public Boolean stop();

    /**
     * Reset the Geth data directory and restart the node
     *
     * @return
     */
    public Boolean reset();

    /**
     * Delete the PID file
     *
     * @return
     */
    public Boolean deletePid();

}
