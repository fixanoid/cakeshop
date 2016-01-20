package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import static com.jpmorgan.ib.caonpd.ethereum.enterprise.util.RpcUtil.*;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Block;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.BlockService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;

@Service
public class BlockServiceImpl implements BlockService {

    @Autowired
    private GethHttpService gethService;

    @Override
    public Block get(String hash, Integer number, String tag) throws APIException {

        String method = null;
        Object input = null;

        if (hash != null && !hash.isEmpty()) {
            method = "eth_getBlockByHash";
            input = hash;
        } else if (number != null && number >= 0) {
            method = "eth_getBlockByNumber";
            input = number;
        } else if (tag != null && !tag.isEmpty()) {
            method = "eth_getBlockByNumber";
            input = tag;
        }

        Map<String, Object> blockData =
                gethService.executeGethCall(method, new Object[]{ input, false });

        if (blockData == null) {
            return null;
        }

        // Convert to model
        Block block = new Block();

        // add addresses directly
        block.setId((String)blockData.get("hash"));
        block.setParentId((String)blockData.get("parentHash"));
        block.setNonce((String)blockData.get("nonce"));
        block.setSha3Uncles((String)blockData.get("sha3Uncles"));
        block.setLogsBloom((String)blockData.get("logsBloom"));
        block.setTransactionsRoot((String)blockData.get("transactionsRoot"));
        block.setStateRoot((String)blockData.get("stateRoot"));
        block.setMiner((String)blockData.get("miner"));
        block.setExtraData((String)blockData.get("extraData"));
        block.setTransactions((List<String>) blockData.get("transactions"));
        block.setUncles((List<String>) blockData.get("uncles"));

        // convert longs
        block.setNumber(toLong("number", blockData));
        block.setDifficulty(toLong("difficulty", blockData));
        block.setTotalDifficulty(toLong("totalDifficulty", blockData));
        block.setGasLimit(toLong("gasLimit", blockData));
        block.setGasUsed(toLong("gasUsed", blockData));
        block.setTimestamp(toLong("timestamp", blockData));

        return block;
    }

}
