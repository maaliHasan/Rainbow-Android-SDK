/**
 * 
 */
package com.ale.security.util;

/**
 * A Basic scheme
 */
public class BasicCredential
{
	
	private String login;
	private String password;
	
	/**
	 * Construct a new BasicCredential with the specified login and password.
	 * 
	 * @param login
	 *            the login.
	 * @param password
	 *            the password.
	 */
	public BasicCredential(String login, String password)
	{
		this.login = login;
		this.password = password;
	}
	
	/**
	 * @return  the login
	 * @uml.property  name="login"
	 */
	public String getLogin()
	{
		return login;
	}
	
	/**
	 * @return  the password
	 * @uml.property  name="password"
	 */
	public String getPassword()
	{
		return password;
	}
	
}
