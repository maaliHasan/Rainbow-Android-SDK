/**
 * 
 */
package com.ale.infra.http.adapter.concurrent;

/**
 * @author fred
 * 
 */
public interface IAsyncServiceVoidCallback
{
	
	/**
	 * Method called when an asynchronous soap request finish
	 * 
	 * @param asyncResult
	 */
	void handleResult(AsyncServiceResponseVoid asyncResult);
}
