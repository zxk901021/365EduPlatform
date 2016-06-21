package com.zhy_9.edu_platform.util;

import java.util.Map;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;

public class HttpUtil {

	public static void getVolley(Context context, String url,
			final VolleyListener listener) {
		RequestQueue queue = MyVolley.getRequestQueue(context);
		StringRequest myReq = new UTF8StringRequest(Method.GET, url,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						listener.onResponse(response);
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						listener.onErrorResponse(error);
					}
				});
		queue.add(myReq);
	}

	public static void postVolley(Context context, String url,
			final Map<String, String> map, final VolleyListener listener) {
		RequestQueue queue = MyVolley.getRequestQueue(context);
		Response.Listener<String> SuccListener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				listener.onResponse(response);
			}
		};
		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				listener.onErrorResponse(error);
			}
		};
		StringRequest myReq = new UTF8StringRequest(Method.POST, url,
				SuccListener, errorListener) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				return map;
			}
		};
		queue.add(myReq);
	}

}
