/**
 * 网络请求
 */
package com.iplant.presenter.http;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;
import com.iplant.MyError;

import android.content.Context;

import java.io.EOFException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * 用于网络请求，是对第三方框架velley的封装
 */
public class HttpRequestModule {
	private final static String tag = HttpRequestModule.class.getSimpleName();
	
	/** 网络引擎实现类 **/
	private RequestQueue mRequestQueue;

	private RequestParams.RequestListener mDefaultListener;
	
	/** 用于记录该模块是否被初始化， 没有被初始化的模块将无法使用 **/
	private boolean mbIsInited = false;

	/*******************************************************************************************/
	/**
	 * single instance
	 */
	private volatile static HttpRequestModule g_Instance;

	/**
	 * 获取单例
	 * 
	 * @return HttpRequestModule单例
	 */
	public static HttpRequestModule getInstance() {
		if (g_Instance == null) {
			synchronized (HttpRequestModule.class) {
				if (g_Instance == null) {
					g_Instance = new HttpRequestModule();
				}
			}
		}
		return g_Instance;
	}

	/**
	 * 模块初始化
	 * 
	 * @param c
	 *            上下文环境
	 */
	public void init(Context c) {
		if (mbIsInited) {
			return;
		}

		mbIsInited = true;
		mRequestQueue = Volley.newRequestQueue(c);
		mRequestQueue.start();
	}

	public void unInit() {
		if (!mbIsInited) {
			return;
		}
		mRequestQueue.stop();
	}

	/**
	 * 增加请求任务，数据会以内部回调的方式返回
	 * 
	 * @param requestParam
	 *            请求参数
	 * @see com.wasu.module.http。httpRequestParams
	 */
	public void addTask(RequestParams requestParam) {
		StringRequest request = new StringRequest(requestParam);
		mRequestQueue.add(request);
	}

	/**
	 * 取消某一类型数据
	 * 
	 * @param tag
	 *            发送请求时RequestParams带的标记
	 */
	public void cancelTask(Object tag) {
		mRequestQueue.cancelAll(tag);
	}

	/**
	 * 用于构建一个带参数的uri， 类似于urlencode,对于中文字符做16进制处理
	 * 
	 * @param url
	 *            元素的地址
	 * @param params
	 *            成对的参数
	 * @return 完整的url, 如http://www.test.com/s?wd=%E6%96%B0%E6%B5%AA
	 */
	public static String composeUri(String url, Map<String, String> params) {
		if (url == null || params == null) {
			return url;
		}

		if (url.indexOf("?") > 0) {
			return url + "&" + encodeParameters(params, "utf-8");
		} else {
			return url + "?" + encodeParameters(params, "utf-8");
		}
	}

	/**
	 * 设置默认的处理函数
	 * 
	 * @param l
	 *            默认处理回调
	 */
	public void setDefaultResponseListener(RequestParams.RequestListener l) {
		mDefaultListener = l;
	}

	/** ------------------------内部实现------------------------------------------- **/
	/**
	 * 接口实现类
	 * 
	 * 传入的context降会全局存在
	 * **/
	protected HttpRequestModule() {
	}

	/**
	 * Converts <code>params</code> into an application/x-www-form-urlencoded
	 * encoded string.
	 */
	private static String encodeParameters(Map<String, String> params,
			String paramsEncoding) {
		StringBuilder encodedParams = new StringBuilder();
		try {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				if (entry.getKey() == null || entry.getValue() == null){
					continue;
				}
				encodedParams.append(URLEncoder.encode(entry.getKey(),
						paramsEncoding));
				encodedParams.append('=');
				encodedParams.append(URLEncoder.encode(entry.getValue(),
						paramsEncoding));
				encodedParams.append('&');
			}
			return encodedParams.toString();
		} catch (UnsupportedEncodingException uee) {
		}
		return "";
	}

	/**
	 * 内部封装类
	 * 
	 */
	private class StringRequest extends Request<String> {
		private RequestParams mRequestParam;

		private String contentType = null; 
		public StringRequest(RequestParams request) {
			this(request.getMethod(), request.getUrl(), null);
			this.mRequestParam = request;

			//parse content type
			if (request.getHeaders() != null && request.getHeaders().containsKey("Content-Type")){
				contentType = request.getHeaders().get("Content-Type");
				request.getHeaders().remove("Content-Type");
			}
			
			if (request.getTag() != null) {
				setTag(request.getTag());
			}

			setRetryPolicy(new DefaultRetryPolicy(request.getTimeOut(),
					request.getRetries(), 1.0f));

			setShouldCache(request.isCached());
		}

		private StringRequest(int method, String url, ErrorListener listener) {
			super(method, url, listener);
		}

		@Override
		public Map<String, String> getHeaders() throws AuthFailureError {
			if (mRequestParam.getHeaders() == null) {
				return super.getHeaders();
			}
			return mRequestParam.getHeaders();
		}
		
		/* (non-Javadoc)
		 * @see com.wasu.thirdparty.net.Request#getBodyContentType()
		 */
		@Override
		public String getBodyContentType() {
			if (contentType != null){
				return contentType;
			}
			
			return super.getBodyContentType();
		}

		@Override
		public byte[] getBody() throws AuthFailureError {
			if (mRequestParam.getBody() != null) {
				return mRequestParam.getBody();
			}
			return super.getBody();
		}

		@Override
		protected Response<String> parseNetworkResponse(NetworkResponse response) {
			try {
				String strResponseData = null;
                if (response.data.length > 1 * 1024 * 1024/** 大于1M的数据会引起溢出，直接不处理 **/){
                    Response.error(new VolleyError("return length overflow, max in 1M"));
                }
				
				if(mRequestParam.getEncryptImpl() != null){
					strResponseData = mRequestParam.getEncryptImpl().decrypt(response.headers, response.data);
				}else{
					strResponseData = new String(response.data, "utf-8");
				}
				
				return Response.success(strResponseData,
						HttpHeaderParser.parseCacheHeaders(response));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return Response.error(new ParseError(e));
			} catch (Exception je) {
				je.printStackTrace();
				return Response.error(new ParseError(je));
			}
		}

		@Override
		protected void deliverResponse(String response) {
			boolean bProcessed = false;
			if (mRequestParam.getRequestListener() != null) {
				bProcessed = mRequestParam.getRequestListener().onResponse(
						MyError.SUCCESS, response, 0, null);
			}

			if (!bProcessed && mDefaultListener != null) {
				mDefaultListener.onResponse(MyError.SUCCESS, response, 0,
						null);
			}
			mRequestParam = null;
		}

		@Override
		public void deliverError(VolleyError error) {
			boolean bProcessed = false;
			
			int errorCode = MyError.DISCONNECT;
			if(error instanceof TimeoutError){
				errorCode = MyError.TIMEOUT;
			}else if(error.networkResponse == null){
				if (error.getCause() != null){
					if (error.getCause() instanceof UnsupportedEncodingException){
						errorCode = MyError.INVALID_DATA;
					}else if(error.getCause() instanceof UnknownHostException){
						errorCode = MyError.DNS_RESOLVE;
					}else if(error.getCause() instanceof EOFException){
						errorCode = MyError.DISCONNECT;
					}
				}
			}else{
				errorCode = error.networkResponse.statusCode;
			}
			
			if (mRequestParam.getRequestListener() != null) {
                bProcessed = mRequestParam.getRequestListener()
                        .onResponse(errorCode, null, 0, null);
			}

			if (!bProcessed && mDefaultListener != null) {
				mDefaultListener.onResponse(errorCode, error.getMessage(), 0, null);
			}
			
			mRequestParam = null;
		}
	}
}
