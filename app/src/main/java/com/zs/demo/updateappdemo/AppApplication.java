package com.zs.demo.updateappdemo;

import android.app.Application;
import android.content.Context;

/**
 * Created by zs
 * Date：2018年 07月 04日
 * Time：15:02
 * —————————————————————————————————————
 * About:
 * —————————————————————————————————————
 */
public class AppApplication extends Application{

    public static Context mContext;

    public static Context getAppContext(){
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }
}
