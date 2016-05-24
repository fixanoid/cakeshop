package com.jpmorgan.ib.caonpd.cakeshop.service;

import com.jpmorgan.ib.caonpd.cakeshop.error.APIException;
import com.jpmorgan.ib.caonpd.cakeshop.model.RequestModel;

import java.util.List;
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

    public List<Map<String, Object>> batchExecuteGethCall(List<RequestModel> requests) throws APIException;

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
     * Returns the current node status
     *
     * @return
     */
    public Boolean isRunning();

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
