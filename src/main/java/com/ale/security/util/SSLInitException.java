/**
 * 
 */
package com.ale.security.util;

/**
 * @author D3MABIL
 * 
 */
public class SSLInitException extends Exception
{
	
	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = -3166697349888492136L;
	
	/**
	 * 
	 */
	public SSLInitException()
	{
		super();
	}
	
	/**
	 * @param msg
	 * @param t
	 */
	public SSLInitException(String msg, Throwable t)
	{
		super(msg, t);
	}
	
	/**
	 * @param msg
	 */
	public SSLInitException(String msg)
	{
		super(msg);
	}
	
	/**
	 * @param t
	 */
	public SSLInitException(Throwable t)
	{
		super(t);
	}
	
}
