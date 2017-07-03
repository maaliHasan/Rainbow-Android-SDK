/**
 * 
 */
package com.ale.infra.http.adapter.concurrent;


import com.ale.util.log.Log;
import com.android.volley.NetworkResponse;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * @author fred
 */
public class RainbowServiceException extends Exception
{
	private static final String LOG_TAG = "RainbowServiceException";

	private static final long serialVersionUID = 3591699946911371190L;
	private int m_statusCode = 0;
	private int m_errorDetailsCode;
	private String m_errorDetailsMessage;

	public RainbowServiceException(String message)
	{
		super(message);
	}
	
	public RainbowServiceException(Throwable t)
	{
		super(t);
		if( t instanceof VolleyError) {
			VolleyError volleyException = (VolleyError) t;
			NetworkResponse networkResponse = volleyException.networkResponse;
			if( networkResponse == null) {
				Log.getLogger().warn(LOG_TAG, "No networkResponse at all");
				return;
			}
			m_statusCode = networkResponse.statusCode;

			//body ={"errorCode":401,
			// "errorMsg":"Unauthorized",
			// "errorDetails":"Unknown login or wrong password for login georges.francisco@al-enterprise.com, login is forbidden",
			// "errorDetailsCode":401501}

			if( m_statusCode != 404) {
				try {
					String body = new String(networkResponse.data, "UTF-8");
					Log.getLogger().verbose(LOG_TAG, "body =" + body);
					JSONObject jsonContent = new JSONObject(body);
					m_errorDetailsCode = jsonContent.optInt("errorDetailsCode", 0);
					m_errorDetailsMessage = jsonContent.optString("errorDetails");
				} catch (UnsupportedEncodingException e) {
					Log.getLogger().warn(LOG_TAG, "Exception while decoding Http Content; " + e.getMessage());
				} catch (JSONException e) {
					Log.getLogger().warn(LOG_TAG, "Exception while decoding Json; " + e.getMessage());
				}
			}
		} else {
			Log.getLogger().warn(LOG_TAG, "Exception not managed; "+t.getMessage());
		}
	}

	public int getStatusCode() {
		return m_statusCode;
	}

	public int getDetailsCode() {
		return m_errorDetailsCode;
	}

	public String getDetailsMessage() {
		return m_errorDetailsMessage;
	}

	public String toString() {
		return super.toString() + " Details: " + getDetailsMessage() + " code: " + getDetailsCode();
	}
}
