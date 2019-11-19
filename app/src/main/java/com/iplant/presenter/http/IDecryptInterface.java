/**   
* @Title:         IDecryptInterface.java
* @Package        com.wasu.module.http
* @author         duhuanbiao
* @date           2014年10月24日 下午4:29:55 
* @Description:   
*/
package com.iplant.presenter.http;

import java.util.Map;

/**
 * 
 */
public abstract interface IDecryptInterface {
	public String decrypt(Map<String, String> header, byte[] data);
	public byte[] encrypt(byte[] data);
}
