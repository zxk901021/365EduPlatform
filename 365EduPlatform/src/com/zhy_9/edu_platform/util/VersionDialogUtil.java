package com.zhy_9.edu_platform.util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class VersionDialogUtil {

	public static void isNeedUpdate(Context context) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle("�汾����");
		builder.setMessage("��ǰ���°汾���£��Ƿ����أ�");
		builder.setPositiveButton("ȷ��", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		builder.setNegativeButton("ȡ��", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		AlertDialog updateDialog = builder.create();
		updateDialog.show();
	}

}
