package com.guokrspace.duducar.util;

/**
 * Created by hyman on 2015-08-05.
 */

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;
import java.util.Set;

/**
 * SharedPreferences的一个工具类，调用setParam就能保存String, Integer, Boolean, Float,
 * Long类型的参数 同样调用getParam就能获取到保存在手机里面的数据
 * 
 * @author Hyman
 * 
 */
public class SharedPreferencesUtils {
	/**
	 * 保存在手机里面的文件名
	 */
	private static final String FILE_NAME = "duducar_prefs";


	//一些字段名常量
	public static final String OUT_TRADE_NO = "tradeNo";

	public static final String COMFIRM_TRADE_RESULT_SID = "tradeSid";

	/**
	 * 保存单个数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
	 * 
	 * @param context
	 * @param fileName
	 * @param key
	 * @param data
	 */
	public static void setParam(Context context, String fileName, String key,
			Object data) {
		if (fileName == null) {
			fileName = FILE_NAME;
		}

		String type = data.getClass().getSimpleName();
		SharedPreferences sp = context.getSharedPreferences(fileName,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();

		if ("String".equals(type)) {
			editor.putString(key, (String) data);
		} else if ("Integer".equals(type)) {
			editor.putInt(key, (Integer) data);
		} else if ("Boolean".equals(type)) {
			editor.putBoolean(key, (Boolean) data);
		} else if ("Float".equals(type)) {
			editor.putFloat(key, (Float) data);
		} else if ("Long".equals(type)) {
			editor.putLong(key, (Long) data);
		}

		editor.commit();
	}

	public static void setParam(Context context, String key, Object data) {
		setParam(context, null, key, data);
	}

	/**
	 * 批量保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
	 * 
	 * @param context
	 */
	public static void setParams(Context context, String fileName,
			Map<String, Object> datas) {
		if (fileName == null) {
			fileName = FILE_NAME;
		}
		SharedPreferences sp = context.getSharedPreferences(fileName,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();

		Set<String> keys = datas.keySet();
		for (String key : keys) {
			Object data = datas.get(key);
			String type = data.getClass().getSimpleName();
			if ("String".equals(type)) {
				editor.putString(key, (String) data);
			} else if ("Integer".equals(type)) {
				editor.putInt(key, (Integer) data);
			} else if ("Boolean".equals(type)) {
				editor.putBoolean(key, (Boolean) data);
			} else if ("Float".equals(type)) {
				editor.putFloat(key, (Float) data);
			} else if ("Long".equals(type)) {
				editor.putLong(key, (Long) data);
			}
		}

		editor.commit();
	}

	public static void setParams(Context context, Map<String, Object> datas) {
		setParams(context, null, datas);
	}

	/**
	 * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
	 * @param context
	 * @param fileName
	 * @param key
	 * @param defaultObject
	 * @return
	 */
	public static Object getParam(Context context, String fileName, String key,
			Object defaultObject) {
		if (fileName == null)
			fileName = FILE_NAME;
		
		String type = defaultObject.getClass().getSimpleName();
		SharedPreferences sp = context.getSharedPreferences(fileName,
				Context.MODE_PRIVATE);

		if ("String".equals(type)) {
			return sp.getString(key, (String) defaultObject);
		} else if ("Integer".equals(type)) {
			return sp.getInt(key, (Integer) defaultObject);
		} else if ("Boolean".equals(type)) {
			return sp.getBoolean(key, (Boolean) defaultObject);
		} else if ("Float".equals(type)) {
			return sp.getFloat(key, (Float) defaultObject);
		} else if ("Long".equals(type)) {
			return sp.getLong(key, (Long) defaultObject);
		}

		return null;
	}

	public static Object getParam(Context context, String key, Object defaultObject) {
		return getParam(context, null, key, defaultObject);
	}

	/**
	 * 清除数据
	 * 
	 * @param context
	 * @param fileName
	 */
	public static void clearEditor(Context context, String fileName) {
		if (fileName == null)
			fileName = FILE_NAME;
		SharedPreferences sp = context.getSharedPreferences(fileName,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.clear();
		editor.commit();
	}

	public static void clearEditor(Context context) {
		clearEditor(context, null);
	}

	/**
	 * 刪除某个数据
	 * 
	 * @param context
	 * @param key
	 */
	public static void removeParam(Context context, String fileName, String key) {
		if (fileName == null)
			fileName = FILE_NAME;
		SharedPreferences sp = context.getSharedPreferences(fileName,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.remove(key);
		editor.commit();
	}

	public static void removeParm(Context context, String key) {
		removeParam(context, null, key);
	}
}
