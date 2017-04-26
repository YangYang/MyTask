package com.imudges.mytask.UI;

import android.app.Application;
import org.xutils.x;

/**
 * Created by yangyang on 2017/4/24.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);//Xutils初始化
    }
}