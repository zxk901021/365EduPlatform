package com.example.view;

import cn.jpush.android.api.JPushInterface;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		getRegisterId();
		webConfigSetting();
	}

	private void initView() {
		mainWeb = (WebView) findViewById(R.id.main_web);
	}

	private void getRegisterId() {
		registerId = JPushInterface.getRegistrationID(this);
	}

	private void webConfigSetting() {
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
