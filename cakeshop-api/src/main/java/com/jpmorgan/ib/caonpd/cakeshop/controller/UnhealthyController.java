package com.jpmorgan.ib.caonpd.cakeshop.controller;

import static org.springframework.http.MediaType.*;

import com.jpmorgan.ib.caonpd.cakeshop.config.AppStartup;
import com.jpmorgan.ib.caonpd.cakeshop.model.APIError;
import com.jpmorgan.ib.caonpd.cakeshop.model.APIResponse;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/unhealthy")
public class UnhealthyController {

    @Autowired
    private AppStartup appStartup;

    public ModelAndView unhealthy(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("unhealthy");
        mav.addObject("appStartup", appStartup);
        mav.addObject("application", request.getServletContext());
        return mav;
    }

    @RequestMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse> unhealthyJson(HttpServletRequest request) {
        APIResponse res = new APIResponse()
            .error(new APIError().title("Service did not start cleanly"))
            .error(new APIError().title("Debug info").detail(appStartup.getDebugInfo(request.getServletContext())))
            .error(new APIError().title("Errors").detail(appStartup.getErrorInfo()));

        return new ResponseEntity<APIResponse>(res, HttpStatus.SERVICE_UNAVAILABLE);
    }


}
