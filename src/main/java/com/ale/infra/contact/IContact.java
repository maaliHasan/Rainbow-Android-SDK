/******************************************************************************
 * Copyright � 2011 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : cebruckn 29 juin 2011
 * *****************************************************************************
 * Defects
 * 2013/01/09 cebruckn crms00414373 Call Grouping not 100% accurate
 */

package com.ale.infra.contact;

import android.graphics.Bitmap;

import com.ale.infra.searcher.IDisplayable;

import java.util.List;
import java.util.Set;


public interface IContact extends IDisplayable
{
    // unique identifier of the contact
    String getId();

    // jabber id :
    String getImJabberId();

    void setImJabberId(String mail);

    boolean isCorporate();

    boolean isNative();

    // name/firstname :

    String getFirstName();

    void setFirstName(String firstName);

    String getLastName();

    void setLastName(String lastName);

    String getNickName();

    void setNickName(String nickname);

    String getDisplayName(String unknownNameString);

    String getDisplayName4Log(String unknownNameString);

    // get contact initials:
    String getInitials();

    // phone numbers:
    void addPhoneNumber(String phoneNumber, String phoneNbE164, PhoneNumber.PhoneNumberType type);

    void addPhoneNumber( PhoneNumber phoneNumber);

    PhoneNumber getFirstOfficePhoneNumber();

    String getFirstPersonalPhoneNumber();

    String getFirstMobilePhoneNumber();

    String getFirstPersonalMobilePhoneNumber();

    Set<PhoneNumber> getPhoneNumbers();

    String getFirstAvailableNumber();

    // emails:
	String getMainEmailAddress();

    String getFirstEmailAddress();

    Set<EmailAddress> getEmailAddresses();

    Set<WebSite> getWebSites();

    void addEmailAddress(String email, EmailAddress.EmailType type);

    String getEmailWithType(EmailAddress.EmailType type);

    String getCorporateId();

    // corporate info
    void setCorporateId(String companyId);

    String getCompanyName();

    void setCompanyName(String companyName);

    String getCompanyId();

    void setCompanyId(String companyId);

    String getJobTitle();

    void setJobTitle(String title);

    List<ContactRole> getRole();

    // postal addresses:
    String getWorkAddress();

    Set<PostalAddress> getPostalAddresses();

    void addPostalAddress(String address, PostalAddress.AddressType type);

    // photo:
    Bitmap getPhoto();

    void setPhoto(Bitmap photoBitmap);

    void fillEmptyFieldsWithContact(IContact contact);

    void registerChangeListener(IContactListener changeListener);

    void unregisterChangeListener(IContactListener changeListener);

    void addWebSite(String string, WebSite.WebSiteType type);

    enum ContactRole
    {
        USER("user"), ADMIN("admin"), GUEST("guest"), UNDEFINED("undefined");
        private String value;

        ContactRole(String value) {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return value;
        }

        public static ContactRole fromString(String text) {
            if (text != null) {
                for (ContactRole status : ContactRole.values()) {
                    if (text.equalsIgnoreCase(status.value)) {
                        return status;
                    }
                }
            }
            return UNDEFINED;
        }
    }

    interface IContactListener
    {
        /**
         * called when contact has been updated
         */

        void contactUpdated(IContact updatedContact);

        /**
         * called when the contact's presence changes
         */

        void onPresenceChanged(RainbowPresence presence);
    }
}
