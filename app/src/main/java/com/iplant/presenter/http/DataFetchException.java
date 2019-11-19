/**   
 * @Title:         HttpException.java
 * @Package        com.wasu.module.http
 * @author         duhuanbiao
 * @date           2014年9月19日 下午3:29:52 
 * @Description:   
 */
package com.iplant.presenter.http;

/**
 * 网络数据异常
 */
public class DataFetchException extends Exception {
	/** 异常代码 **/
	public int code;

	/** 异常具体原因 **/
	public String extraMsg;

	/**
	 * @param code
	 *            异常代码
	 * @param errorMsg
	 *            异常具体原因
	 */
	public DataFetchException(int code, String errorMsg) {
		super(errorMsg);
		this.code = code;
		this.extraMsg = errorMsg;
	}
}
