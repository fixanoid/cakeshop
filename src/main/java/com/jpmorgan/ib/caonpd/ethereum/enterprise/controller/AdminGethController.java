/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.controller;


import com.google.gson.Gson;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.AdminBean;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Node;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.RequestModel;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.NodeService;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author N631539
 */

@Controller
public class AdminGethController {
    
    @Autowired
    private GethHttpService gethService;    
    
    @Autowired
    private NodeService nodeService;
    
    @Autowired
    private AdminBean adminBean;
    
    @RequestMapping(value = {"/node/{funcName}","/miner/{funcName}"}, method = POST,produces = MediaType.APPLICATION_JSON_VALUE)
    protected @ResponseBody String adminFuncCall(@PathVariable String funcName, @RequestParam (value = "func_args", required = false) String funcArguments) throws APIException {
        
        String args [] = null;
        String response = null;
        RequestModel request = null;
        Node node = null;
        
        if(StringUtils.isNotEmpty(funcName) && funcName.equalsIgnoreCase("status")){
            node = nodeService.get();
            response = (node==null?new Node().toString():node.toString());
            return response;
        }
        
        if (StringUtils.isNotEmpty(funcArguments)) {
            args = funcArguments.split(",");
        //TODO read param from request
        }else if(AdminBean.ADMIN_MINER_START_KEY.equalsIgnoreCase(funcName)){
            args = new String[]{" "};
        }
        
        Map<String,String> functionNames = adminBean.getFunctionNames();
        String gethFunctionName = functionNames.get(funcName); 
        
        if(StringUtils.isNotEmpty(gethFunctionName)){
            request = new RequestModel(GethHttpService.GETH_API_VERSION, gethFunctionName, args, GethHttpService.USER_ID);
        } 
        Gson gson = new Gson();
        if (request != null)
         response = gethService.executeGethCall(gson.toJson(request));
        return response;
    }
    
    
}
