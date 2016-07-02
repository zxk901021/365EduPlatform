package com.zhy_9.edu_platform.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.zhy_9.edu_platform.constants.MIMITypeArray;
import com.zhy_9.hse.jpush.R;
import com.zhy_9.hse.jpush.activity.NotificationActivity;

public class DownLoadService extends Service {
	private NotificationManager notificationMrg;
	public static Map<String, MyThread> threadcache = new HashMap<String, MyThread>();
	private Holder holder;
	int notifyflag = 1;

	private ConnectivityManager connectivityManager;
	private NetworkInfo info;

	public void onCreate() {
		super.onCreate();
		notificationMrg = (NotificationManager) this
				.getSystemService(android.content.Context.NOTIFICATION_SERVICE);

		// 注册网络监听接收器
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mFilter.addAction("android.basic.notification.click.pause");
		mFilter.addAction("android.basic.notification.click.cancel");
		mFilter.addAction("android.basic.notification.click.restart");
		registerReceiver(stateReceiver, mFilter);
		// 注册任务广播接收器

	}

	@SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		final String apkurl = intent.getStringExtra("url");
		Intent notificationIntent = new Intent(getApplicationContext(),
				NotificationActivity.class);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		notificationIntent.putExtra("url", apkurl);
		Bundle bundle = new Bundle();
		bundle.putInt("flag", notifyflag);
		Log.i("avi", "loadfile中的flag     " + notifyflag);
		notificationIntent.putExtras(bundle);
		// addflag设置跳转类型
		PendingIntent contentIntent1 = PendingIntent.getActivity(
				getApplicationContext(), notifyflag, notificationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		@SuppressWarnings("deprecation")
		Notification.Builder builder = new Builder(getApplicationContext());
		Notification notification = new Notification(R.drawable.hse_logo,
				"下载进度", System.currentTimeMillis());// 设定Notification出现时的声音，一般不建议自定义
		// System.currentTimeMillis()
		notification.flags = Notification.FLAG_ONGOING_EVENT
				| Notification.FLAG_NO_CLEAR;// 出现在 “正在运行的”栏目下面
		// notification.flags |= Notification.FLAG_NO_CLEAR;// 标题栏默认清除按钮失效
		RemoteViews contentView1 = new RemoteViews(getPackageName(),
				R.layout.notification_version);
		contentView1.setTextViewText(R.id.n_title, "准备下载");
		contentView1.setTextViewText(R.id.n_text, "当前进度：" + 0 + "% ");
		contentView1.setProgressBar(R.id.n_progress, 100, 0, false);
		notification.contentView = contentView1;
		notification.contentIntent = contentIntent1;

		final MyThread thread = new MyThread(notification, apkurl, notifyflag++);

		thread.setchanger(true);
		threadcache.put(apkurl, thread);
		thread.start();

		return START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(stateReceiver);
	}

	// 状态栏视图更新
	private Notification displayNotificationMessage(Notification notification,
			int count, int flag, String url, String filename) {
		notification.contentIntent = receiveMsmIntent(flag, url);
		RemoteViews contentView1 = notification.contentView;
		contentView1.setTextViewText(R.id.n_title, filename);
		contentView1.setTextViewText(R.id.n_text, "当前进度：" + count + "% ");
		contentView1.setProgressBar(R.id.n_progress, 100, count, false);
		notification.contentView = contentView1;
		// 提交一个通知在状态栏中显示。如果拥有相同标签和相同id的通知已经被提交而且没有被移除，该方法会用更新的信息来替换之前的通知。
		notificationMrg.notify(flag, notification);
		return notification;

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void sendMsg(int what, int c, String url,
			Notification notification, int flag, Uri uri, String filename,
			boolean changer) {
		Message msg = new Message();
		msg.what = what;// 用来识别发送消息的类型
		msg.arg1 = 0;
		holder = new Holder();
		holder.count = c;
		holder.url = url;
		holder.flag = flag;
		holder.notify = notification;
		holder.Uri = uri;
		holder.filename = filename;
		holder.changer = changer;
		msg.obj = holder;// 消息传递的自定义对象信息
		handler.sendMessage(msg);
	}

	// 定义一个Handler，用于处理下载线程与主线程间通讯
	private Handler handler = new Handler() {
		@SuppressLint("ShowToast")
		@Override
		public void handleMessage(Message msg) {
			final Holder data = (Holder) msg.obj;
			if (!Thread.currentThread().isInterrupted()) {
				// 判断下载线程是否中断
				switch (msg.what) {
				case 1:
					if (data.count >= 99) {
						notificationMrg.cancel(data.flag);
						break;
					}

					if (threadcache.containsKey(data.url)) {
						// 每次更新时，先以key，扫描hashmap，存在则读取出来。
						Notification notification;
						MyThread thread;
						thread = threadcache.get(data.url);
						notification = thread.notification;
						notification = displayNotificationMessage(notification,
								data.count, data.flag, data.url, data.filename);
						thread.notification = notification;
						threadcache.put(data.url, thread);
					}
					break;
				case 2:
					if (data.changer) {
						Log.e("data.uri", data.Uri.toString());
						openfile(data.Uri);
					} else {
						Log.i("avi", "取消下载");
						Toast.makeText(getApplicationContext(),
								data.filename + "下载取消", 1).show();
					}
					threadcache.remove(data.url);
					Intent cancel = new Intent()
							.setAction("android.basic.button.click.cancel");
					cancel.putExtra("url", data.url);
					sendBroadcast(cancel);
					break;
				case -1:
					String error = msg.getData().getString("error");
					Toast.makeText(getApplication(), data.filename + "下载异常终止",
							1).show();
					Intent cancel2 = new Intent()
							.setAction("android.basic.button.click.cancel");
					cancel2.putExtra("url", data.url);
					sendBroadcast(cancel2);
					break;
				// 否则输出错误提示
				}
			}
			super.handleMessage(msg);
		}
	};

	public void openfile(Uri url) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		// 区别于默认优先启动在activity栈中已经存在的activity（如果之前启动过，并还没有被destroy的话）而是无论是否存在，都重新启动新的activity
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// intent.setDataAndType(url,
		// "application/vnd.android.package-archive");
		intent.setAction(Intent.ACTION_VIEW);
		String type = getMIMEType(url.toString());
		intent.setDataAndType(url, type);
		startActivity(intent);
	}

	public String getMIMEType(String fileName) {
		String type = "/";
		int dotIndex = fileName.lastIndexOf(".");
		if (dotIndex < 0) {
			return type;
		}
		String endName = fileName.substring(dotIndex, fileName.length())
				.toLowerCase();
		if (endName == "")
			return type;
		int len = MIMITypeArray.MIMITable.length;
		for (int i = 0; i < len; i++) {
			if (endName.equals(MIMITypeArray.MIMITable[i][0])) {
				type = MIMITypeArray.MIMITable[i][1];
			}
		}
		return type;
	}

	public class Holder {
		Notification notify;
		String url;
		int count;
		int flag;
		Uri Uri;
		String filename;
		boolean changer;
	}

	private BroadcastReceiver stateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				connectivityManager = (ConnectivityManager)

				getSystemService(Context.CONNECTIVITY_SERVICE);
				info = connectivityManager.getActiveNetworkInfo();
				if (info != null && info.isAvailable()) {
					String name = info.getTypeName();
				} else {
					while (notifyflag > 0) {
						notificationMrg.cancel(notifyflag);
						notifyflag -= 1;
					}
					Toast.makeText(getApplicationContext(), "网络异常，下载服务停止！", 1)
							.show();
					onDestroy();
				}
			}

			if (action.equals("android.basic.notification.click.cancel")) {
				Bundle bundle = intent.getExtras();
				int m = bundle.getInt("flag");
				String url = intent.getStringExtra("url");
				notificationMrg.cancel(m);
				if (threadcache.containsKey(url)) {
					MyThread onethread = threadcache.get(url);
					onethread.setchanger(false);
				}
			}

			if (action.equals("android.basic.notification.click.pause")) {
				Bundle bundle = intent.getExtras();
				int m = bundle.getInt("flag");
				String url = intent.getStringExtra("url");
				// notificationMrg.cancel(m);
				if (threadcache.containsKey(url)) {
					MyThread onethread = threadcache.get(url);
					Log.i("avi", "pause   clickbroadcast中 thread 线程      url: "
							+ onethread.geturl());
					onethread.setSuspend(true);
					// 当前线程暂停/ 等待
				}
			}

			if (action.equals("android.basic.notification.click.restart")) {
				Bundle bundle = intent.getExtras();
				int m = bundle.getInt("flag");
				String url = intent.getStringExtra("url");
				if (threadcache.containsKey(url)) {
					MyThread onethread = threadcache.get(url);
					onethread.setSuspend(false);
					// 调用setSuspend(false)让当前线程恢复/唤醒
				}
			}
		}
	};

	private PendingIntent receiveMsmIntent(int flag, String url) {
		Intent intent = new Intent(getApplicationContext(),
				NotificationActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra("url", url);
		Bundle bundle = new Bundle();
		bundle.putInt("flag", flag);
		intent.putExtras(bundle);
		PendingIntent contentIntent = PendingIntent.getActivity(
				getApplicationContext(), flag, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		return contentIntent;
	}

	public class MyThread extends Thread {
		private boolean changer;
		private String url;
		private int flag;
		private boolean suspend = false;
		private String control = ""; // 只是需要一个对象而已，这个对象没有实际意义
		private Notification notification;

		public MyThread(Notification notification, String apkurl, int notifyflag) {
			super();
			this.notification = notification;
			this.url = apkurl;
			this.flag = notifyflag;
		}

		@SuppressLint("DefaultLocale")
		public void run() {
			System.out.println("父类中的方法");
			String url = geturl();
			int flag = getflag();
			String filename;
			int first = url.lastIndexOf("/") + 1;
			filename = url.substring(first, url.length()).toLowerCase();
			Log.i("filename", "从url中截取的filename  " + filename);
			double m = 0.0;

			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(url);
			HttpResponse response;
			try {
				response = client.execute(get);
				HttpEntity entity = response.getEntity();
				double length = entity.getContentLength();
				InputStream is = entity.getContent();
				// 使用InputStream对文件进行读取，就是字节流的输入
				FileOutputStream fileOutputStream = null;
				File file = null;
				Log.i("avi", "开关状态      " + getchanger());
				if (is != null && getchanger()) {
					file = new File(Environment.getExternalStorageDirectory(),
							filename);
					fileOutputStream = new FileOutputStream(file);
					byte[] buf = new byte[1024];
					int ch = -1;
					float count = 0;
					// ch中存放从buf字节数组中读取到的字节个数
					while ((ch = is.read(buf)) != -1 && getchanger()) {

						synchronized (control) {
							if (getSuspend()) {
								try {
									Log.i("avi", "暂停下载任务                "
											+ getSuspend());
									Intent pause = new Intent()
											.setAction("android.basic.button.click.pause");
									pause.putExtra("url", url);
									sendBroadcast(pause);
									control.wait();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}

						fileOutputStream.write(buf, 0, ch);
						count += ch;
						// 从字节数组读取数据read(buf)后，返回，读取的个数，count中保存，已下载的数据字节数
						double temp = count / length;
						if (temp >= m) {
							Log.i("TAG", "读取字节循环中的count" + temp);
							m += 0.1;
							int load = (int) (count * 100 / length);
							sendMsg(1, load, url, notification, flag, null,
									filename, true);
						}
						// 函数调用handler发送信息
					}
				}
				// 文件输出流为空，则表示下载完成，安装apk文件
				Uri Url = Uri.fromFile(file);
				Log.i("TAG", "下载完成，传递文件位置Url  " + Url);

				sendMsg(2, 0, url, notification, 0, Url, filename, getchanger());
				fileOutputStream.flush();
				if (fileOutputStream != null) {
					fileOutputStream.close();
				}
			} catch (Exception e) {
				sendMsg(-1, 0, url, notification, 0, null, filename, true);
			}

		}

		@SuppressLint("ShowToast")
		public void setSuspend(boolean suspend) {
			this.suspend = suspend;
			if (!suspend) {
				synchronized (control) {
					String filename = geturlfilename(url);
					Log.i("avi", filename + "继续下载   :   " + getSuspend());
					Toast.makeText(getApplicationContext(),
							filename + "继续下载---", 1).show();
					Intent restart = new Intent();
					restart.setAction("android.basic.button.click.restart");
					restart.putExtra("url", url);
					sendBroadcast(restart);
					control.notify();
				}
			}

		}

		public boolean getSuspend() {
			return this.suspend;
		}

		// 设置开关状态
		public void setchanger(boolean changer) {
			this.changer = changer;
		}

		public boolean getchanger() {
			return this.changer;
		}

		public int getflag() {
			return this.flag;
		}

		public String geturl() {
			return this.url;
		}

		@SuppressLint("DefaultLocale")
		public String geturlfilename(String url) {

			String filename;
			int first = url.lastIndexOf("/") + 1;
			filename = url.substring(first, url.length()).toLowerCase();
			return filename;
		}

	}

}
