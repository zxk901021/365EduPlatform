package com.zhy_9.edu_platform.base;

import cn.jpush.android.api.BasicPushNotificationBuilder;
import cn.jpush.android.api.JPushInterface;
import android.app.Application;
import com.zhy_9.hse.jpush.R;

public class BaseApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		JPushInterface.setDebugMode(true);
		JPushInterface.init(this);
		BasicPushNotificationBuilder builder = new BasicPushNotificationBuilder(this);
		builder.statusBarDrawable = R.drawable.hse_logo;
		JPushInterface.setPushNotificationBuilder(1, builder);
	}

}
