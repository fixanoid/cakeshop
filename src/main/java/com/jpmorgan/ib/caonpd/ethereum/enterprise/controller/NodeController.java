/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.controller;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.AdminBean;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.config.JsonMethodArgumentResolver.JsonBodyParam;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIData;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIError;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIResponse;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Node;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.NodeService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl.GethHttpServiceImpl;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author N631539
 */
@RestController
@RequestMapping(value = "/api/node",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
public class NodeController extends BaseController {

    @Autowired
    private GethHttpService gethService;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private AdminBean adminBean;

    @RequestMapping({"/{funcName}", "/miner/{funcName}"})
    protected ResponseEntity<APIResponse> adminFuncCall(@PathVariable String funcName,
            @JsonBodyParam(value = "args", required = false) String funcArguments) throws APIException {

        String args[] = null;
        Node node;
        APIResponse apiResponse = new APIResponse();
        Map<String, Object> data=null;

        if (StringUtils.isNotEmpty(funcName) && ( funcName.equalsIgnoreCase("status") || funcName.equalsIgnoreCase("get")) ) {
            node = nodeService.get();
            apiResponse.setData(new APIData(node.getId(), "node", node));
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        }

        if (StringUtils.isNotEmpty(funcArguments)) {
            args = funcArguments.split(",");
        } else if (AdminBean.ADMIN_MINER_START_KEY.equalsIgnoreCase(funcName)) {
            args = new String[]{"1"}; //set default to one cpu
        }

        Map<String, String> functionNames = adminBean.getFunctionNames();
        String gethFunctionName = functionNames.get(funcName);

        if (gethFunctionName == null) {
            apiResponse.addError(new APIError(null, "400", "Bad request"));
            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
        }

        data = gethService.executeGethCall(gethFunctionName, args);

        if (data != null) {
            if (data.containsKey(GethHttpServiceImpl.SIMPLE_RESULT)) {
                return new ResponseEntity<>(APIResponse.newSimpleResponse(data.get(GethHttpServiceImpl.SIMPLE_RESULT)), HttpStatus.OK);
            }

            APIData apiData = nodeService.getAPIData(data);
            apiResponse.setData(apiData);
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);

        } else {
            apiResponse.addError(new APIError(null, "500", "Empty response from server"));
            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
        }

    }

}
