/**
 * 
 */
package com.ale.infra.http.adapter.concurrent;

import com.ale.infra.proxy.directory.SearchResponseByJid;

/**
 * A response to an asynchronous service call with a T result.
 * 
 * @param < T >
 *            the generic result type
 */
public class AsyncServiceResponseResult<T> extends AsyncServiceResponseVoid
{
	
	private T m_result;
	
	/**
	 * Construct a new SoapAsyncResult with the specified exception and result
	 * 
	 * @param e
	 *            the exception
	 * @param result
	 *            the result
	 */
	public AsyncServiceResponseResult(RainbowServiceException e, T result)
	{
		super(e);
		this.m_result = result;
	}



	/**
	 * @return the result;
	 * @uml.property name="result"
	 */
	public T getResult()
	{
		return m_result;
	}
}
