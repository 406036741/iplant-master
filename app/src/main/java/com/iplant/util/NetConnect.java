package com.iplant.util;

import java.net.URL;
import java.net.URLConnection;

/**
 * Created by shris on 2017/3/17.
 */

public class NetConnect {

    public static String testUrlWithTimeOut(String wLANIP,String wINTERNET,int timeOutMillSeconds){
        long lo = System.currentTimeMillis();
        URL url;
        try {
            url = new URL(wLANIP);
            URLConnection co =  url.openConnection();
            co.setConnectTimeout(timeOutMillSeconds);
            co.connect();
            System.out.println("局域网可用");
            return wLANIP;
        } catch (Exception e1) {
            System.out.println("局域网不可用!");
        }
        System.out.println(System.currentTimeMillis()-lo);
        return wINTERNET;
    }

}
