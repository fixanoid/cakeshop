package com.jpmorgan.ib.caonpd.cakeshop.service.task;

import com.jpmorgan.ib.caonpd.cakeshop.dao.PeerDAO;
import com.jpmorgan.ib.caonpd.cakeshop.error.APIException;
import com.jpmorgan.ib.caonpd.cakeshop.model.Peer;
import com.jpmorgan.ib.caonpd.cakeshop.service.NodeService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Load stored peers and try to reconnect to them
 *
 * @author chetan
 *
 */
@Component
@Scope("prototype")
public class LoadPeersTask implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
            .getLogger(LoadPeersTask.class);

    @Autowired
    private PeerDAO peerDAO;

    @Autowired
    private NodeService nodeService;

    @Override
    public void run() {

        List<Peer> peers = peerDAO.list();
        if (peers.size() > 0) {
            LOG.info("Reconnecting " + peers.size() + " peer(s)");
        }

        for (Peer peer : peers) {
            try {
                LOG.debug("Reconnecting to " + peer.getNodeUrl());
                nodeService.addPeer(peer.getNodeUrl());
            } catch (APIException e) {
                LOG.warn("Failed to add peer " + peer.getNodeUrl());
            }
        }

    }

}
