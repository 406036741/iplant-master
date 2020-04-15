package com.iplant.model;

import java.util.Calendar;

public class EXCMessage  {
    public int CompanyID;
    public long ID;
    public long MessageID;
    public long ModuleID;
    public long StationID;
    public String StationNo;
    public long ResponsorID;
    public long Type;
    public String Title = "";
    public String MessageText = "";
    public int ShiftID = 0;
    public Calendar EditTime = Calendar.getInstance();
    public Calendar CreateTime = Calendar.getInstance();
    /**
     *     /未读  0  //已发送未读   1 // 已读 2   // 已读后不发送了
     */
    public int Active=0;
}

