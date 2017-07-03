/**
 * 
 */
package com.ale.security.util;

/**
 * An authorization request.
 */
public class AuthorizationRequest
{
	
	/**
	 * @uml.property  name="scheme"
	 * @uml.associationEnd  
	 */
	private HttpAuthorizationScheme scheme;
	private String realm;

	/**
	 * Construct a new AuthorizationRequest from the specified header.
	 *
	 * @param header
	 *            the header field.
	 */
	public AuthorizationRequest(String header)
	{
		parseHeader(header);
	}

	/**
	 * Parser the WWW-Authenticate header
	 *
	 * @param header
	 */
	private void parseHeader(String header)
	{

		int index = header.indexOf(' ');
		if (index <= 0)
		{
			throw new StringIndexOutOfBoundsException();
		}
		String schemePart = header.substring(0, index).trim().toLowerCase();
		String realmPart = header.substring(index + 1).trim();

		if ("basic".equals(schemePart))
		{
			scheme = HttpAuthorizationScheme.BASIC;
		}
		else
		{
			scheme = com.ale.security.util.HttpAuthorizationScheme.DIGEST;
		}

		index = realmPart.indexOf('=');
		if (index <= 0)
		{
			throw new StringIndexOutOfBoundsException();
		}

		String realms = realmPart.substring(index + 1).trim();
		if (realms.startsWith("\"") && realms.endsWith("\""))
		{
			realm = realms.substring(1, realms.length() - 1);
		}
		else
		{
			throw new StringIndexOutOfBoundsException();
		}
	}

	/**
	 * @return  the scheme
	 * @uml.property  name="scheme"
	 */
	public HttpAuthorizationScheme getScheme()
	{
		return scheme;
	}
	
	/**
	 * @return  the realm
	 * @uml.property  name="realm"
	 */
	public String getRealm()
	{
		return realm;
	}
}
