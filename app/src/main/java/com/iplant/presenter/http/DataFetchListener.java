/**   
 * @Title:         DataFetchListener.java
 * @Package        com.wasu.module.datafetch
 * @author         duhuanbiao
 * @date           2014年9月18日 下午4:11:04 
 * @Description:   
 */
package com.iplant.presenter.http;

import org.json.JSONObject;

/**
 * 网络数据请求回调
 */
public interface DataFetchListener {
	/**
	 * 当请求需要返回一个ObjectBase实体类时的回调
	 */
	public interface ObjectListener {
		/**
		 * @param retcode
		 *            0标示成功，其它标示错误， @see com.wasu.common.WError
		 * @param extraMsg
		 *            : 如果出错，标示具体的错误信息
		 * @param data
		 *            :如果成功，返回实体类
		 */
		public void onObjectGet(int retcode, String extraMsg, ObjectBase data);
	}

	/**
	 * 当请求需要返回一个json实体类的回调
	 */
	public interface JsonListener {
		/**
		 * @param retcode
		 *            0标示成功，其它标示错误， @see com.wasu.common.WError
		 * @param extraMsg
		 *            如果出错，标示具体的错误信息
		 * @param jsondata
		 *            如果成功，返回jsoan类
		 */
		public void onJsonGet(int retcode, String extraMsg, JSONObject jsondata);
	}
}
