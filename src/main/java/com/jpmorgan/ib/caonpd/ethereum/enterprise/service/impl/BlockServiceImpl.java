package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import static com.jpmorgan.ib.caonpd.ethereum.enterprise.util.RpcUtil.*;

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

        // Convert to model
        Block block = new Block();

        // add addresses directly
        block.setHash((String)blockData.get("hash"));
        block.setParentHash((String)blockData.get("parentHash"));

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
