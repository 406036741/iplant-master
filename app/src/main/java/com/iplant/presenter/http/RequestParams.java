package com.iplant.presenter.http;

import java.util.HashMap;
import java.util.Map;

public class RequestParams {
	public interface Method {
		/**
		 * This is the deprecated way that needs to be handled for backwards
		 * compatibility. If the request's post body is null, then the
		 * assumption is that the request is GET. Otherwise, it is assumed that
		 * the request is a POST.
		 **/
		int DEPRECATED_GET_OR_POST = -1;
		/** http get **/
		int GET = 0;
		/** http post **/
		int POST = 1;
		/** rest put **/
		int PUT = 2;
		/** rest delete **/
		int DELETE = 3;
		/** rest head **/
		int HEAD = 4;
		/** rest option **/
		int OPTIONS = 5;
		int TRACE = 6;
		int PATCH = 7;
	}

	/** The default socket timeout in milliseconds */
	private static final int DEFAULT_TIMEOUT_MS = 5000; /** 5秒超时 **/

	/** The default number of retries */
	private static final int DEFAULT_MAX_RETRIES = 1;

	/**
	 * 网络请求的回调
	 */
	public static interface RequestListener {
		/**
		 * 处理回调函数
		 * 
		 * @param errorCode
		 * @param data
		 * @param arg1
		 * @param extra
		 * @return TRUE : 标示已经处理 FALSE : 标示没有处理，如果有默认处理函数，就交给默认处理函数
		 */
		public boolean onResponse(int errorCode, String data, int arg1,
				Object extra);
	}

	/** 请求方式，默认为GET **/
	private int mMethod = Method.GET;

	/** 地址，不带参数 **/
	private String mUrl;

	private RequestListener mRequestListener;

	/** 请求的HEADER部分 **/
	private Map<String, String> mHeaders;

	/** POST等的body信息 **/
	private byte[] mBody;

	/** The default socket timeout in milliseconds */
	private int mTimeOut = DEFAULT_TIMEOUT_MS;

	/** The default number of retries */
	private int mRetries = DEFAULT_MAX_RETRIES;

	/** 设置该请求是否需要cache **/
	private boolean bCached = true;

	/** tag，用于标示当前请求 **/
	private Object mTag;
	
	/** 用于文件加密 **/
	private IDecryptInterface mDecryptImpl;

	/**
	 * 构造请求类
	 * 
	 * @param url
	 *            请求地址
	 * @param header
	 *            请求的header部分
	 * @param listener
	 *            收到数据后的回调
	 */
	public RequestParams(String url, Map<String, String> header,
			RequestListener listener) {
		this.mUrl = url;
		if (header != null){
			this.mHeaders = new HashMap<String, String>(header);
		}else{
			this.mHeaders = null;
		}
		
		this.mRequestListener = listener;
	}

	/**
	 * 构造请求类
	 * 
	 * @param medthod
	 *            请求方式， @see Method
	 * @param url
	 *            请求地址
	 * @param header
	 *            请求的header部分
	 * @param listener
	 *            收到数据后的回调
	 */
	public RequestParams(int medthod, String url, Map<String, String> header,
			RequestListener listener) {
		this(url, header, listener);
		this.mMethod = medthod;
	}

	/**
	 * 构造请求类
	 * 
	 * @param medthod
	 *            请求方式， @see Method
	 * @param url
	 *            请求地址
	 * @param header
	 *            请求的header部分
	 * @param body
	 *            请求的body部分
	 * @param listener
	 *            收到数据后的回调
	 */
	public RequestParams(int medthod, String url, Map<String, String> header,
			byte[] body, RequestListener listener) {
		this(url, header, listener);
		this.mMethod = medthod;
		this.mBody = body;
	}

	/**
	 * @param tag
	 *            唯一标明该次请求的标识
	 * @param medthod
	 *            请求方式， @see Method
	 * @param url
	 *            请求地址
	 * @param header
	 *            请求的header部分
	 * @param listener
	 *            收到数据后的回调
	 */
	public RequestParams(Object tag, int medthod, String url,
			Map<String, String> header, RequestListener listener) {
		this(medthod, url, header, listener);
		this.mTag = tag;
	}

	/**
	 * 设置请求超时和重试机制， 默认为10秒， 重试3次
	 * 
	 * @param TimeOut
	 *            : 超时时间， 单位为毫秒， -1标示默认
	 * @param Retries
	 *            : 重试次数，-1标示默认
	 */
	public void setRequestPolicy(int TimeOut, int Retries) {
		if (TimeOut < 0) {
			this.mTimeOut = DEFAULT_TIMEOUT_MS;
		} else {
			this.mTimeOut = TimeOut;
		}

		if (Retries < 0) {
			this.mRetries = DEFAULT_MAX_RETRIES;
		} else {
			this.mRetries = Retries;
		}
	}

	/**
	 * 获取当前使用的请求方式
	 * 
	 * @return 当前请求方式
	 */
	public int getMethod() {
		return mMethod;
	}

	/**
	 * 获取当前请求地址
	 * 
	 * @return 当前请求地址
	 */
	public String getUrl() {
		return mUrl;
	}

	/**
	 * 获取当前请求的返回回调
	 * 
	 * @return 请求返回回调
	 */
	public RequestListener getRequestListener() {
		return mRequestListener;
	}

	/**
	 * 获取请求头
	 * 
	 * @return 当次请求的头信息
	 */
	public Map<String, String> getHeaders() {
		return mHeaders;
	}

	/**
	 * 当初请求的标识
	 * 
	 * @return
	 * 	当次请求标识
	 */
	public Object getTag() {
		return mTag;
	}

	/**
	 * 设置当前请求标识
	 * 
	 * @param tag
	 *            标识
	 */
	public void setTag(Object tag) {
		mTag = tag;
	}

	/**
	 * 获取当前超时时间
	 * 
	 * @return 返回当次请求的超时时间，单位为毫秒
	 */
	public int getTimeOut() {
		return mTimeOut;
	}

	/**
	 * 获取当次请求的重试次数
	 * 
	 * @return 重试次数
	 */
	public int getRetries() {
		return mRetries;
	}

	/**
	 * 查询是否当次请求的返回数据需要在底层 cache
	 * 
	 * @return 是否标记为底层cache
	 */
	public boolean isCached() {
		return bCached;
	}

	/**
	 * 设置当次请求的返回数据需要在底层 cache
	 * 
	 * @param bCached
	 *            是否标记为底层cache
	 */
	public void setCached(boolean bCached) {
		this.bCached = bCached;
	}

	/**
	 * 获取当次请求的body数据
	 * 
	 * @return 当次请求body数据
	 */
	public byte[] getBody() {
		return mBody;
	}
	
	/**
	 * 设置加密实现
	 * @param l
	 */
	public void setEncryptImpl(IDecryptInterface l){
		this.mDecryptImpl = l;
	}
	
	/**
	 * 获取加密实现
	 * @return
	 */
	public IDecryptInterface getEncryptImpl(){
		return mDecryptImpl;
	}
}
