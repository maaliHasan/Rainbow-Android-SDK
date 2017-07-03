package com.ale.infra.proxy.directory;

import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.contact.EmailAddress;
import com.ale.infra.contact.IContact;
import com.ale.infra.contact.PhoneNumber;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.rainbow.JSONParser;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchResponse extends RestResponse {

    private static final String LOG_TAG = "SearchResponse";

    private List<DirectoryContact> m_contacts = new ArrayList<>();


    public SearchResponse(IPlatformServices platformServices, String search) throws Exception {
        if (platformServices.getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, ">SearchResponse; " + search);

        JSONObject usersFounded = new JSONObject(search);
            if (usersFounded.has("data")) {
                JSONArray arrayUsersFounded = usersFounded.getJSONArray("data");
                if (arrayUsersFounded != null) {
                    for (int userCtr = 0; userCtr < arrayUsersFounded.length(); userCtr++) {
                        JSONObject jsonObj = arrayUsersFounded.getJSONObject(userCtr);
                        JSONParser json = new JSONParser(jsonObj);

                        String loginEmail = "";
                        DirectoryContact contact = new DirectoryContact();
                        if (jsonObj.has(ID))
                            contact.setCorporateId(json.getString(ID));
                        if (jsonObj.has(JID_IM)) {
                            contact.setImJabberId(json.getString(JID_IM));
                            Log.getLogger().verbose(LOG_TAG, "Parsing JID=" + contact.getImJabberId());
                        }
                        if (jsonObj.has(JID_TEL))
                            contact.setJidTel(json.getString(JID_TEL));
                        if (jsonObj.has(LASTNAME))
                            contact.setLastName(json.getString(LASTNAME));
                        if (jsonObj.has(FIRSTNAME))
                            contact.setFirstName(json.getString(FIRSTNAME));
                        if (jsonObj.has(NICKNAME))
                            contact.setNickName(json.getString(NICKNAME));

                        if (jsonObj.has(TITLE))
                            contact.setTitle(json.getString(TITLE));
                        if (jsonObj.has(JOBTITLE))
                            contact.setJobTitle(json.getString(JOBTITLE));
                        if (jsonObj.has(COUNTRY))
                            contact.setCountry(json.getString(COUNTRY));
                        if (jsonObj.has(LANGUAGE))
                            contact.setLanguage(json.getString(LANGUAGE));
                        if (jsonObj.has(TIMEZONE))
                            contact.setTimeZone(json.getString(TIMEZONE));
                        if (jsonObj.has(COMPANY_ID))
                            contact.setCompanyId(json.getString(COMPANY_ID));
                        if (jsonObj.has(COMPANY_NAME))
                            contact.setCompanyName(json.getString(COMPANY_NAME));
                        if (jsonObj.has(LOGINEMAIL)) {
                            loginEmail = json.getString(LOGINEMAIL);
                            contact.setLoginEmail(loginEmail);
                        }
                        if (jsonObj.has(LAST_AVATAR_UPDATE_DATE))
                            contact.setLastAvatarUpdateDate(json.getString(LAST_AVATAR_UPDATE_DATE));

                        if (jsonObj.has(ROLES)) {
                            JSONArray roles = jsonObj.getJSONArray(ROLES);
                            if (roles != null && roles.length() > 0) {
                                for (int i = 0; i < roles.length(); ++i) {
                                    String currentObj = roles.getString(i);
                                    if (!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(currentObj))
                                        contact.getRole().add(IContact.ContactRole.fromString(currentObj));
                                }
                            }
                        }

                        if (!jsonObj.isNull(EMAILS)) {
                            JSONArray emails = jsonObj.getJSONArray(EMAILS);
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
                                            default:
                                                contact.addEmailAddress(emailValue, EmailAddress.EmailType.OTHER);
                                                break;
                                        }
                                    }
                                }
                            }
                        }

                        if (!jsonObj.isNull(PHONE_NUMBERS)) {
                            JSONArray phones = jsonObj.getJSONArray(PHONE_NUMBERS);
                            if (phones != null && phones.length() > 0) {
                                for (int i = 0; i < phones.length(); i++) {
                                    JSONObject currentPhoneObj = phones.getJSONObject(i);
                                    String number = "";
                                    String numberE164 = "";
                                    String country = "";
                                    String phoneNumberId = "";
                                    boolean isFromSystem = false;
                                    String systemId = "";
                                    String pbxId = "";
                                    String type = "";
                                    String deviceType = "";
                                    if (currentPhoneObj.has(NUMBER))
                                        number = currentPhoneObj.getString(NUMBER);
                                    if (currentPhoneObj.has(NUMBER_E164))
                                        numberE164 = currentPhoneObj.getString(NUMBER_E164);
                                    if (currentPhoneObj.has(COUNTRY))
                                        country = currentPhoneObj.getString(COUNTRY);
                                    if (currentPhoneObj.has(PHONE_NUMBER_ID))
                                        phoneNumberId = currentPhoneObj.getString(PHONE_NUMBER_ID);
                                    if (currentPhoneObj.has(IS_FROM_SYSTEM))
                                        isFromSystem = currentPhoneObj.getBoolean(IS_FROM_SYSTEM);
                                    if (currentPhoneObj.has(SYSTEM_ID))
                                        systemId = currentPhoneObj.getString(SYSTEM_ID);

                                    if (currentPhoneObj.has(PBX_ID))
                                        pbxId = currentPhoneObj.getString(PBX_ID);
                                    if (currentPhoneObj.has(TYPE))
                                        type = currentPhoneObj.getString(TYPE);
                                    if (currentPhoneObj.has(DEVICE_TYPE))
                                        deviceType = currentPhoneObj.getString(DEVICE_TYPE);

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

                        m_contacts.add(contact);
                    }
                }
        }
    }

    public List<DirectoryContact> getContacts() {
        return m_contacts;
    }

        private PhoneNumber.PhoneNumberType getDeviceType(String type, String deviceType) {
        if ("home".equalsIgnoreCase(type)) {
            if ("landline".equalsIgnoreCase((deviceType)))
                return PhoneNumber.PhoneNumberType.HOME;
            else if ("fax".equalsIgnoreCase((deviceType)))
                return PhoneNumber.PhoneNumberType.FAX_HOME;
            else if ("mobile".equalsIgnoreCase((deviceType)))
                return PhoneNumber.PhoneNumberType.MOBILE;
            else
                return PhoneNumber.PhoneNumberType.OTHER;
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
