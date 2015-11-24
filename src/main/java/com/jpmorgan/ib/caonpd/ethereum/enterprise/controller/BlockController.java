package com.jpmorgan.ib.caonpd.ethereum.enterprise.controller;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ethereum.util.RLP;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.Gson;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.config.JsonMethodArgumentResolver.JsonBodyParam;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Block;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.RequestModel;
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

    Map<String, Object> blockData = gethService.executeGethCall(method, new Object[]{ input, false });
    System.out.println(blockData);

    Block block = new Block();
//    block.setNumber(blockData.get("number"));

    String hexNum = (String)blockData.get("number");
    System.out.println("hexnum: " + hexNum);

    byte[] decoded;
    if (hexNum.startsWith("0x")) {
    	decoded = Hex.decode(hexNum.substring(2, hexNum.length()-1));
    } else {
    	decoded = Hex.decode(hexNum);
    }

    System.out.println(decoded);

    int num = RLP.decodeInt(decoded, 0);
    System.out.println("got: " + num);


    return null;
  }

  @RequestMapping(value = "/submit_func", method = POST)
  protected @ResponseBody String submitFuncCall(@RequestParam ("func_name") String funcName, @RequestParam (value = "func_args", required = false) String funcArguments) {
      //funcArguments must be comma separated values
      //first generate json to execute function
      //request need method name, method arguments and id. jsonrpc defaults to version 2.0
      String args [] = null;
      if (StringUtils.isNoneEmpty(funcArguments)) {
          args = funcArguments.split(",");
      }
      RequestModel request = new RequestModel("2.0", funcName, args, "id");
      Gson gson = new Gson();
      String response = gethService.executeGethCall(gson.toJson(request));
      return response;
  }

}
