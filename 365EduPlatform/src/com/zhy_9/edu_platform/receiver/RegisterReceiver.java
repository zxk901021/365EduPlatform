package com.zhy_9.edu_platform.receiver;

import com.zhy_9.hse.jpush.activity.PushActivity;

import cn.jpush.android.api.JPushInterface;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class RegisterReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
//		Toast.makeText(context, "ע��ɹ���", Toast.LENGTH_SHORT).show();
		Bundle bundle = intent.getExtras();
		if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
			System.out.println("�û��������֪ͨ");
			Intent webIntent = new Intent(context, PushActivity.class);
			webIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
			String alert = bundle.getString(JPushInterface.EXTRA_ALERT);
			String extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
			System.out.println(title + "***" + alert + "***" + extra);
			webIntent.putExtra("title", title);
			webIntent.putExtra("extra", extra);
			context.startActivity(webIntent);
			
		}
		
	}

}
