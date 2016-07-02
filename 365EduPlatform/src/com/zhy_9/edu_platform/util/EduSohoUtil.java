package com.zhy_9.edu_platform.util;

import java.io.InputStream;
import java.util.HashSet;

import com.zhy_9.edu_platform.service.DownLoadService;
import com.zhy_9.edu_platform.view.MaterialDialog;
import com.zhy_9.hse.jpush.activity.MainActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

public class EduSohoUtil {

	public static Bitmap readBitmap(Context context, int resId) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		InputStream is = context.getResources().openRawResource(resId);
		return BitmapFactory.decodeStream(is, null, opt);

	}
	
	public static HashSet<String> getArray (String str, String tag, Editor editor){
		HashSet<String> sets = new HashSet<String>();
		if (!TextUtils.isEmpty(str)) {
			if (tag.equals(str)) {
				return null;
			} else {
				String[] set = str.split("_");

				if (!TextUtils.isEmpty(set[0])) {
					String a = "e" + set[0];
					sets.add(a);
				}
				if (!TextUtils.isEmpty(set[1])) {
					String b = "d" + set[1];
					sets.add(b);
				}
				if (!TextUtils.isEmpty(set[2])) {
					String[] set1 = set[2].split("$$$");
					for (int i = 0; i < set1.length; i++) {
						sets.add("g" + set1[i]);
					}
				}
				if (!TextUtils.isEmpty(set[3])) {
					String[] set2 = set[3].split("$$$");
					for (int i = 0; i < set2.length; i++) {
						sets.add("p" + set2[i]);
					}
				}
				editor.putString("tag", str);
				editor.commit();
			}
		}
		return sets;
	}
	
	public static void showUpdateDialog(final Context context, final String url){
		final MaterialDialog dialog = new MaterialDialog(context);
		dialog.setTitle("更新");
		dialog.setMessage("当前有新版本，是否更新？");
		dialog.setPositiveButton("确认", new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context,
						DownLoadService.class);
				intent.putExtra("url", url);
				intent.setAction("com.zhy_9.edu_platform.service.DownLoadService");
				context.startService(intent);
				dialog.dismiss();
			}
		});
		dialog.setNegativeButton("否", new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}
}
