package com.iplant.util;

import java.util.Map;

import org.json.JSONStringer;

public class JsonBuilder {
	/**
	 * 组装body
	 * 
	 * @param params
	 * @return
	 */
	public static byte[] buildPostBody(Map<String, Object> params) {
		if (params == null) {
			return null;
		}

		JSONStringer jsBody = new JSONStringer();
		try {
			jsBody.object();
			for (String key : params.keySet()) {
				jsBody.key(key).value(params.get(key));
			}
			jsBody.endObject();
			return jsBody.toString().getBytes("utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
