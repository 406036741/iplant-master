package com.iplant.util;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by shris on 2017/4/10.
 */

public class MacUtil {
    public static String getLocalMacAddress() {
        String Mac=null;
        try{

            String path="sys/class/net/wlan0/address";
            if((new File(path)).exists())
            {
                FileInputStream fis = new FileInputStream(path);
                byte[] buffer = new byte[8192];
                int byteCount = fis.read(buffer);
                if(byteCount>0)
                {
                    Mac = new String(buffer, 0, byteCount, "utf-8");
                }
            }
            if(Mac==null||Mac.length()==0)
            {
                path="sys/class/net/eth0/address";
                FileInputStream fis_name = new FileInputStream(path);
                byte[] buffer_name = new byte[8192];
                int byteCount_name = fis_name.read(buffer_name);
                if(byteCount_name>0)
                {
                    Mac = new String(buffer_name, 0, byteCount_name, "utf-8");
                }
            }
            if(Mac==null||Mac.length()==0){
                return "";
            }
        }catch(Exception io){
            return "";
        }
        return Mac.trim();
    }
}
