package com.jpmorgan.ib.caonpd.ethereum.enterprise.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.config.JsonMethodArgumentResolver.JsonBodyParam;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Block;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;

@RestController
@RequestMapping(value = "/block",
	method = RequestMethod.POST,
	consumes = MediaType.APPLICATION_JSON_VALUE,
	produces = MediaType.APPLICATION_JSON_VALUE)
public class BlockController {

  @Autowired
  private GethHttpService gethService;

  @Value("${geth.genesis}")
  private String genesisFilePath;

  private Long toLong(String key, Map<String, Object> blockData) {
      return Long.decode((String)blockData.get(key));
  }

  @RequestMapping(value = "/get")
  public ResponseEntity<Block> getBlock(
  		@JsonBodyParam(required=false) String hash,
  		@JsonBodyParam(required=false) Integer number,
  		@JsonBodyParam(required=false) String tag) throws JsonParseException, JsonMappingException, IOException {

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

    Map<String, Object> blockData = gethService.executeGethCall(method, new Object[]{ 0, false });

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

    System.out.println(block);

    return new ResponseEntity<Block>(block, HttpStatus.OK);
  }

}
