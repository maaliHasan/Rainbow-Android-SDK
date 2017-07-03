/**
 * 
 */
package com.ale.infra.http.adapter.concurrent;

/**
 * A response to an asynchronous service call.
 */
public class AsyncServiceResponseVoid
{
	private RainbowServiceException m_exception;
	
	/**
	 * Construct a new SoapAsyncResult with the specified exception
	 * 
	 * @param e
	 *            the exception
	 */
	public AsyncServiceResponseVoid(RainbowServiceException e)
	{
		this.m_exception = e;
	}
	
	public RainbowServiceException getException()
	{
		return this.m_exception;
	}
	
	/**
	 * @return true if the calling service has raise an exception
	 */
	public boolean exceptionRaised()
	{
		return this.m_exception != null;
	}
}
