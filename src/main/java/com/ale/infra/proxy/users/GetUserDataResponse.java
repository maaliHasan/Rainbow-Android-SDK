package com.ale.infra.proxy.users;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.contact.EmailAddress;
import com.ale.infra.contact.IContact;
import com.ale.infra.contact.PhoneNumber;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by wilsius on 02/08/16.
 */
public class GetUserDataResponse extends RestResponse {
    private static final String LOG_TAG = "GetUserDataResponse";
    private Contact contactResult;

    public GetUserDataResponse (String data) throws Exception {
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, ">GetUserDataResponse; "+data);

        contactResult = new Contact();
        JSONObject obj = new JSONObject(data);

        JSONObject json = obj.getJSONObject("data");
        DirectoryContact contact = new DirectoryContact();
        contact.setCorporateId(json.optString(ID, null));
        contact.setImJabberId(json.optString(JID_IM, null));
        contact.setJidTel(json.optString(JID_TEL, null));
        contact.setLastName(json.optString(LASTNAME, null));
        contact.setFirstName(json.optString(FIRSTNAME, null));
        contact.setTitle(json.optString(TITLE, ""));
        contact.setJobTitle(json.optString(JOBTITLE, ""));
        contact.setCountry(json.optString(COUNTRY, null));
        contact.setLanguage(json.optString(LANGUAGE, null));
        contact.setTimeZone(json.optString(TIMEZONE, null));
        contact.setCompanyId(json.optString(COMPANY_ID, null));
        contact.setCompanyName(json.optString(COMPANY_NAME, null));
        contact.setLoginEmail(json.optString(LOGINEMAIL, null));
        contact.setLastAvatarUpdateDate(json.optString(LAST_AVATAR_UPDATE_DATE, null));


        if(json.has(ROLES)) {
            JSONArray roles = json.getJSONArray(ROLES);
            if (roles != null && roles.length() > 0) {
                for (int i = 0; i < roles.length(); ++i) {
                    String currentObj = roles.getString(i);
                    if(!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(currentObj))
                        contact.getRole().add(IContact.ContactRole.fromString(currentObj));
                }
            }
        }

        if( json.has(EMAILS)) {
            JSONArray emails = json.getJSONArray(EMAILS);
            if (emails != null && emails.length() > 0) {
                for (int emailCtr = 0; emailCtr < emails.length(); ++emailCtr) {
                    JSONObject currentEmailObj = emails.getJSONObject(emailCtr);
                    String emailType = currentEmailObj.getString(EMAILS_TYPE);
                    String emailValue = currentEmailObj.getString(EMAILS_VALUE);
                    if (!StringsUtil.isNullOrEmpty(emailType) && emailValue != null) {
                        switch (emailType) {
                            case EMAILS_TYPE_WORK:
                                contact.addEmailAddress(emailValue, EmailAddress.EmailType.WORK);
                                break;
                            case EMAILS_TYPE_HOME:
                                contact.addEmailAddress(emailValue, EmailAddress.EmailType.HOME);
                                break;
                        }
                    }
                }
            }
        }

        if (!json.isNull(PHONE_NUMBERS)) {
            JSONArray phones = json.getJSONArray(PHONE_NUMBERS);
            if (phones != null && phones.length() > 0) {
                for (int i = 0; i < phones.length(); i++) {
                    JSONObject currentPhoneObj = phones.getJSONObject(i);
                    String number ="";
                    String numberE164 ="";
                    String country ="";
                    String phoneNumberId = "";
                    boolean isFromSystem = false;
                    String systemId ="";
                    String pbxId = "";
                    String type = "";
                    String deviceType ="";
                    if (currentPhoneObj.has(NUMBER)) number = currentPhoneObj.getString(NUMBER);
                    if (currentPhoneObj.has(NUMBER_E164)) numberE164 = currentPhoneObj.getString(NUMBER_E164);
                    if (currentPhoneObj.has(COUNTRY)) country = currentPhoneObj.getString(COUNTRY);
                    if (currentPhoneObj.has(PHONE_NUMBER_ID)) phoneNumberId = currentPhoneObj.getString(PHONE_NUMBER_ID);
                    if (currentPhoneObj.has(IS_FROM_SYSTEM)) isFromSystem = currentPhoneObj.getBoolean(IS_FROM_SYSTEM);
                    if (currentPhoneObj.has(SYSTEM_ID)) systemId = currentPhoneObj.getString(SYSTEM_ID);

                    if (currentPhoneObj.has(PBX_ID)) pbxId = currentPhoneObj.getString(PBX_ID);
                    if (currentPhoneObj.has(TYPE)) type = currentPhoneObj.getString(TYPE);
                    if (currentPhoneObj.has(DEVICE_TYPE)) deviceType = currentPhoneObj.getString(DEVICE_TYPE);

                    PhoneNumber phoneNumber = new PhoneNumber(number, getDeviceType(type, deviceType));
                    phoneNumber.setPhoneNumberE164(numberE164);
                    phoneNumber.setCountry(country);
                    phoneNumber.setPhoneNumberId(phoneNumberId);
                    phoneNumber.setIsFromSystem(isFromSystem);
                    phoneNumber.setSystemId(systemId);
                    phoneNumber.setPbxId(pbxId);
                    contact.addPhoneNumber(phoneNumber);
                }
            }
        }


        contactResult.setDirectoryContact(contact);
    }
    public Contact getContact() {
        return contactResult;
    }

    private PhoneNumber.PhoneNumberType getDeviceType(String type, String deviceType) {
        if ("home".equalsIgnoreCase(type)) {
            if ("landline".equalsIgnoreCase((deviceType)))
                return PhoneNumber.PhoneNumberType.HOME;
            else if ("fax".equalsIgnoreCase((deviceType)))
                return PhoneNumber.PhoneNumberType.FAX_HOME;
            else if ("mobile".equalsIgnoreCase((deviceType)))
                return PhoneNumber.PhoneNumberType.MOBILE;
            else    return PhoneNumber.PhoneNumberType.OTHER;
        } else if ("work".equalsIgnoreCase(type)) {
            if ("landline".equalsIgnoreCase((deviceType)))
                return PhoneNumber.PhoneNumberType.COMPANY_MAIN;
            else if ("fax".equalsIgnoreCase((deviceType)))
                return PhoneNumber.PhoneNumberType.FAX_WORK;
            else if ("mobile".equalsIgnoreCase((deviceType)))
                return PhoneNumber.PhoneNumberType.WORK_MOBILE;
            else
                return PhoneNumber.PhoneNumberType.OTHER;
        } else
            return PhoneNumber.PhoneNumberType.OTHER;
    }
}
