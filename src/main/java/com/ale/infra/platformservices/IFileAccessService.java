/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : IFileAccessService.java
 * Summary : 
 ******************************************************************************
 * History
 * 
 */
package com.ale.infra.platformservices;

import java.io.File;

/**
 * The service provided by the plateform to handle file access.
 * 
 */
public interface IFileAccessService
{
	
	/**
	 * Get the directory specified by its name. create it if it doesn't exist.
	 * 
	 * @param name
	 *            the directory.
	 * @return the File object representing this directory.
	 */
	File getDirectory(String name);
}
