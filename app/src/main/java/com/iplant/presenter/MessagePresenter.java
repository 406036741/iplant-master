package com.iplant.presenter;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.iplant.GudData;
import com.iplant.MyError;
import com.iplant.model.EXCMessage;
import com.iplant.model.ToolBox;
import com.iplant.model.ZMessage;
import com.iplant.presenter.http.DataFetchListener.JsonListener;
import com.iplant.presenter.http.DataFetchModule;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class MessagePresenter {
    static final String TAG = "MessagePresenter";
    private Context mContext;
    static HashMap<String, ToolBox> mCacheList;

    public MessagePresenter(Context c) {
        mContext = c;
        mCacheList = new HashMap<String, ToolBox>();
    }


    public void doCheckMessage(final String userInfo, String passWord) {

        try {
            DataFetchModule.getInstance().fetchJsonGet(
                    GudData.DOMAIN + "api/HomePage/MsgAll" + "?cadv_ao=" + userInfo + "&cade_po=" + passWord,
                    new JsonListener() {
                        @Override
                        public void onJsonGet(int retcode, String extraMsg, JSONObject jsondata) {

                            ZMessage result = new ZMessage();
                            if (retcode != MyError.SUCCESS) {
                                result.errorcode = retcode;
                                result.errorMsg = extraMsg;
                                EventBus.getDefault().post(result);
                                return;
                            }

                            try {
                                JSONArray wJSONArray = jsondata.getJSONArray("list");
                                String wJson = wJSONArray.toString();
                                List<EXCMessage> wEXCMessageList = JSON.parseArray(wJson, EXCMessage.class);
                                if (wEXCMessageList != null) {
                                    result.EXCMessageList = wEXCMessageList;
                                }

                            } catch (JSONException ex) {
                                ex.printStackTrace();
                            }


                            EventBus.getDefault().post(result);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
