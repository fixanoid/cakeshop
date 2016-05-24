package com.jpmorgan.ib.caonpd.cakeshop.service.impl;

import static com.jpmorgan.ib.caonpd.cakeshop.util.RpcUtil.*;

import com.jpmorgan.ib.caonpd.cakeshop.error.APIException;
import com.jpmorgan.ib.caonpd.cakeshop.model.Contract;
import com.jpmorgan.ib.caonpd.cakeshop.model.ContractABI;
import com.jpmorgan.ib.caonpd.cakeshop.model.Event;
import com.jpmorgan.ib.caonpd.cakeshop.service.ContractService;
import com.jpmorgan.ib.caonpd.cakeshop.service.EventService;
import com.jpmorgan.ib.caonpd.cakeshop.service.GethHttpService;
import com.jpmorgan.ib.caonpd.cakeshop.util.RpcUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventServiceImpl implements EventService {

    class BlockRangeFilter {
        private final String fromBlock;
        private final String toBlock;

        public BlockRangeFilter(Long fromBlock, Long toBlock) {
            this.fromBlock = RpcUtil.toHex(fromBlock);
            this.toBlock = RpcUtil.toHex(toBlock);
        }

        public String getFromBlock() {
            return fromBlock;
        }

        public String getToBlock() {
            return toBlock;
        }
    }

    @Autowired
    private GethHttpService gethService;

    @Autowired
    private ContractService contractService;

    @SuppressWarnings("unchecked")
    @Override
    public List<Event> listForBlock(Long blockNumber) throws APIException {
        Map<String, Object> res = gethService.executeGethCall("eth_getLogs", new Object[] { new BlockRangeFilter(blockNumber, blockNumber) });
        List<Map<String, Object>> results = (List<Map<String, Object>>) res.get("_result");
        return processEvents(results);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Event> processEvents(List<Map<String, Object>> rawEvents) throws APIException {

        List<Event> events = new ArrayList<>();
        for (Map<String, Object> data : rawEvents) {

            Event event = new Event();
            event.setBlockId((String) data.get("blockHash"));
            event.setBlockNumber(toLong("blockNumber", data));

            event.setLogIndex(toLong("logIndex", data));
            event.setTransactionId((String) data.get("transactionHash"));
            event.setContractId((String) data.get("address"));

            Contract contract = contractService.get(event.getContractId());
            if (contract == null || contract.getABI() == null) {
                // TODO can't process this event
                // this will occur when loading a transaction related to a contract deploy
                // because it isn't yet registered
                continue;
            }

            ContractABI abi = ContractABI.fromJson(contract.getABI());

            List<String> topics = (List<String>) data.get("topics");
            String eventSigHash = (topics).get(0);

            com.jpmorgan.ib.caonpd.cakeshop.model.ContractABI.Event abiEvent = abi.findEventBySignature(eventSigHash);
            event.setName(abiEvent.name);

            byte[] logData = Hex.decode(((String) data.get("data")).substring(2));
            byte[][] topicData = new byte[topics.size()][];
            for (int i = 0; i < topics.size(); i++) {
                String t = topics.get(i);
                topicData[i] = Hex.decode(t.substring(2));
            }

            Object[] decodeHex = abiEvent.decode(logData, topicData).toArray();
            event.setData(decodeHex);

            events.add(event);
        }
        return events;
    }

}
