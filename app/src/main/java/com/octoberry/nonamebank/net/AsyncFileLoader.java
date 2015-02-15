package com.octoberry.nonamebank.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.octoberry.nonamebank.R;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class AsyncFileLoader extends AsyncTask<Bundle, Void, String> {

	private final String TAG = getClass().getName();
	
	private String apiURL;
	
	private FileResponseListener delegate;
	private int responseCode = 200;
	private Context mContext;
	private String targetPath; 
	
	public AsyncFileLoader(Context context, String targetPath) {
		mContext = context;
		apiURL = mContext.getResources().getString(R.string.server_url);
		this.targetPath = targetPath;
	}
	
	public void registryListener(FileResponseListener listener) {
		delegate = listener;
	}
	
	@Override
	protected String doInBackground(Bundle... arguments) {
		HashMap<String, String> params = null;
		String url = apiURL;
		HttpResponse response = null;
		
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
        	
        	Log.d("###", "doInBackground: url: " + url);     
        	
        	// get header parameters
            if ((arguments.length > 1) && (arguments[1] != null)) {
            	params = new HashMap<String, String>();
            	for (String key : arguments[1].keySet()) {
            		params.put(key, arguments[1].getString(key));
            	}	
            }
        	
            HttpClient httpclient = new DefaultHttpClient();                                   
            
            HttpGet httpget = new HttpGet(url);
            if (params != null) {
            	for (String key : params.keySet()) {
            		httpget.setHeader(key, params.get(key));
            	}
            }         
            response = httpclient.execute(httpget);
            
            if (response == null) {
            	Log.e(TAG, "can't execute request");
        		return null;
            }        
            
            responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == 200) {
	            HttpEntity fileEntity = response.getEntity();
	            InputStream is = fileEntity.getContent();
	            
	            File file = new File(targetPath);
	            if (file.exists()) {
	            	file.delete();
	            }
	            
	            FileOutputStream fos = new FileOutputStream(file);
	            int inByte;
	            while((inByte = is.read()) != -1) fos.write(inByte);
	            is.close();
	            fos.close();
	            return targetPath;
            } else {
            	Log.e(TAG, "Can't load credentials file.");
            	String msg = EntityUtils.toString(response.getEntity());
            	Log.d(TAG, "response: " + msg);
            	return msg;
            }
        } catch (Exception ex) {
            ex.printStackTrace();           
        }

        return null;
	}

	@Override
	protected void onPostExecute(String path) {
		 if (delegate != null) {
			 delegate.handleResponse(responseCode, path);			 
         } else {
        	 delegate.handleResponse(responseCode, null);
         }
	}
}
