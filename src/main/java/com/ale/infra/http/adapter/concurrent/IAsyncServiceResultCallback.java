/**
 *
 */
package com.ale.infra.http.adapter.concurrent;

/**
 * The Interface IAsyncServiceResultCallback.
 *
 * @param <T> the generic result type
 * @author fred
 */
public interface IAsyncServiceResultCallback<T>
{
    /**
     * Method called when an asynchronous soap request finish
     *
     * @param asyncResult
     */
    void handleResult(AsyncServiceResponseResult<T> asyncResult);
}
