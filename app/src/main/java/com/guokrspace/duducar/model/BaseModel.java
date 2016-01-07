/**
 * 基本Model类，根据反射打印对象信息
 * @author hyman
 *
 */
package com.guokrspace.duducar.model;

import java.lang.reflect.Field;

public class BaseModel {

//	public int status;//1，成功；-1失败
//
//	public String msg;

	public String modelToString() {
		String s = "";
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			try {
				field.setAccessible(true);
				if (field.get(this) != null) {
					s += field.getName() + "=" + field.get(this).toString()
							+ "|";
				} else {
					s += field.getName() + "=null|";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return s;
	}

}
