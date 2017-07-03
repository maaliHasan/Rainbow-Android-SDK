/******************************************************************************
 * Copyright � 2011 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : cebruckn 29 juin 2011
 * *****************************************************************************
 * Defects
 * 2011/11/03 cebruckn crms00344414 [Crash]-XmppContact Details
 * 2012/01/04 cebruckn crms00354119 [Favorites]-Wrong detail sheet displayed
 * 2013/01/09 cebruckn crms00414373 Call Grouping not 100% accurate
 * 2013/01/11 cebruckn crms00414859 Acra crash reports analysis and corrections from 2.0.29.3
 * 2013/02/27 cebruckn crms00423393 XML Eventing-PhotoURL not anymore propagated
 */

package com.ale.infra.contact;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.data_model.IMultiSelectable;
import com.ale.infra.searcher.IDisplayable;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author cebruckn
 */
public class Contact implements IMultiSelectable, IContact.IContactListener, IDisplayable, IRainbowContact
{
    public static final String ORANGE = "#ff4500";
    public static final String YELLOW = "#d38700";
    public static final String GREEN = "#348833";
    public static final String DARKGREEN = "#007356";
    public static final String TURQUOISE = "#00b2a9";
    public static final String BLUE = "#00b0e5";
    public static final String DARKBLUE = "#0085ca";
    public static final String PURPLE = "#6639b7";
    public static final String PINK = "#91278a";
    public static final String MAGENTA = "#cf0072";
    public static final String BURBON = "#a50034";
    public static final String RED = "#d20000";
    final private String LOG_TAG = "Contact";

    private Set<ContactListener> m_changeListeners = new HashSet<>();

    private String m_invitationId = null;
    private LocalContact localContact = null;
    private DirectoryContact directoryContact = null;
    private List<IContact> orderedContactList = new LinkedList<>();
    private boolean rosterInProgress = false;
    private String role;
    private boolean m_clickActionInProgress = false;

    public Contact()
    {
        refreshOrderedContact();
    }

    public Contact(DirectoryContact directoryContact, LocalContact localContact)
    {
        setLocalContact(localContact);
        setDirectoryContact(directoryContact);
        refreshOrderedContact();
    }

    public LocalContact getLocalContact()
    {
        return this.localContact;
    }

    public DirectoryContact getDirectoryContact()
    {
        return this.directoryContact;
    }

    public synchronized void setDirectoryContact(DirectoryContact directoryContact)
    {
        if (directoryContact != null)
        {
            RainbowPresence oldpresence = null;
            if (this.directoryContact != null)
            {
                this.directoryContact.unregisterChangeListener(this);
                oldpresence = this.directoryContact.getPresence();
            }
            this.directoryContact = directoryContact;
            this.directoryContact.setPresence(null, oldpresence);
            this.directoryContact.registerChangeListener(this);
            refreshOrderedContact();
            notifyContactUpdated();
        }
    }

    public synchronized void setLocalContact(LocalContact localContact)
    {
        if (localContact != null)
        {
            if (this.localContact != null)
            {
                this.localContact.unregisterChangeListener(this);
            }
            this.localContact = localContact;
            this.localContact.registerChangeListener(this);
            refreshOrderedContact();
            notifyContactUpdated();
        }
    }



    public synchronized void removeLocalContact()
    {
        if (localContact != null)
        {
            localContact.unregisterChangeListener(this);
            localContact = null;
            notifyContactUpdated();
        }
    }

    public synchronized void removeDirectoryContact()
    {
        if (directoryContact != null)
        {
            directoryContact.unregisterChangeListener(this);
            directoryContact = null;
            notifyContactUpdated();
        }
    }


    private synchronized void refreshOrderedContact()
    {
        orderedContactList.clear();

        // Order Contact List with following priorities :
        // 1) Local contact
        if (localContact != null)
            orderedContactList.add(localContact);
        // 2) Rest Contact
        if (directoryContact != null)
            orderedContactList.add(directoryContact);
    }

    public void setPresenceSettings(RainbowPresence presence)
    {
        if (directoryContact != null)
        {
            directoryContact.setPresenceSettings(presence);
        }
    }

    public void setLoginEmail(String email)
    {
        if (directoryContact != null)
        {
            directoryContact.setLoginEmail(email);
        }
    }

    public String getLoginEmail()
    {
        return  directoryContact.getLoginEmail();
    }

    public void setPresence(String resourceIdFromPresence, RainbowPresence pres)
    {
        if (directoryContact != null)
        {
            directoryContact.setPresence(resourceIdFromPresence, pres);
        }
    }

    public void setTelPresence(String resourceIdFromPresence, RainbowPresence pres)
    {
        if (directoryContact != null)
        {
            directoryContact.setTelPresence(resourceIdFromPresence, pres);
        }
    }

    public String getContactId()
    {
        String jid = getImJabberId();
        if (!StringsUtil.isNullOrEmpty(jid))
        {
            return jid;
        }
        if (localContact != null && !StringsUtil.isNullOrEmpty(localContact.getId()))
        {
            return localContact.getId();
        }
        return "";
    }

    public String getImJabberId()
    {
        if (directoryContact != null && !StringsUtil.isNullOrEmpty(directoryContact.getImJabberId()))
        {
            return directoryContact.getImJabberId();
        }
        return "";
    }

    public boolean isCorporate()
    {
        return directoryContact != null;
    }

    public boolean isInvited () {
       return  RainbowContext.getInfrastructure().getInvitationMgr().isInvited(this);
    }

    public boolean isNative()
    {
        return localContact != null;
    }

    public boolean isRoster()
    {
        return directoryContact != null && directoryContact.isRoster();
    }

    public boolean isBot()
    {
        return directoryContact != null && directoryContact.isBot();
    }

    public void setIsRoster(boolean b)
    {
        synchronized (this)
        {
            if (directoryContact != null)
            {
                directoryContact.setIsRoster(b);
                notifyContactUpdated();
            }
        }

        if (RainbowContext.getInfrastructure().getDatabaseMgr()!= null) {
            RainbowContext.getInfrastructure().getDatabaseMgr().getContactDataSource().setIsRoster(this, b);
        }
    }

    public synchronized String getFirstName()
    {
        for (IContact contact : orderedContactList)
        {
            String info = contact.getFirstName();
            if (!StringsUtil.isNullOrEmpty(info))
                return info;
        }
        return "";
    }

    public synchronized String getLastName()
    {
        for (IContact contact : orderedContactList)
        {
            String info = contact.getLastName();
            if (!StringsUtil.isNullOrEmpty(info))
                return info;
        }
        return "";
    }

    public synchronized String getNickName()
    {
        for (IContact contact : orderedContactList)
        {
            String info = contact.getNickName();
            if (!StringsUtil.isNullOrEmpty(info))
                return info;
        }
        return "";
    }

    @Override
    public synchronized String getDisplayName(String unknownNameString)
    {
        for (IContact contact : orderedContactList)
        {
            // Give null to test if embedded displayName is null
            String info = contact.getDisplayName(null);
            if (!StringsUtil.isNullOrEmpty(info))
                return info;
        }
        return unknownNameString;
    }

    public synchronized String getDisplayName4Log(String unknownNameString)
    {
        String displayName;
        if (!RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable()) {
            displayName = getInitials(unknownNameString);
        } else {
            displayName = getDisplayName(unknownNameString);
        }
        return displayName;
    }

    public synchronized String getInitials(String unknownNameString)
    {
        for (IContact contact : orderedContactList)
        {
            String info = contact.getInitials();
            if (!StringsUtil.isNullOrEmpty(info))
                return info;
        }
        if( !StringsUtil.isNullOrEmpty(unknownNameString)) {
            return unknownNameString.substring(0,1);
        }
        return "";
    }

    public synchronized PhoneNumber getFirstOfficePhoneNumber()
    {
        for (IContact contact : orderedContactList)
        {
            PhoneNumber info = contact.getFirstOfficePhoneNumber();
            if (info != null)
                return info;
        }
        return null;
    }

    public synchronized String getFirstMobilePhoneNumber()
    {
        for (IContact contact : orderedContactList)
        {
            String info = contact.getFirstMobilePhoneNumber();
            if (!StringsUtil.isNullOrEmpty(info))
                return info;
        }
        return "";
    }
    public synchronized String getFirstPersonalPhoneNumber()
    {
        for (IContact contact : orderedContactList)
        {
            String info = contact.getFirstPersonalPhoneNumber();
            if (!StringsUtil.isNullOrEmpty(info))
                return info;
        }
        return "";
    }

    public synchronized String getFirstPersonalMobilePhoneNumber()
    {
        for (IContact contact : orderedContactList)
        {
            String info = contact.getFirstPersonalMobilePhoneNumber();
            if (!StringsUtil.isNullOrEmpty(info))
                return info;
        }
        return "";
    }

    public synchronized Set<PhoneNumber> getPhoneNumbers()
    {
        Set<PhoneNumber> infos = new HashSet<>();
        for (IContact contact : orderedContactList)
        {
            Set<PhoneNumber> currentPhoneNb = contact.getPhoneNumbers();
            if (currentPhoneNb != null) {
                infos.addAll(currentPhoneNb);
            }
        }
        return infos;
    }

    public synchronized String getFirstAvailableNumber()
    {
        for (IContact contact : orderedContactList)
        {
            String info = contact.getFirstAvailableNumber();
            if (!StringsUtil.isNullOrEmpty(info))
                return info;
        }
        return "";
    }

    public synchronized Set<EmailAddress> getEmailAddresses()
    {
        for (IContact contact : orderedContactList)
        {
            Set<EmailAddress> infos = contact.getEmailAddresses();
            if (infos != null)
                return infos;
        }
        return new HashSet<>();
    }

    public synchronized String getMainEmailAddress()
    {
        for (EmailAddress email : getEmailAddresses())
        {
            if (email.getType() == EmailAddress.EmailType.WORK)
            {
                return email.getValue();
            }
        }
        return "";
    }

    public synchronized String getFirstEmailAddress()
    {
        for (IContact contact : orderedContactList)
        {
            String emailAddress = contact.getFirstEmailAddress();
            if (!StringsUtil.isNullOrEmpty(emailAddress))
                return emailAddress;
        }
        return "";
    }

    public synchronized String getEmailAddressForType(EmailAddress.EmailType type)
    {
        for (EmailAddress email : getEmailAddresses())
        {
            if (email.getType() == type)
            {
                return email.getValue();
            }
        }

        return "";
    }

    public synchronized String getCorporateId()
    {
        for (IContact contact : orderedContactList) {
            String info = contact.getCorporateId();
            if (!StringsUtil.isNullOrEmpty(info))
                return info;
        }
        return "";
    }

    public synchronized String getCompanyName()
    {
        for (IContact contact : orderedContactList)
        {
            String info = contact.getCompanyName();
            if (!StringsUtil.isNullOrEmpty(info))
                return info;
        }
        return "";
    }

    public synchronized String getCompanyId()
    {
        for (IContact contact : orderedContactList)
        {
            String info = contact.getCompanyId();
            if (!StringsUtil.isNullOrEmpty(info))
                return info;
        }
        return "";
    }

    public String getJobTitle()
    {
        for (IContact contact : orderedContactList)
        {
            String info = contact.getJobTitle();
            if (!StringsUtil.isNullOrEmpty(info))
                return info;
        }
        return "";
    }

    @Override
    public void setInvitationId(String invitationId) {
        m_invitationId = invitationId;
    }

    @Override
    public String getInvitationId() {
        return m_invitationId;
    }

    public List<IContact.ContactRole> getRoles() {
        for (IContact contact : orderedContactList)
        {
            if (contact.getRole() != null)
                return contact.getRole();
        }
        return null;
    }

    public synchronized String getWorkAddress()
    {
        for (IContact contact : orderedContactList)
        {
            String info = contact.getWorkAddress();
            if (!StringsUtil.isNullOrEmpty(info))
                return info;
        }
        return "";
    }

    public synchronized Set<PostalAddress> getPostalAddresses()
    {
        for (IContact contact : orderedContactList)
        {
            Set<PostalAddress> infos = contact.getPostalAddresses();
            if (infos != null)
                return infos;
        }
        return new HashSet<>();
    }

    public synchronized Bitmap getPhoto()
    {
        for (IContact contact : orderedContactList)
        {
            Bitmap info = contact.getPhoto();
            if (info != null)
                return info;
        }
        return null;
    }

    public void setPhoto(Bitmap photo)
    {
        if (directoryContact != null)
        {
            directoryContact.setPhoto(photo);
        }
    }

    public static String getColorValueFromDisplayName(String displayName) {
        String[] textAvatarColors = new String[]{ORANGE, YELLOW, GREEN, DARKGREEN, TURQUOISE, BLUE, DARKBLUE, PURPLE, PINK, MAGENTA, BURBON, RED};

        // make display name with uppercases only
        String upperCaseDisplayName = displayName.toUpperCase();
        int sum = 0;
        for (int charCtr = 0; charCtr < upperCaseDisplayName.length(); charCtr++) {
            sum += upperCaseDisplayName.charAt(charCtr);
        }

        //get the color from the array
        return textAvatarColors[(sum % 12)];
    }

    public static int getColorFromDisplayName(String contactName) {

        return Color.parseColor(getColorValueFromDisplayName(contactName));
    }


    public RainbowPresence getPresence()
    {
        if (directoryContact != null)
        {
            return directoryContact.getPresence();
        }
        return null;
    }

    public synchronized void notifyContactUpdated()
    {

        for (ContactListener listener : m_changeListeners.toArray(new ContactListener[m_changeListeners.size()]))
        {
            listener.contactUpdated(Contact.this);
        }
    }

    public synchronized void notifyPresenceChanged()
    {
        for (ContactListener listener : m_changeListeners.toArray(new ContactListener[m_changeListeners.size()]))
        {
            listener.onPresenceChanged(Contact.this, getPresence());
        }
    }

    public synchronized void notifyOnActionInProgress()
    {

        for (ContactListener listener : m_changeListeners.toArray(new ContactListener[m_changeListeners.size()]))
        {
            listener.onActionInProgress(m_clickActionInProgress);
        }
    }

    @Override
    public void contactUpdated(IContact updatedContact) {
        notifyContactUpdated();
    }

    @Override
    public void onPresenceChanged(RainbowPresence presence)
    {
        notifyPresenceChanged();
    }

    public synchronized void registerChangeListener(ContactListener changeListener)
    {
        if( !m_changeListeners.contains(changeListener)) {
            m_changeListeners.add(changeListener);
        }
    }

    public synchronized void unregisterChangeListener(ContactListener changeListener)
    {
        m_changeListeners.remove(changeListener);
    }

    public boolean isRosterInProgress()
    {
        return rosterInProgress;
    }

    public void setRosterInProgress(boolean rosterInProgress)
    {
        this.rosterInProgress = rosterInProgress;
    }

    public boolean hasEmailAddress(String emailAddress){
            for (EmailAddress email : this.getEmailAddresses().toArray(new EmailAddress[this.getEmailAddresses().size()])) {
                if (email.getValue().equals(emailAddress)) {
                    return true;
                }
            }
            return false;
    }

    @Override
    public synchronized boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact)) return false;

        Contact contact = (Contact) o;

        if (contact.getImJabberId() != null && this.getImJabberId() != null && contact.getImJabberId().equalsIgnoreCase(this.getImJabberId())) return true;

        if (rosterInProgress != contact.rosterInProgress) return false;
        if (localContact != null && !localContact.equals(contact.localContact)) return false;
        if (directoryContact != null && !directoryContact.equals(contact.directoryContact)) return false;
        if (orderedContactList != null && !orderedContactList.equals(contact.orderedContactList)) return false;
        return role.equals(contact.role);
    }


    @Override
    public synchronized int hashCode() {
        int result = 0;
        if( localContact != null ) result = 31 * result + localContact.hashCode();
        if( directoryContact != null ) result = 31 * result + directoryContact.hashCode();
        if( role != null ) result = 31 * result + role.hashCode();
        result = 31 * result + (rosterInProgress ? 1 : 0);
        return result;
    }

    public boolean isValid(){

        if (isCorporate()) {
            if (getDirectoryContact().getLastName() == null && getDirectoryContact().getFirstName() == null && getDirectoryContact().getEmailAddresses().isEmpty())
                return false;
        }
        return true;

    }

    public boolean isClickActionInProgress() {
        return m_clickActionInProgress;
    }

    public void setClickActionInProgress(boolean clickActionInProgress) {
        m_clickActionInProgress = clickActionInProgress;
        notifyOnActionInProgress();
    }

    public void dumpInLog(String dumpLogTag) {
        if( localContact == null ) {
            Log.getLogger().info(dumpLogTag, "    **  No Local Contact");
        } else {
            Log.getLogger().info(dumpLogTag, "    **  Local Contact:");
            localContact.dumpInLog(dumpLogTag);
        }
        if( directoryContact == null ) {
            Log.getLogger().info(dumpLogTag, "    **  No Directory Contact");
        } else {
            Log.getLogger().info(dumpLogTag, "    **  Directory Contact:");
            directoryContact.dumpInLog(dumpLogTag);
        }
    }

    @Override
    public int getSelectableType() {
        return 0;
    }

    public String getCountry() {
        if( this.directoryContact != null)
            return this.directoryContact.getCountry();
        return null;
    }

    public boolean isOnlyUser() {
        List<IContact.ContactRole> roles = this.getRoles();
        if( roles != null && getRoles().size() >= 1 ) {
            for(IContact.ContactRole role : roles) {
                if( !role.equals(IContact.ContactRole.USER) )
                    return false;
            }
            return true;
        }
        return true;
    }

    public synchronized void merge(Contact contact) {
        if( contact == null || contact.getDirectoryContact() == null )
            return;

        if( this.directoryContact == null)
            this.directoryContact = contact.getDirectoryContact();
        else
            this.directoryContact.merge(contact.getDirectoryContact());

        if( this.localContact == null)
            this.localContact = contact.getLocalContact();
    }

    public interface ContactListener
    {
        /**
         * called when contact has been updated
         */
        void contactUpdated(Contact updatedContact);

        /**
         * called when the contact's presence changes
         */
        void onPresenceChanged(Contact contact, RainbowPresence presence);

        //called when ActionInProgress
        void onActionInProgress(final boolean clickActionInProgress);
    }

}
