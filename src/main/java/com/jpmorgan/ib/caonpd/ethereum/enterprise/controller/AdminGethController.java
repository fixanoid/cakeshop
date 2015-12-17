/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.controller;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.config.JsonMethodArgumentResolver.JsonBodyParam;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIResponse;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.NodeInfo;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.NodeService;

/**
 *
 * @author N631539
 */
@Controller
public class AdminGethController extends BaseController {

    @Autowired
    private GethHttpService gethService;

    @Autowired
    private NodeService nodeService;

    @Value("${geth.verbosity:}")
    private Integer verbosity;
    @Value("${geth.mining:}")
    private Boolean mining;
    @Value("${geth.identity:}")
    private String identity;
    @Value("${geth.networkid}")
    private Integer networkid;

    @RequestMapping(value = {"/node/settings/update"}, method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
    protected @ResponseBody
    ResponseEntity<APIResponse> updateNodeInfo(@JsonBodyParam(required = false) String verbosity,
            @JsonBodyParam(required = false) String identity,
            @JsonBodyParam(required = false) String mining,
            @JsonBodyParam(required = false) String networkid) throws APIException {

        Map<String, String> newProps = new HashMap();

        String response;

        if (StringUtils.isNotEmpty(mining)) {
            newProps.put("geth.mining", mining);
            this.mining = Boolean.valueOf(mining);
        }

        if (StringUtils.isNotEmpty(identity)) {
            newProps.put("geth.identity", identity);
            this.identity = identity;
        }

        if (StringUtils.isNotEmpty(verbosity)) {
            newProps.put("geth.verbosity", verbosity);
            this.verbosity = Integer.valueOf(verbosity);
        }

        if (StringUtils.isNotEmpty(networkid)) {
            newProps.put("geth.networkid", networkid);
            this.networkid = Integer.valueOf(networkid);
        }

        if (newProps.size() > 0) {
            gethService.setNodeInfo(this.identity, this.mining, this.verbosity, this.networkid);
            nodeService.updateNodeInfo(newProps);
            response = "Node Updated";
        } else {
            response = "Params are empty. Node has not been updated";
        }

        return new ResponseEntity(APIResponse.newSimpleResponse(response), HttpStatus.OK);
    }

    @RequestMapping(value = {"/node/reset"}, method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
    protected @ResponseBody
    ResponseEntity<APIResponse> resetNodeInfo() {
        Boolean reset = nodeService.resetNodeInfo();
        return new ResponseEntity(APIResponse.newSimpleResponse(reset), HttpStatus.OK);
    }

    @RequestMapping(value = {"/node/settings"}, method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
    protected @ResponseBody
    ResponseEntity<APIResponse> getNodeInfo() throws APIException {
        NodeInfo nodeInfo = new NodeInfo(identity, mining, networkid, verbosity);
        APIResponse res = new APIResponse();
        res.setData(nodeInfo.toAPIData());
        return new ResponseEntity(res, HttpStatus.OK);
    }
}
