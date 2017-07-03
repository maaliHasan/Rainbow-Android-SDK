/**
 * 
 */
package com.ale.security.util;

import java.io.IOException;

/**
 * HttpUnauthorizedException is thrown when a http request get 401 in rsponse.
 */
public class HttpUnauthorizedException extends IOException
{
	
	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = -7679979708085569134L;
	
	/**
	 * @uml.property  name="request"
	 * @uml.associationEnd  
	 */
	private AuthorizationRequest request;

	/**
	 * Construct a new Exception for the specified HttpURLConnection.
	 *
	 * @param request
	 *            An authorization request.
	 */
	public HttpUnauthorizedException(AuthorizationRequest request)
	{
		this.request = request;
	}

	/**
	 * @return the request
	 */
	public AuthorizationRequest getAuthorizationRequest()
	{
		return request;
	}
	
}
