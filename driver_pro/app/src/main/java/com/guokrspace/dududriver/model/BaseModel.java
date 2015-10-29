/**
 * 基本Model类，根据反射打印对象信息
 * @author hyman
 *
 */
package com.guokrspace.dududriver.model;

import java.lang.reflect.Field;

public class BaseModel {
	
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
