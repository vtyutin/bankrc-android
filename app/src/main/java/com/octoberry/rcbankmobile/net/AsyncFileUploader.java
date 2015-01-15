package com.octoberry.rcbankmobile.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.octoberry.rcbankmobile.R;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class AsyncFileUploader extends AsyncTask<Bundle, Void, JSONObject> {

	private final String TAG = getClass().getName();

	private String apiURL;
	private JSONResponseListener delegate;
	private int responseCode = 200;
	private Context mContext;
	
	public AsyncFileUploader(Context context) {
		mContext = context;
		apiURL = mContext.getResources().getString(R.string.server_url);
	}

	public void registryListener(JSONResponseListener listener) {
		delegate = listener;
	}

	@Override
	protected JSONObject doInBackground(Bundle... arguments) {
		String responseString;
		HashMap<String, String> params = null;
        HashMap<String, String> bodyParams = null;
		String url = apiURL;
		HttpResponse response = null;

		Log.d("###", "doInBackground");
		try {
			if ((arguments == null) || (arguments.length == 0)) {
				Log.e(TAG, "invalid arguments list");
				return null;
			}

			// get file to upload
			String filePath = arguments[0].getString("filePath");
			Log.d("###", "upload file: " + filePath);
			if ((filePath == null) || (filePath.length() == 0)) {
				Log.e(TAG, "invalid filePath: " + filePath);
				return null;
			} else {
				File f = new File(filePath);
				if (!f.exists()) {
					Log.e(TAG, "file does not exist: " + filePath);
					return null;
				}
				if (filePath.startsWith("http")) {
					filePath = downloadFile(filePath);
					if ((filePath == null) || (filePath.length() == 0)) {
						Log.e(TAG, "can't download file to: " + filePath);
						return null;
					}
				}					
			}
			
			// get endpoint value
			String endPoint = arguments[0].getString("endpoint");
			if ((endPoint == null) || (endPoint.length() == 0)) {
				Log.e(TAG, "invalid endpoint value: " + endPoint);
				return null;
			}
			url = url + endPoint;

			// get header parameters
			if ((arguments.length > 1) && (arguments[1] != null)) {
				params = new HashMap<String, String>();
				for (String key : arguments[1].keySet()) {
					params.put(key, arguments[1].getString(key));
				}
			}

            // get body parameters
            if ((arguments.length > 2) && (arguments[2] != null)) {
                bodyParams = new HashMap<String, String>();
                for (String key : arguments[2].keySet()) {
                    bodyParams.put(key, arguments[2].getString(key));
                }
            }

			HttpClient httpclient = new DefaultHttpClient();

			MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            
            File file = new File(filePath);
            if(file.exists())
            {
                entityBuilder.addBinaryBody("file", file);
            } else {
            	Log.e(TAG, "file doesn't exist: " + filePath);
				return null;
            }

            if (bodyParams != null) {
                for (String key : bodyParams.keySet()) {
                    entityBuilder.addTextBody(key, bodyParams.get(key));
                }
            }

            HttpEntity entity = entityBuilder.build();

			HttpPost httppost = new HttpPost(url);
			httppost.setEntity(entity);
			if (params != null) {
				for (String key : params.keySet()) {
					httppost.setHeader(key, params.get(key));
				}
			}
			response = httpclient.execute(httppost);	
			responseCode = response.getStatusLine().getStatusCode();
			HttpEntity httpEntity = response.getEntity();
			responseString = EntityUtils.toString(httpEntity);

			entity = response.getEntity();
			try {
			    if (entity != null)
			        entity.consumeContent();
			    } catch (IOException e) {
			        e.printStackTrace();
			}
			
			Log.d("response is", responseString);
			if ((responseString != null) && (responseString.length() > 0)) {
				return new JSONObject(responseString);
			} else {
				return new JSONObject(new String("{\"status\": " + responseCode
						+ "}"));
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
	
	private String downloadFile(String path) {
		try {
	        URL url = new URL(path);
	        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
	        urlConnection.setRequestMethod("GET");
	        urlConnection.setDoOutput(true);
	        urlConnection.connect();
	        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
	        File file = new File(directory, url.getFile());
	        FileOutputStream fileOutput = new FileOutputStream(file);
	        InputStream inputStream = urlConnection.getInputStream();
	        byte[] buffer = new byte[2048];
	        int bufferLength = 0;
	        while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
	                fileOutput.write(buffer, 0, bufferLength);
	        }
	        fileOutput.close();
	        return file.getAbsolutePath();
		} catch (MalformedURLException e) {
	        e.printStackTrace();
		} catch (IOException e) {
	        e.printStackTrace();
		}		
		return null;
	}
}
