package com.iplant;

/**
 * 定义基本的错误类型
 */
public class MyError {
	/************************************************************************
	 * 基础定义
	 * **********************************************************************/	
	/** 没有错误 **/
	public final static int SUCCESS			= 0;
	/** 未知错误 **/
	public final static int UNKNOWN		= 1;
	/** 不支持 **/
	public final static int UNSUPPORT		= 2;
	/** 无效的数据 **/
	public final static int INVALID_DATA	= 3;
	/** 未授权**/
	public final static int UNAUTH		= 4; 
	/** 网络统一错误**/
	public static final int NET 			= 5;
	/** 超时 **/
	public final static int TIMEOUT		= 6;	
	/** 文件读写失败 **/
	public final static int IO			= 7;	
	/** 内存不足 **/
	public final static int MEM_NOT_ENGOUGH = 8;
	/** 调用状态错误 **/
	public static final int NOT_READY 	= 9;
	/** 异步操作，结果会通过回调调用 **/
	public static final int ASYNC_PROC 	= 10;
	/** 加解密失败 **/
	public static final int ENCRYPT 		= 11;
	/** http地址无法解析**/
	public static final int DNS_RESOLVE 	= 12;
	/** 连接中断 **/
	public final static int DISCONNECT	= 13; 
	/** 无效的输入 **/
	public final static int INVALID_PARAM	= 14; 	
	/** 服务端错误 **/
	public final static int SERVER	= 15; 	
}
