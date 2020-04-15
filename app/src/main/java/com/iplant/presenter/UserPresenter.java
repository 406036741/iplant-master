package com.iplant.presenter;

import com.iplant.GudData;
import com.iplant.MyError;
import com.iplant.model.Account;
import com.iplant.model.EXCMessage;
import com.iplant.model.ModelBase;
import com.iplant.presenter.db.DBManage;
import com.iplant.presenter.http.DataFetchListener.JsonListener;
import com.iplant.presenter.http.DataFetchModule;
import com.iplant.util.JsonBuilder;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserPresenter {
    public static class ChangePwdResult extends ModelBase<ChangePwdResult> {
        private static final long serialVersionUID = -7165681305440217240L;
    }

    /**
     * 用户登录
     *
     * @param account
     * @param pwd
     */
    public void login(final String account, final String pwd, final String Mac, final String wTaken) {

        try {
            //构造登陆请求
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");

            Map<String, Object> body = new HashMap<String, Object>();
            body.put("user_id", account);
            body.put("token", wTaken);
            body.put("passWord", pwd);
            body.put("PhoneMac", Mac);
            DataFetchModule.getInstance().fetchJsonPost(
                    GudData.DOMAIN + "api/User/Login",
                    null,
                    headers,
                    JsonBuilder.buildPostBody(body),
                    new JsonListener() {

                        @Override
                        public void onJsonGet(int retcode, String extraMsg, JSONObject jsondata) {
                            Account myAccount = new Account();
                            if (retcode != MyError.SUCCESS) {
                                myAccount.errorcode = retcode;
                                myAccount.errorMsg = extraMsg;
                            } else {
                                try {
                                    myAccount.account = account;
                                    myAccount.encryptPwd = jsondata.getJSONObject("info").optString("Password");
                                    myAccount.encryptAccount = jsondata.getJSONObject("info").optString("LoginName");
                                    myAccount.name = jsondata.getJSONObject("info").optString("Name");
                                    myAccount.role = jsondata.getJSONObject("info").optString("Position");
                                    myAccount.department = jsondata.getJSONObject("info").optString("Department");
                                    myAccount.companyID = jsondata.getJSONObject("info").optInt("CompanyID");
                                    myAccount.ADUserID =  jsondata.getJSONObject("info").optString("LoginID");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    myAccount.insertOrUpdate();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                            EventBus.getDefault().post(myAccount);
                        }
                    }, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void GetMessageState(final String account, final String pwd, List<EXCMessage> wEXCMessageList) {

        try {
            //构造登陆请求
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");

            Map<String, Object> body = new HashMap<String, Object>();
            body.put("user_id", account);
            body.put("data", wEXCMessageList);
            body.put("passWord", pwd);

            DataFetchModule.getInstance().fetchJsonPost(
                    GudData.DOMAIN + "api/EXCAndon/UpdateMessage",
                    null,
                    headers,
                    JsonBuilder.buildPostBody(body),
                    new JsonListener() {

                        @Override
                        public void onJsonGet(int retcode, String extraMsg, JSONObject jsondata) {

                        }
                    }, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 用户登出
     */
    public void logout(String account) {
        //删除当前账号
        try {
//			Account myAccount = DBManage.queryBy(Account.class, "account", account);
            DBManage.deleteBy(Account.class, GudData.KEY_Account, account);

            GudData.CustomMap.clear();
            //发送账号登出消息
//			DataFetchModule.getInstance().fetchJsonGet(GudData.DOMAIN + "api/pointer/logout?user_info=" + myAccount.encryptAccount, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改密码
     *
     * @param account
     * @param oldPwd
     * @param newPwd
     */
    public void changePwd(String account, String oldPwd, String newPwd) {
        //构造登陆请求
        try {

            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");

            Map<String, Object> body = new HashMap<String, Object>();
            body.put("iD", account);
            body.put("encryptPwd", newPwd);
            DataFetchModule.getInstance().fetchJsonPost(
                    GudData.DOMAIN + "api/user/pwdModify",
                    null,
                    headers,
                    JsonBuilder.buildPostBody(body),
                    new JsonListener() {
                        @Override
                        public void onJsonGet(int retcode, String extraMsg, JSONObject jsondata) {
                            ChangePwdResult result = new ChangePwdResult();
                            if (retcode != MyError.SUCCESS) {
                                result.errorcode = retcode;
                                result.errorMsg = extraMsg;
                            }
                            EventBus.getDefault().post(result);
                        }
                    }, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
