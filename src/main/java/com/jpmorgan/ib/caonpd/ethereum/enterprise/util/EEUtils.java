/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.util;


import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;


public class EEUtils {

    public static class IP {
        private String iface;
        private String addr;

        public IP(String iface, String addr) {
            this.iface = iface;
            this.addr = addr;
        }

        public String getIface() {
            return iface;
        }

        public void setIface(String iface) {
            this.iface = iface;
        }

        public String getAddr() {
            return addr;
        }

        public void setAddr(String addr) {
            this.addr = addr;
        }

        @Override
        public String toString() {
            return addr;
        }
    }

    
    public static List<IP> getAllIPs() throws APIException {

        List<IP> ips = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();

                // filter out some interfaces
                if ((SystemUtils.IS_OS_MAC && !iface.getName().startsWith("en"))
                        || ((SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_LINUX) && !iface.getName().startsWith("eth"))) {
                    continue;
                } 

                // collect IPs
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress ip = addresses.nextElement();
                    if (ip instanceof Inet4Address && ip.isSiteLocalAddress()) {
                        ips.add(new IP(iface.getName(), ip.getHostAddress()));
                    }
                }
            }

            // sort by interface name
            // we want the lowest (en0 or eth0) interface which is probably the ethernet adapter (vs WIFI)
            // (on MAC, at least the interfaces come back in a weird order)
            ips.sort(new Comparator<IP>() {
                @Override
                public int compare(IP o1, IP o2) {
                    return o1.getIface().compareTo(o2.getIface());
                }
            });

            return ips;

        } catch (SocketException e) {
            throw new APIException("Faild to get local IP address", e);
        }

    }

}
