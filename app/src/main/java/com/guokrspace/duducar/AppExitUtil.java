package com.guokrspace.duducar;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by daddyfang on 15/11/16.
 */
public class AppExitUtil {

    private List<Activity> activityList = new LinkedList<Activity>();
    private static AppExitUtil instance;

    private AppExitUtil() {
    }

    // 单例模式中获取唯一的AppExitUtil实例
    public static AppExitUtil getInstance() {
        if(null == instance) {
            instance =new AppExitUtil();
        }
        return instance;
    }

    // 添加Activity到容器中
    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    // 遍历所有Activity并finish
    public void exit() {
        for(Activity activity : activityList) {
            activity.finish();
        }
        System.exit(0);
    }
}
