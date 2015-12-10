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
import java.util.Map;
import org.apache.commons.lang3.StringUtils;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 *
 * @author N631539
 */

@Controller
public class NodeController {
    
    @Autowired
    private GethHttpService gethService;    
    
    @Autowired
    private NodeService nodeService;
    
    @Autowired
    private AdminBean adminBean;
    
    @RequestMapping(value = {"/node/{funcName}","/miner/{funcName}"}, method = POST,produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<APIResponse> adminFuncCall(@PathVariable String funcName, @JsonBodyParam (value = "args", required = false) String funcArguments) throws APIException {
        
        String args [] = null;
        Node node = null;
        APIResponse apiResponse = new APIResponse();
        Map<String, Object> data=null;
        
        try{
            
            if(StringUtils.isNotEmpty(funcName) && funcName.equalsIgnoreCase("status")){
                
                node = nodeService.get();
                apiResponse.setData(new APIData(node.getId(),"Node",node));
                return new ResponseEntity<>(apiResponse, HttpStatus.OK);  
                
            }

            if (StringUtils.isNotEmpty(funcArguments)){

                args = funcArguments.split(",");

            }else if(AdminBean.ADMIN_MINER_START_KEY.equalsIgnoreCase(funcName)){

                args = new String[]{"1"};//set default to one cpu

            }

            Map<String,String> functionNames = adminBean.getFunctionNames();
            String gethFunctionName = functionNames.get(funcName); 

        
            if (gethFunctionName != null){ 
                data = gethService.executeGethCall(gethFunctionName, args);
            }

            if(data != null){
                
                Object result = data.get("id");
                if(result != null)
                    apiResponse = APIResponse.newSimpleResponse(result);
                
            }

        }catch(APIException ex){
            
            APIError error = new APIError();
            error.setCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
            error.setTitle("API Error");
            apiResponse.addError(error);
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            
        }
        
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
    
}

