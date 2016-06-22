package com.zhy_9.edu_platform.util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class VersionDialogUtil {

	public static void isNeedUpdate(Context context) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle("版本更新");
		builder.setMessage("当前有新版本更新，是否下载？");
		builder.setPositiveButton("确定", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		builder.setNegativeButton("取消", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		AlertDialog updateDialog = builder.create();
		updateDialog.show();
	}

}
