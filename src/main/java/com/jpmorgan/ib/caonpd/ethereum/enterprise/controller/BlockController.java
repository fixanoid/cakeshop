package com.jpmorgan.ib.caonpd.ethereum.enterprise.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.config.JsonMethodArgumentResolver.JsonBodyParam;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIError;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIResponse;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Block;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.BlockService;

@RestController
@RequestMapping(value = "/block",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
public class BlockController {

    @Autowired
    BlockService blockService;

    @RequestMapping(value = "/get")
    public ResponseEntity<APIResponse> getBlock(
            @JsonBodyParam(required=false) String hash,
            @JsonBodyParam(required=false) Integer number,
            @JsonBodyParam(required=false) String tag) throws APIException {

        Block block = blockService.get(hash, number, tag);
        //System.out.println(block);

        APIResponse res = new APIResponse();

        if (block != null) {
            res.setData(block.toAPIData());
            return new ResponseEntity<APIResponse>(res, HttpStatus.OK);
        }

        APIError err = new APIError();
        err.setStatus("404");
        err.setTitle("Block not found");
        res.addError(err);
        return new ResponseEntity<APIResponse>(res, HttpStatus.NOT_FOUND);
    }

}
