package com.example.view;

import java.util.Set;

import com.zhy_9.edu_platform.util.VersionDialogUtil;
import com.zhy_9.edu_platform.view.MaterialDialog;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
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

public class MainActivity extends Activity {

	private WebView mainWeb;
	private WebSettings webSettings;
	public static final String JAVA_SCRIPT_INTERFACE_METHOD_NAME = "wst";
	public static final String EDU_PLATFORM_URL = "http://192.168.1.188/wb/api/install";
	private String registerId;
	CookieManager cookieManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		getRegisterId();
		setAliasAndTags();
		webConfigSetting();
		cookieManager = CookieManager.getInstance();
		String cookies = cookieManager.getCookie(EDU_PLATFORM_URL);
		if (!TextUtils.isEmpty(cookies)) {
			Log.e("cookies", cookies);
		}
//		VersionDialogUtil.isNeedUpdate(this);
		MaterialDialog dialog = new MaterialDialog(this);
		dialog.setTitle("更新");
		dialog.setMessage("有新更新");
		dialog.setPositiveButton("确定", new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
			}
		});
		dialog.setNegativeButton("取消", new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
			}
		});
		dialog.show();
	}

	private void initView() {
		mainWeb = (WebView) findViewById(R.id.main_web);
	}

	private void getRegisterId() {
		registerId = JPushInterface.getRegistrationID(this);
	}
	
	private void setAliasAndTags(){
		if (!TextUtils.isEmpty(registerId)) {
			JPushInterface.setAlias(this, registerId, new TagAliasCallback() {
				
				@Override
				public void gotResult(int arg0, String arg1, Set<String> arg2) {
					
				}
			});
		}else {
			
		}
	}

	@SuppressLint("SetJavaScriptEnabled") private void webConfigSetting() {
		mainWeb.setWebViewClient(mWebViewClient);
		webSettings = mainWeb.getSettings();
		webSettings.setJavaScriptEnabled(true);
		mainWeb.addJavascriptInterface(this, JAVA_SCRIPT_INTERFACE_METHOD_NAME);
		mainWeb.setWebChromeClient(mWebChromeClient);
		mainWeb.loadUrl(EDU_PLATFORM_URL);
	}

	@JavascriptInterface
	public String startFunction(String str) {
		return registerId;
	}
	
	@JavascriptInterface
	public void setFunction(String str){
		if (!TextUtils.isEmpty(str)) {
			Log.e("setFunction", str);
		}else {
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
		if (keyCode == KeyEvent.ACTION_DOWN) {
			if (mainWeb.canGoBack()) {
				mainWeb.goBack();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	};

}
