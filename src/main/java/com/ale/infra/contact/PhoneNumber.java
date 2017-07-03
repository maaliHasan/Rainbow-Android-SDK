/******************************************************************************
 * Copyright � 2013 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : cebruckn 8 janv. 2013
 ******************************************************************************
 * Defects
 *
 */

package com.ale.infra.contact;

import com.ale.util.StringsUtil;

/**
 * @author cebruckn
 * 
 */
public class PhoneNumber
{
	public enum PhoneNumberType
	{
		WORK,
		FAX_WORK,
		FAX_HOME,
		WORK_MOBILE,
		MAIN,
		OFFICE,
		COMPANY_MAIN,
		MOBILE,
		HOME,
		ASSISTANT,
		CUSTOM,
		UNKNOWN,
		OTHER;
		
		public String value()
		{
			return name();
		}
		
		public static PhoneNumberType fromValue(String v)
		{
			return valueOf(v);
		}
	}


	private String m_phoneNumberValue;
	private PhoneNumberType m_phoneNumberType;


	private String m_phoneNumberE164;
	private String country;
	private String phoneNumberId;
	private boolean isFromSystem;
	private String systemId;
	private String pbxId;


	
	public String getPhoneNumberValue()
	{
		if (StringsUtil.isNullOrEmpty(m_phoneNumberE164))
			return m_phoneNumberValue;
		else
			return m_phoneNumberE164;
	}
	
	public PhoneNumberType getPhoneNumberType()
	{
		return m_phoneNumberType;
	}
	
	public PhoneNumber(String value, PhoneNumberType type)
	{
		m_phoneNumberValue = value;
		m_phoneNumberType = type;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_phoneNumberType == null) ? 0 : m_phoneNumberType.hashCode());
		result = prime * result + ((m_phoneNumberValue == null) ? 0 : m_phoneNumberValue.hashCode());
		return result;
	}

	public String getPhoneNumberE164() {
		return m_phoneNumberE164;
	}

	public void setPhoneNumberE164(String m_phoneNumberE164) {
		this.m_phoneNumberE164 = m_phoneNumberE164;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPhoneNumberId() {
		return phoneNumberId;
	}

	public void setPhoneNumberId(String phoneNumberId) {
		this.phoneNumberId = phoneNumberId;
	}

	public boolean isFromSystem() {
		return isFromSystem;
	}

	public void setIsFromSystem(boolean isFromSystem) {
		this.isFromSystem = isFromSystem;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getPbxId() {
		return pbxId;
	}

	public void setPbxId(String pbxId) {
		this.pbxId = pbxId;
	}


	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PhoneNumber other = (PhoneNumber) obj;
		if (m_phoneNumberType != other.m_phoneNumberType)
			return false;
		if (m_phoneNumberValue == null)
		{
			if (other.m_phoneNumberValue != null)
				return false;
		}
		else if (!m_phoneNumberValue.equals(other.m_phoneNumberValue))
			return false;
		return true;
	}
	
}
