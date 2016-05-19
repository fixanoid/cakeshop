package com.jpmorgan.ib.caonpd.ethereum.enterprise.controller;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.config.AppStartup;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UnhealthyController {

    @Autowired
    private AppStartup appStartup;

    @RequestMapping("/unhealthy")
    public ModelAndView unhealthy(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("unhealthy");
        mav.addObject("appStartup", appStartup);
        mav.addObject("application", request.getServletContext());
        return mav;
    }

}
