/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.controller;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.RequestModel;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author I629630
 */
@Controller
public class GethController {

    @Autowired
    private GethHttpService gethService;

    @Value("${geth.genesis}")
    private String genesis;
    @Value("${geth.datadir}")
    private String datadir;

    @RequestMapping("/index")
    protected @ResponseBody
    String getIndex() {
        return "Enterprise Index Page";
    }

    @RequestMapping(value = "/submit_func", method = POST)
    protected @ResponseBody
    String submitFuncCall(@RequestParam("func_name") String funcName, @RequestParam(value = "func_args", required = false) String funcArguments) {
        //funcArguments must be comma separated values
        //first generate json to execute function
        //request need method name, method arguments and id. jsonrpc defaults to version 2.0
        String args[] = null;
        if (StringUtils.isNotEmpty(funcArguments)) {
            args = funcArguments.split(",");
        }
        RequestModel request = new RequestModel(GethHttpService.GETH_API_VERSION, funcName, args, GethHttpService.USER_ID);
        Gson gson = new Gson();
        String response = gethService.executeGethCall(gson.toJson(request));
        return response;
    }

    @RequestMapping(value = "/node-control/start", method = POST)
    protected @ResponseBody
    Boolean startGeth(HttpServletRequest request, @RequestParam(value = "start_params", required = false) String[] startupParams) {
        String genesisDir = request.getServletContext().getRealPath("/") + genesis;
        String command = request.getServletContext().getRealPath("/");
        List<String> params = null;
        if (null != startupParams) {
            params = Lists.newArrayList(startupParams);
        }
        Boolean started = gethService.startGeth(command, genesisDir, null, params);
        return started;
    }

    @RequestMapping(value = "/node-control/stop", method = POST)
    protected @ResponseBody
    Boolean stopGeth() {
        Boolean stopped = gethService.stopGeth();
        gethService.deletePid();
        return stopped;
    }

    @RequestMapping(value = "/node-control/restart", method = POST)
    protected @ResponseBody
    Boolean restartGeth(HttpServletRequest request) {
        Boolean stopped = gethService.stopGeth();
        Boolean deleted = gethService.deletePid();
        Boolean restarted = false;
        if (stopped && deleted) {
            String genesisDir = request.getServletContext().getRealPath("/") + genesis;
            String command = request.getServletContext().getRealPath("/");
            restarted = gethService.startGeth(command, genesisDir, null, null);
        }
        return restarted;
    }
    
    @RequestMapping(value = "/node-control/reset", method = POST)
    protected @ResponseBody
    Boolean resetGeth(HttpServletRequest request, @RequestParam(value = "start_params", required = false) String[] startupParams) {
        String eth_datadir = datadir.startsWith("/.") ? System.getProperty("user.home") + datadir : datadir;
        Boolean stopped = gethService.stopGeth();
        gethService.deletePid();
        Boolean deletedDaraDir = gethService.deletEthDatabase(eth_datadir);
        Boolean reset = false;
        if (stopped && deletedDaraDir) {
            String genesisDir = request.getServletContext().getRealPath("/") + genesis;
            String command = request.getServletContext().getRealPath("/");
            reset = gethService.startGeth(command, genesisDir, null, null);
        }
        return reset;
    }
    
    
}
