package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Node;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.NodeInfo;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Peer;

import java.util.List;

public interface NodeService {

    public String NODE_RUNNING_STATUS="running";
    public String NODE_NOT_RUNNING_STATUS="stopped";

    /**
     * Get node information
     *
     * @return {@link Node}
     * @throws APIException
     */
    public Node get() throws APIException;

    /**
     * Update node configuration (may trigger restart)
     *
     * @param logLevel     Log level (0 = least verbose, 6 = most verbose)
     * @param networkID
     * @param identity
     * @param mining
     * @return
     * @throws APIException
     */
    public NodeInfo update(
            Integer logLevel,
            Integer networkID,
            String identity,
            Boolean mining,
            String extraParams,
            String genesisBlock) throws APIException;

    /**
     * Reset node back to default configuration (will restart)
     *
     * @return
     */
    public Boolean reset();

    /**
     * Retrieve a list of connected peers
     *
     * @return
     * @throws APIException
     */
    public List<Peer> peers() throws APIException;

}
