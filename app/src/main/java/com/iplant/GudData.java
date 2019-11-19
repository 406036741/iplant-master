package com.iplant;

import com.iplant.model.Account;

import java.util.HashMap;
import java.util.Map;

public class GudData {
    //KEY映射
    //账号，用于存放账户信息
    public static final String KEY_Account = "account";
    public static final String KEY_ServerIP = "serverip";
    public static final String KEY_REMEMBERPWD = "remenberpwd";
    public static final String KEY_PASSWORD = "password";
    public static  String Login_PASSWORD = "";
    public static Map<String,String> CustomMap= new HashMap<String,String>();
    public static Account myAccount;
    public static int mPerson_judge = 0;
    public static int mShift_Status = 0;
    public static String QRScanHint = "按设备扫描键扫码";
    //地址，用于intent传递显示地址
    public static final String KEY_URL = "webUrl";
    public static final String KEY_ModuleID ="KEY_ModuleID";
    //地址栏
    //public static String DOMAIN = "http://193rt95681.imwork.net:55909/";
   // public static String DOMAIN = "http://172.16.128.82:8088/MESCore/";
    public static String DOMAIN = "http://10.200.10.24:8080/MESCore/";
    //public static String DOMAIN = "http://192.168.31.137:8088/MESCore/";
    //public static String DOMAIN = "http://10.200.255.171:8088/MESCore/";
    //内网地址
    public static final String LANIP = "http://192.168.10.251:8080/";

    //外网地址
    public static final String INTERNET = "http://zhouquan.imwork.net:20106/";


    //刷新频率
    //版本检测
    public static final int TICKS_UPDATE_VERSION = 10 * 60 * 1000; //10分钟
    //消息
    public static final int TICKS_UPDATE_GROUP = 10 * 1000; //10秒

}
