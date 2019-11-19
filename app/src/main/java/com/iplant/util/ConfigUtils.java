package com.iplant.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ConfigUtils {
	
	/** 默认配置文件名称 **/
	private static final String DEFAULT_CONFIG_NAME = "wasu_config";

	/**获取配置项
	 * @param c 上下文环境
	 * @param configFile 配置文件名称，或者传null为默认
	 * @param Key 对应的key值
	 * @return
	 * 		字符串或者空
	 */
	public static String getString(Context c, String configFile, String Key){
		if (configFile == null){
			configFile = DEFAULT_CONFIG_NAME;
		}
		SharedPreferences sp = c.getSharedPreferences(configFile, 0);
		return sp.getString(Key, "");
	}
	
	/**获取配置项
	 * @param c 上下文环境
	 * @param configFile 配置文件名称，或者传null为默认
	 * @param Key 对应的key值
	 * @return
	 * 		-1或者对应数值
	 */
	public static long getLong(Context c, String configFile, String Key){
		if (configFile == null){
			configFile = DEFAULT_CONFIG_NAME;
		}
		SharedPreferences sp = c.getSharedPreferences(configFile, 0);
		return sp.getLong(Key, -1L);
	}
	
	/**获取配置项
	 * @param c 上下文环境
	 * @param configFile 配置文件名称，或者传null为默认
	 * @param Key 对应的key值
	 * @return
	 * 		-1或者对应数值
	 */
	public static int getInt(Context c, String configFile, String Key){
		if (configFile == null){
			configFile = DEFAULT_CONFIG_NAME;
		}
		SharedPreferences sp = c.getSharedPreferences(configFile, 0);
		return sp.getInt(Key, -1);
	}
	
	/**保存配置项
	 * @param c 上下文环境
	 * @param configFile 配置文件名称，或者传null为默认
	 * @param Key 对应的key值
	 * @param value 对应的value
	 */
	public static void saveData(Context c, String configFile, String Key, Object value){
		if (configFile == null){
			configFile = DEFAULT_CONFIG_NAME;
		}
		SharedPreferences sp = c.getSharedPreferences(configFile, 0);
		Editor edit = sp.edit();
		if (value instanceof String){
			edit.putString(Key, (String)value);
		}else if(value instanceof Long){
			edit.putLong(Key, (Long)value);
		}else if(value instanceof Integer){
			edit.putInt(Key, (Integer)value);
		}
		edit.commit();
	}
}
