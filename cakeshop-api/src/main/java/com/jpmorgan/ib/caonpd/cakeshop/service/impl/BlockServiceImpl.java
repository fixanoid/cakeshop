package com.jpmorgan.ib.caonpd.cakeshop.service.impl;

import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Block;
import static com.jpmorgan.ib.caonpd.cakeshop.util.AbiUtils.*;

import com.jpmorgan.ib.caonpd.cakeshop.error.APIException;
//import com.jpmorgan.ib.caonpd.cakeshop.model.Block;
import com.jpmorgan.ib.caonpd.cakeshop.model.RequestModel;
import com.jpmorgan.ib.caonpd.cakeshop.service.BlockService;
import com.jpmorgan.ib.caonpd.cakeshop.service.GethHttpService;
import java.math.BigInteger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BlockServiceImpl implements BlockService {

    @Autowired
    private GethHttpService gethService;

    @Override
    public Block get(String id, Long number, String tag) throws APIException {

        String method = null;
        Object input = null;

        if (id != null && !id.isEmpty()) {
            method = "eth_getBlockByHash";
            input = id;
        } else if (number != null && number >= 0) {
            method = "eth_getBlockByNumber";
            input = number;
        } else if (tag != null && !tag.isEmpty()) {
            method = "eth_getBlockByNumber";
            input = tag;
        }

        if (method == null || input == null) {
            throw new APIException("Bad request");
        }

        Map<String, Object> blockData =
                gethService.executeGethCall(method, new Object[]{ input, false });

        return processBlockData(blockData);
    }

    @SuppressWarnings("unchecked")
    private Block processBlockData(Map<String, Object> blockData) {
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
        block.setBlockNumber(new BigInteger(String.valueOf(toLong("number", blockData))));
        block.setDifficulty(new BigInteger(String.valueOf(toLong("difficulty", blockData))));
        block.setTotalDifficulty(new BigInteger(String.valueOf(toLong("totalDifficulty", blockData))));
        block.setGasLimit(new BigInteger(String.valueOf(toLong("gasLimit", blockData))));
        block.setGasUsed(new BigInteger(String.valueOf(toLong("gasUsed", blockData))));
        block.setTimestamp(new BigInteger(String.valueOf(toLong("timestamp", blockData))));

        return block;
    }

    @Override
    public List<Block> get(long start, long end) throws APIException {
        List<RequestModel> reqs = new ArrayList<>();
        for (long i = start; i <= end; i++) {
            reqs.add(new RequestModel("eth_getBlockByNumber", new Object[]{ i, false }, 42L));
        }
        return batchGet(reqs);
    }

    @Override
    public List<Block> get(List<Long> numbers) throws APIException {
        List<RequestModel> reqs = new ArrayList<>();
        for (Long num : numbers) {
            reqs.add(new RequestModel("eth_getBlockByNumber", new Object[]{ num, false }, 42L));
        }
	    return batchGet(reqs);
    }

    private List<Block> batchGet(List<RequestModel> reqs) throws APIException {
        List<Map<String, Object>> batchRes = gethService.batchExecuteGethCall(reqs);

	    // TODO ignore return order for now
	    List<Block> blocks = new ArrayList<>();
	    for (Map<String, Object> blockData : batchRes) {
	        blocks.add(processBlockData(blockData));
	    }
	    return blocks;
    }

}
