package com.octoberry.nonamebank.net;

import org.json.JSONObject;

public interface JSONResponseListener {
	void handleResponse(int result, JSONObject response, String error);
}
