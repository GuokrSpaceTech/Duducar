package com.guokrspace.duducar.util;

import android.content.res.Resources;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DisplayUtils {
	
	
	/**
	 * Convert Dp to Pixel
	 */
	public static int dpToPx(float dp, Resources resources){
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
		return (int) px;
	}
	
	public static int getRelativeTop(View myView) {
//	    if (myView.getParent() == myView.getRootView())
	    if(myView.getId() == android.R.id.content)
	        return myView.getTop();
	    else
	        return myView.getTop() + getRelativeTop((View) myView.getParent());
	}
	
	public static int getRelativeLeft(View myView) {
//	    if (myView.getParent() == myView.getRootView())
		if(myView.getId() == android.R.id.content)
			return myView.getLeft();
		else
			return myView.getLeft() + getRelativeLeft((View) myView.getParent());
	}

	public static String getShortDate(String date){
		if (TextUtils.isEmpty(date)) {
			return "刚刚";
		}
		if(date.length() < 11){//s
			date += "000";
		}
		Date orderDate = new Date(Long.parseLong(date));
		SimpleDateFormat format = new SimpleDateFormat("MM月dd日 HH:mm");
		return format.format(orderDate);
	}
	
}
