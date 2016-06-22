package com.zhy_9.edu_platform.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class PackInfoUtil {

	public static String getVersionName(Context context)
			throws NameNotFoundException {
		PackageManager manager = context.getPackageManager();
		PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
		return info.versionName;
	}

	public static int getVersionCode(Context context)
			throws NameNotFoundException {
		PackageManager manager = context.getPackageManager();
		PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
		return info.versionCode;
	}

}
