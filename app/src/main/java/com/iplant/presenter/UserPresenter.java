package com.iplant.presenter;

import com.iplant.GudData;
import com.iplant.MyError;
import com.iplant.model.Account;
import com.iplant.model.ModelBase;
import com.iplant.presenter.db.DBManage;
import com.iplant.presenter.http.DataFetchListener.JsonListener;
import com.iplant.presenter.http.DataFetchModule;
import com.iplant.util.DesUtil;
import com.iplant.util.JsonBuilder;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UserPresenter {
	public static class ChangePwdResult extends ModelBase<ChangePwdResult>{
		private static final long serialVersionUID = -7165681305440217240L;
	}
	/**
	 * 用户登录
	 * @param account
	 * @param pwd
	 */
	public void login(final String account, final String pwd ,final String Mac){

		try {
			//构造登陆请求
			HashMap<String, String> headers = new HashMap<String, String>();
			headers.put("Content-Type", "application/json");

			Map<String, Object> body = new HashMap<String, Object>();
			body.put("user_id", account);
			String wToken="";
			if(false){
				String wT4=account.substring(0,account.length()/2);
				String wT2=account.substring(account.length()/2);
				String wT3= MessageFormat.format("{0}-{1}",
						String.valueOf(Calendar.getInstance().get(Calendar.YEAR)),Calendar.getInstance().get(Calendar.MONTH)) ;
				String wT5=  String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) ;
				String wT1=  MessageFormat.format("{0}:{1}:{2}",
						Calendar.getInstance().get(Calendar.HOUR),Calendar.getInstance().get(Calendar.MINUTE),Calendar.getInstance().get(Calendar.SECOND)) ;

				wToken=MessageFormat.format("{0}+-abc072-+{1}+-abc072-+{2}+-abc072-+{3}+-abc072-+{4}",
						wT1,wT2,wT3,wT4,wT5);

				wToken= DesUtil.encrypt(wToken);
				body.put("token", pwd);
			}


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
                            if (retcode != MyError.SUCCESS){
                                myAccount.errorcode = retcode;
                                myAccount.erorMsg   = extraMsg;
                            }else{
								try {
									myAccount.account = account;
									myAccount.password= jsondata.getJSONObject("info").optString("Password");
									myAccount.myID      = jsondata.getJSONObject("info").optString("LoginName");
									myAccount.name    = jsondata.getJSONObject("info").optString("Name");
									myAccount.role	  = jsondata.getJSONObject("info").optString("Position");
									myAccount.department = jsondata.getJSONObject("info").optString("Department");
									myAccount.companyID = jsondata.getJSONObject("info").optInt("CompanyID");
									myAccount.sex	  = "";
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
	
	/**
	 * 用户登出
	 */
	public void logout(String account){
		//删除当前账号
		try {
//			Account myAccount = DBManage.queryBy(Account.class, "account", account);
			DBManage.deleteBy(Account.class, "account", account);
            GudData.CustomMap.clear();
			//发送账号登出消息
//			DataFetchModule.getInstance().fetchJsonGet(GudData.DOMAIN + "api/pointer/logout?user_info=" + myAccount.myID, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 修改密码
	 * @param account
	 * @param oldPwd
	 * @param newPwd
	 */
	public void changePwd(String account, String oldPwd, String newPwd){
		//构造登陆请求
		try {

			HashMap<String, String> headers = new HashMap<String, String>();
			headers.put("Content-Type", "application/json");

			Map<String, Object> body = new HashMap<String, Object>();
			body.put("iD", account);
			body.put("password", newPwd);
			DataFetchModule.getInstance().fetchJsonPost(
                    GudData.DOMAIN + "api/user/pwdModify",
                    null,
                    headers,
                    JsonBuilder.buildPostBody(body),
                    new JsonListener() {
                        @Override
                        public void onJsonGet(int retcode, String extraMsg, JSONObject jsondata) {
                            ChangePwdResult result = new ChangePwdResult();
                            if (retcode != MyError.SUCCESS){
                                result.errorcode = retcode;
                                result.erorMsg   = extraMsg;
                            }
                            EventBus.getDefault().post(result);
                        }
                    }, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
