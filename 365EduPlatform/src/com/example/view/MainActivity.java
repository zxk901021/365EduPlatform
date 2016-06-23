package com.example.view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import com.android.volley.VolleyError;
import com.zhy_9.edu_platform.adapter.LaunchPagerAdapter;
import com.zhy_9.edu_platform.service.DownLoadService;
import com.zhy_9.edu_platform.util.EduSohoUtil;
import com.zhy_9.edu_platform.util.HttpUtil;
import com.zhy_9.edu_platform.util.PackInfoUtil;
import com.zhy_9.edu_platform.util.VolleyListener;
import com.zhy_9.edu_platform.view.CircleIndicator;
import com.zhy_9.edu_platform.view.MaterialDialog;
import com.zhy_9.hse.jpush.R;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

@SuppressLint("NewApi") public class MainActivity extends Activity implements OnClickListener{
	
	
	private ViewPager launchPager;
	private List<View> views;
	private static final int[] imgs = new int[] { R.drawable.edu_launcher_1,
			R.drawable.edu_launcher_2, R.drawable.edu_launcher_3 };
	private LaunchPagerAdapter adapter;
	private CircleIndicator indicator;
	private Button start;

	private WebView mainWeb;
	private WebSettings webSettings;
	public static final String JAVA_SCRIPT_INTERFACE_METHOD_NAME = "wst";
	public static final String EDU_PLATFORM_URL = "http://192.168.1.188/";
	public static final String CHECK_VERSION_URL = "http://192.168.1.188/wb/api/getversion";
	private String registerId;
	private SharedPreferences preferences;
	private Editor editor;
	private String tag;
	private SwipeRefreshLayout refresh;
	private String versionResponse;
	public static final int VERSION_CODE = 1;
	private String downUrl;
	private static final String ANDROID_HSE_COOKIE = "hsecookie=hseclientforandroid";
	private RelativeLayout pagerLayout;

	OnRefreshListener listener = new OnRefreshListener() {

		@Override
		public void onRefresh() {
			mainWeb.reload();
		}
	};

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case VERSION_CODE:
				try {
					JSONObject versionJSON = new JSONObject(versionResponse);
					String serverVersion = versionJSON.getString("version");
					String packageVersion = PackInfoUtil
							.getVersionName(MainActivity.this);
					String downloadUrl = versionJSON.getString("app_android");
					downUrl = downloadUrl;
					checkUpdate(serverVersion, packageVersion, downloadUrl);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		initAdapter();
		addPagerChangedListener();
		initEditor();
		checkVersion();
		getRegisterId();
		setAliasAndTags();
		webConfigSetting();
	}

	private void showDialog() {
		final MaterialDialog dialog = new MaterialDialog(this);
		dialog.setTitle("更新");
		dialog.setMessage("当前有新版本，是否更新？");
		dialog.setPositiveButton("确认", new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						DownLoadService.class);
				intent.putExtra("url", downUrl);
				intent.setAction("com.zhy_9.edu_platform.service.DownLoadService");
				startService(intent);
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

	private void checkUpdate(String oldVersion, String newVersion, String url) {
		if (!TextUtils.isEmpty(oldVersion) && !TextUtils.isEmpty(newVersion)) {
			if (!oldVersion.equals(newVersion)) {
				showDialog();
			}
		}

	}
	
	private void syncCookie(String url){
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		cookieManager.setCookie(url, ANDROID_HSE_COOKIE);
		cookieManager.flush();
	}

	private void initView() {
		mainWeb = (WebView) findViewById(R.id.main_web);
		refresh = (SwipeRefreshLayout) findViewById(R.id.refresh);
		refresh.setColorSchemeColors(R.color.green, R.color.lite_blue);
		refresh.setOnRefreshListener(listener);
		
		launchPager = (ViewPager) findViewById(R.id.launch_pager);
		indicator = (CircleIndicator) findViewById(R.id.launch_indicator);
		start = (Button) findViewById(R.id.launch_start);
		pagerLayout = (RelativeLayout) findViewById(R.id.pager_layout);
	}

	private void initEditor() {
		preferences = getSharedPreferences("hse_prefer", Context.MODE_PRIVATE);
		editor = preferences.edit();
		tag = preferences.getString("tag", "");
	}
	
	

	private void checkVersion() {
		HttpUtil.getVolley(this, CHECK_VERSION_URL, new VolleyListener() {

			@Override
			public void onErrorResponse(VolleyError error) {

			}

			@Override
			public void onResponse(String response) {
				versionResponse = response;
				handler.sendEmptyMessage(VERSION_CODE);
			}
		});
	}

	private void getRegisterId() {
		registerId = JPushInterface.getRegistrationID(this);
	}

	private void setAliasAndTags() {
		if (!TextUtils.isEmpty(registerId)) {
			JPushInterface.setAlias(this, registerId, new TagAliasCallback() {

				@Override
				public void gotResult(int arg0, String arg1, Set<String> arg2) {

				}
			});
		} else {

		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void webConfigSetting() {
		mainWeb.setWebViewClient(mWebViewClient);
		webSettings = mainWeb.getSettings();
		webSettings.setJavaScriptEnabled(true);
		mainWeb.addJavascriptInterface(this, JAVA_SCRIPT_INTERFACE_METHOD_NAME);
		mainWeb.setWebChromeClient(mWebChromeClient);
		syncCookie(EDU_PLATFORM_URL);
		mainWeb.loadUrl(EDU_PLATFORM_URL);
	}

	@JavascriptInterface
	public String startFunction(String str) {
		return registerId;
	}

	public HashSet<String> getArray(String str) {
		HashSet<String> sets = new HashSet<String>();
		if (!TextUtils.isEmpty(str)) {
			if (tag.equals(str)) {
				return null;
			}else {
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

	@JavascriptInterface
	public void setFunction(final String str) {
		if (!TextUtils.isEmpty(str)) {
			Log.e("setFunction", str);
			Set<String> tags = new HashSet<String>();
			tags = getArray(str);
			if (tags == null) {
				return;
			}else {
				for (int i = 0; i < tags.size(); i++) {
					Log.e("tags", tags.toString());
				}
				JPushInterface.setTags(this, tags, new TagAliasCallback() {

					@Override
					public void gotResult(int arg0, String arg1, Set<String> arg2) {
						Log.e("setTag", arg0 + "");

						// if (arg0 == 0) {
						// editor.putString("tag", str);
						// editor.commit();
						// }
					}
				});
			}

		} else {
			Log.e("setFunction", "str = null");
		}

	}

	private WebViewClient mWebViewClient = new WebViewClient() {
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return false;
		};

	};

	private CustomViewCallback mCallback = null;
	private View mView = null;

	private WebChromeClient mWebChromeClient = new WebChromeClient() {
		public void onShowCustomView(android.view.View view,
				CustomViewCallback callback) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			if (mCallback != null) {
				mCallback.onCustomViewHidden();
				mCallback = null;
				return;
			}
			ViewGroup parent = (ViewGroup) mainWeb.getParent();
			parent.removeView(mainWeb);
			parent.addView(view);
			setFullScreen();
			mView = view;
			mCallback = callback;
		};

		public void onHideCustomView() {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
			if (mView != null) {
				if (mCallback != null) {
					mCallback.onCustomViewHidden();
					mCallback = null;
				}
				ViewGroup parent = (ViewGroup) mView.getParent();
				parent.removeView(mView);
				parent.addView(mainWeb);
				quitFullScreen();
				mView = null;
			}
		};

		public void onProgressChanged(WebView view, int newProgress) {
			if (newProgress < 100) {
				refresh.post(new Runnable() {

					@Override
					public void run() {
						refresh.setRefreshing(true);
					}
				});
			} else {
				refresh.post(new Runnable() {

					@Override
					public void run() {
						refresh.setRefreshing(false);
					}
				});
			}
		};
	};

	private void setFullScreen() {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	private void quitFullScreen() {
		final WindowManager.LayoutParams attr = getWindow().getAttributes();
		attr.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
		getWindow().setAttributes(attr);
		getWindow()
				.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

	}

	protected void onPause() {
		mainWeb.reload();
		super.onPause();
	};

	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mainWeb.canGoBack()) {
				mainWeb.goBack();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	};
	
	public void initAdapter() {
		int j = imgs.length;
		views = new ArrayList<View>();
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		for (int i = 0; i < j; i++) {
			ImageView img = new ImageView(this);
			img.setLayoutParams(params);
			Bitmap bitmap = EduSohoUtil.readBitmap(this, imgs[i]);
			img.setImageBitmap(bitmap);
			// img.setImageResource(imgs[i]);
			img.setScaleType(ImageView.ScaleType.FIT_XY);
			views.add(img);
		}
		adapter = new LaunchPagerAdapter(views);
		launchPager.setAdapter(adapter);
		indicator.setViewPager(launchPager);
	}
	
	public void addPagerChangedListener() {
		launchPager
				.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
					@Override
					public void onPageScrolled(int position,
							float positionOffset, int positionOffsetPixels) {

					}

					@Override
					public void onPageSelected(int position) {
						if (position == (views.size() - 1)) {
							start.setVisibility(View.VISIBLE);
							start.setClickable(true);
							start.setOnClickListener(MainActivity.this);
						} else {
							start.setVisibility(View.INVISIBLE);
							start.setClickable(false);
						}
					}

					@Override
					public void onPageScrollStateChanged(int state) {

					}
				});
	}

	@Override
	public void onClick(View v) {
		pagerLayout.setVisibility(View.GONE);
		refresh.setVisibility(View.VISIBLE);
	}
	
}
