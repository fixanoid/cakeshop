/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.util;


import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl.GethHttpServiceImpl;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EEUtils {
    
    private static final Logger LOG = LoggerFactory.getLogger(GethHttpServiceImpl.class);
    
    public static String getLocalIP() throws APIException{
        InetAddress inetAddress = null;
        
            try {
                inetAddress = InetAddress.getLocalHost();
            } catch (UnknownHostException ex) {
                LOG.error("Faild to get local IP address");
                throw new APIException("Faild to get local IP address", ex);
            }
            
            return inetAddress.getHostAddress();
            
    }
    
}
