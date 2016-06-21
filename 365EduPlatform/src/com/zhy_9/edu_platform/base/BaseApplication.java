package com.zhy_9.edu_platform.base;

import cn.jpush.android.api.JPushInterface;
import android.app.Application;

public class BaseApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		JPushInterface.setDebugMode(true);
		JPushInterface.init(this);
	}

}
