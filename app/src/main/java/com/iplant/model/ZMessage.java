package com.iplant.model;


import java.util.ArrayList;
import java.util.List;

public class ZMessage extends ModelBase {
    public List<EXCMessage> EXCMessageList;
   public ZMessage()
   {
       EXCMessageList =new ArrayList<>();
   }
}

