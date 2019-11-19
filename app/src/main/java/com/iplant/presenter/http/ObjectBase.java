/**   
 * @Title:         ObjectBase.java
 * @Package        com.wasu.module.datafetch
 * @author         duhuanbiao
 * @date           2014年9月18日 下午4:12:33 
 * @Description:   
 */
package com.iplant.presenter.http;

import java.io.Serializable;

/**
 * 网络获取实体类的基类， 需要实现createFromResponse
 */
public abstract class ObjectBase implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 用于解析格式，由调用者提供
	 * 
	 * @param response
	 *             服务端返回的数据
	 */
	public abstract void createFromResponse(String response)
			throws DataFetchException;
}
