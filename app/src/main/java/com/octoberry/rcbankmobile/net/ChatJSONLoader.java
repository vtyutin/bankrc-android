package com.octoberry.rcbankmobile.net;

import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.octoberry.rcbankmobile.R;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class ChatJSONLoader extends AsyncTask<Bundle, Void, JSONObject> {

	private final String TAG = getClass().getName();
	
	private String apiURL;
	private JSONResponseListener delegate;
	private int responseCode = 200;
	private Context mContext;
	
	public ChatJSONLoader(Context context) {
		mContext = context;
		apiURL = mContext.getResources().getString(R.string.server_url);
	}
	
	public void registryListener(JSONResponseListener listener) {
		delegate = listener;
	}
	
	@Override
	protected JSONObject doInBackground(Bundle... arguments) {
		String responseString;
		StringEntity entity = null;
		HashMap<String, String> params = null;
		String url = apiURL;
		HttpResponse response = null;
		
		Log.d("###", "doInBackground");
        try {
        	if ((arguments == null) || (arguments.length == 0)) {
        		Log.e(TAG, "invalid arguments list");
        		return null;
        	}
        	
        	// get endpoint value
        	String endPoint = arguments[0].getString("endpoint");
        	if ((endPoint == null) || (endPoint.length() == 0)) {
        		Log.e(TAG, "invalid endpoint value: " + endPoint);
        		return null;
        	}
        	url = url + endPoint;
        	
        	// get request type value
        	String requestType = arguments[0].getString("requestType");
        	if ((requestType == null) || ((!requestType.equals("GET")) && (!requestType.equals("POST")))) {
        		Log.e(TAG, "invalid request type value: " + requestType);
        		return null;
        	}
        	
        	// get header parameters
            if ((arguments.length > 1) && (arguments[1] != null)) {
            	params = new HashMap<String, String>();
            	for (String key : arguments[1].keySet()) {
            		params.put(key, arguments[1].getString(key));
            	}	
            }
        	
        	// get body parameters for post request
        	JSONObject jsonBodyParams = null;
            if ((arguments.length > 2) && (arguments[2] != null)) {
            	jsonBodyParams = new JSONObject();
            	for (String key : arguments[2].keySet()) {
            		jsonBodyParams.put(key, arguments[2].getString(key));
            	}
            	entity = new StringEntity(jsonBodyParams.toString(), HTTP.UTF_8);
            	entity.setContentType("application/json");
            }
        	
            HttpClient httpclient = new DefaultHttpClient();                                   
            
            if (requestType.equals("POST")) {
            	HttpPost httppost = new HttpPost(url);
            	if (entity != null) {
            		httppost.setEntity(entity);
            	}
            	if (params != null) {
            		for (String key : params.keySet()) {
            			httppost.setHeader(key, params.get(key));
            		}
            	}
            	
            	response = httpclient.execute(httppost);
            } else if (requestType.equals("GET")) {
            	HttpGet httpget = new HttpGet(url);
            	if (params != null) {
            		for (String key : params.keySet()) {
            			httpget.setHeader(key, params.get(key));
            		}
            	}
            	response = httpclient.execute(httpget);
            }
            
            if (response == null) {
            	Log.e(TAG, "can't execute request");
        		return null;
            }
            
            responseCode = response.getStatusLine().getStatusCode();
            HttpEntity httpEntity = response.getEntity();
            responseString = EntityUtils.toString(httpEntity);

            Log.d("response is", responseString);
            if ((responseString != null) && (responseString.length() > 0)) {
            	return new JSONObject(responseString);
            } else {
            	return new JSONObject(new String("{\"status\": " + responseCode + "}"));            	
            }
        } catch (Exception ex) {
            ex.printStackTrace();           
        }

        return null;
	}

	@Override
	protected void onPostExecute(JSONObject result) {
		 if (delegate != null) {
			 if (result != null) {
				 delegate.handleResponse(responseCode, result, "");
			 } else {
				 delegate.handleResponse(-1, result, "error");
			 }
         }
	}
}
