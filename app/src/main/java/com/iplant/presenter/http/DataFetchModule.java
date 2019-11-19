/**   
 * @Title:         DataFetchModule.java
 * @Package        com.wasu.module.datafetch
 * @author         duhuanbiao
 * @date           2014年9月18日 下午3:55:03 
 *   
 */
package com.iplant.presenter.http;

import java.util.Map;

import org.json.JSONObject;

import com.iplant.MyError;
import com.iplant.presenter.http.DataFetchListener.JsonListener;
import com.iplant.presenter.http.DataFetchListener.ObjectListener;

import android.content.Context;

/**
 * 对网络模块的进一步封装，对于请求，直接返回一个实体类
 */
public class DataFetchModule{
	private final String tag = DataFetchModule.class.getSimpleName();
	
	protected DataFetchModule() {}

	/** 单例模式 **/
	private static DataFetchModule g_Instance;

	/** 用于记录该模块是否被初始化， 没有被初始化的模块将无法使用 **/
	private boolean mbIsInited = false;
	
	/*----------------------------------对外接口----------------------------------------------*/
	/**
	 * 获取类的单例
	 * 
	 * @return DataFetchModule单例
	 */
	public static DataFetchModule getInstance() {
		synchronized (DataFetchModule.class) {
			if (g_Instance == null) {
				g_Instance = new DataFetchModule();
			}

			return g_Instance;
		}
	}

	/**
	 * 模块的初始化， 使用前必须调用， 该模块需要依赖MsgQueueModule和HttpRequestModule 
	 * @see com.wasu.module.msgqueue.MsgQueueModule
	 * @see com.wasu.module.http.HttpRequestModule
	 * 
	 * @param context
	 *            上下文环境
	 */
	public void init(Context c) {
		if (mbIsInited) {
			return;
		}

		mbIsInited = true;
		HttpRequestModule.getInstance().init(c);
	}

	/**
	 * 简化版本
	 * @param url
	 *            请求的url
	 * @param clazz
	 *            解析返回的数据的类
	 * @param callback
	 * 			  数据获取后的回调
	 * @param decryptImpl
	 * 			 加密实现
	 */
	public void fetchObjectGet(String url,
			final Class<? extends ObjectBase> clazz, ObjectListener callback, IDecryptInterface decryptImpl) {
		processHttpRequest(RequestParams.Method.GET, type_object, url, null,
				null, null, clazz, callback, decryptImpl);
	}
	
	/**
	 * 简化版本
	 * @param url
	 *            请求的url
	 * @param clazz
	 *            解析返回的数据的类
	 * @param callback
	 * 			  数据获取后的回调
	 */
	public void fetchObjectGet(String url,
			final Class<? extends ObjectBase> clazz, ObjectListener callback){
		fetchObjectGet(url, clazz, callback, null);
	}

	/**
	 * 简化版本
	 * @param url
	 *            请求的url
	 * @param clazz
	 *            解析返回的数据的类
	 * @param callback
	 * 			      数据获取后的回调
	 * @param decryptImpl
	 * 			 加密实现
	 */
	public void fetchJsonGet(String url, JsonListener callback, IDecryptInterface decryptImpl) {
		processHttpRequest(RequestParams.Method.GET, type_json, url, null,
				null, null, null, callback, decryptImpl);
	}
	
	/**
	 * 简化版本
	 * @param url
	 *            请求的url
	 * @param clazz
	 *            解析返回的数据的类
	 * @param callback
	 * 			      数据获取后的回调
	 */
	public void fetchJsonGet(String url, JsonListener callback) {
		fetchJsonGet(url, callback, null);
	}

	/**
	 * 发送http get请求
	 * @param url
	 *            请求的url
	 * @param param
	 *            请求的头参数
	 * @param headers
	 *            请求的header参数
	 * @param clazz
	 *            解析的类
	 * @param msgid
	 *            发送消息的id， 不需要填-1
	 * @param callback
	 *            接收的回调
	 * @param decryptImpl
	 * 			 加密实现
	 */
	public void fetchObjectGet(String url, Map<String, String> param,
			Map<String, String> headers, Class<? extends ObjectBase> clazz,
			ObjectListener callback, IDecryptInterface decryptImpl) {
		processHttpRequest(RequestParams.Method.GET, type_object, url, param,
				headers, null, clazz, callback, decryptImpl);
	}
	
	/**
	 * 发送http get请求
	 * @param url
	 *            请求的url
	 * @param param
	 *            请求的头参数
	 * @param headers
	 *            请求的header参数
	 * @param clazz
	 *            解析的类
	 * @param msgid
	 *            发送消息的id， 不需要填-1
	 * @param callback
	 *            接收的回调
	 */
	public void fetchObjectGet(String url, Map<String, String> param,
			Map<String, String> headers, Class<? extends ObjectBase> clazz,
			ObjectListener callback) {
		fetchObjectGet(url, param, headers, clazz, callback, null);
	}

	/**
	 * 发送http post请求
	 * @param url
	 *            请求的url
	 * @param param
	 *            请求的头参数
	 * @param headers
	 *            请求的header参数
	 * @param clazz
	 *            解析的类
	 * @param msgid
	 *            发送消息的id， 不需要填-1
	 * @param callback
	 *            接收的回调
	 * @param decryptImpl
	 * 			 加密实现
	 */
	public void fetchObjectPost(String url, Map<String, String> param,
			Map<String, String> headers, byte[] body,
			Class<? extends ObjectBase> clazz,
			ObjectListener callback, IDecryptInterface decryptImpl) {
		processHttpRequest(RequestParams.Method.POST, type_object, url, param,
				headers, body, clazz, callback, decryptImpl);
	}
	
	/**
	 * 发送http post请求
	 * @param url
	 *            请求的url
	 * @param param
	 *            请求的头参数
	 * @param headers
	 *            请求的header参数
	 * @param clazz
	 *            解析的类
	 * @param msgid
	 *            发送消息的id， 不需要填-1
	 * @param callback
	 *            接收的回调
	 */
	public void fetchObjectPost(String url, Map<String, String> param,
			Map<String, String> headers, byte[] body,
			Class<? extends ObjectBase> clazz,
			ObjectListener callback) {
		fetchObjectPost(url, param, headers, body, clazz, callback, null);
	}

	/**
	 * 发送http get请求
	 * @param url
	 *            请求的url
	 * @param param
	 *            请求的头参数
	 * @param headers
	 *            请求的header参数
	 * @param msgid
	 *            发送消息的id， 不需要填-1
	 * @param callback
	 *            接收的回调
	 * @param decryptImpl
	 * 			 加密实现
	 */
	public void fetchJsonGet(String url, Map<String, String> param,
			Map<String, String> headers, JsonListener callback, IDecryptInterface decryptImpl) {
		processHttpRequest(RequestParams.Method.GET, type_json, url, param,
				headers, null, null, callback, decryptImpl);
	}
	
	/**
	 * 发送http get请求
	 * @param url
	 *            请求的url
	 * @param param
	 *            请求的头参数
	 * @param headers
	 *            请求的header参数
	 * @param msgid
	 *            发送消息的id， 不需要填-1
	 * @param callback
	 *            接收的回调
	 */
	public void fetchJsonGet(String url, Map<String, String> param,
			Map<String, String> headers, JsonListener callback) {
		fetchJsonGet(url, param, headers, callback, null);
	}

	/**
	 * 发送http post请求
	 * @param url
	 *            请求的url
	 * @param param
	 *            请求的头参数
	 * @param headers
	 *            请求的header参数
	 * @param msgid
	 *            发送消息的id， 不需要填-1
	 * @param callback
	 *            接收的回调
	 */
	public void fetchJsonPost(String url, Map<String, String> param,
			Map<String, String> headers, byte[] body, 
			JsonListener callback, IDecryptInterface decryptImpl) {
		processHttpRequest(RequestParams.Method.POST, type_json, url, param,
				headers, body, null, callback, decryptImpl);
	}
	
	/**
	 * 发送http post请求
	 * @param url
	 *            请求的url
	 * @param param
	 *            请求的头参数
	 * @param headers
	 *            请求的header参数
	 * @param msgid
	 *            发送消息的id， 不需要填-1
	 * @param callback
	 *            接收的回调
	 */
	public void fetchJsonPost(String url, Map<String, String> param,
			Map<String, String> headers, byte[] body, 
			JsonListener callback) {
		fetchJsonPost(url, param, headers, body, callback, null);
	}

	/**-------------------------------内部实现------------------------------**/
	protected void doNotify(int retcode, String extraMsg, ObjectBase data,
			ObjectListener callback) {
		if (callback != null) {
			callback.onObjectGet(retcode, extraMsg, data);
		}
	}

	protected void doNotify(int retcode, String extraMsg, JSONObject data,
			JsonListener callback) {
		if (callback != null) {
			callback.onJsonGet(retcode, extraMsg, data);
		}
	}

	protected final static int type_object = 1;
	protected final static int type_json = 2;

	protected void processHttpRequest(int requestType, final int processtype,
			String url, Map<String, String> param, Map<String, String> headers,
			byte[] body, final Class<? extends ObjectBase> clazz,
			final Object callback, IDecryptInterface decryptImpl) {
		/** 组合url **/
		url = HttpRequestModule.composeUri(url, param);
		RequestParams requestParam = new RequestParams(requestType, url,
				headers, body, new RequestParams.RequestListener() {
					@Override
					public boolean onResponse(int errorCode, String data,
							int arg1, Object extra) {
						if (processtype == type_object) {
							if (errorCode != 0) {
								doNotify(errorCode, data, null,
										(ObjectListener) callback);
								return true;
							}
							try {
								ObjectBase myObject = clazz.newInstance();
								myObject.createFromResponse(data);
								doNotify(errorCode, null, myObject,
										(ObjectListener) callback);
							} catch (DataFetchException e) {
								e.printStackTrace();
								doNotify(e.code, e.extraMsg, null,
										(ObjectListener) callback);
							} catch (Exception e) {
								e.printStackTrace();
								doNotify(MyError.NET, e.toString(),null, (ObjectListener) callback);
							}
							return true;
						} else if (processtype == type_json) {
							if (errorCode != 0) {
								doNotify(errorCode, data, null,
										(JsonListener) callback);
								return true;
							}
							try {
								JSONObject myObject = new JSONObject(data);
								
								//适应iplant项目格式
								if (myObject.has("resultCode") && myObject.has("returnObject")){
									int resultCode = myObject.getInt("resultCode");
									JSONObject returnObject = myObject.getJSONObject("returnObject");
									if (resultCode != 1000){
										errorCode = MyError.SERVER;
										data = returnObject.optString("msg");
									}else{
										errorCode = MyError.SUCCESS;
										myObject = returnObject;
									}
								}
								
								doNotify(errorCode, null, myObject,
										(JsonListener) callback);
							} catch (Exception e) {
								e.printStackTrace();
								doNotify(MyError.INVALID_DATA, 
										e.toString(), null, (JsonListener) callback);
							}
							return true;
						}

						return true;
					}
				});

		requestParam.setEncryptImpl(decryptImpl);
		HttpRequestModule.getInstance().addTask(requestParam);
	}

}
