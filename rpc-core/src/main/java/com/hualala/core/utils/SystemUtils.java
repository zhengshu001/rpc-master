package com.hualala.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiangbin on 2016/9/5.
 */
public class SystemUtils {

    private static Logger logger = LoggerFactory.getLogger(SystemUtils.class);

    private static Map<String, String> systemMap = new HashMap<>();

    public static String sysHostname() {
        String hostname = systemMap.get("hostname");
        if (hostname == null) {
            try {
                hostname = InetAddress.getLocalHost().getHostName();
            } catch (Exception var) {
                hostname = "";
            }
            systemMap.put("hostname", hostname);
        }
        return hostname;
    }

    public static String sysHostAddress() {
        String hostaddress = systemMap.get("hostaddress");
        if (hostaddress == null) {
            try {
                hostaddress = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception var) {
                hostaddress = "";
            }
            systemMap.put("hostaddress", hostaddress);
        }
        return hostaddress;
    }
}
