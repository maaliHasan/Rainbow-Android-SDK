package com.ale.infra.contact;

import android.graphics.Bitmap;

import com.ale.infra.xmpp.PresenceWithDate;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by grobert on 18/05/16.
 */
public class DirectoryContact extends AbstractContact
{
    private static final String LOG_TAG = "DirectoryContact";

    private boolean isRoster = false;
    private RainbowPresence presence = RainbowPresence.UNSUBSCRIBED;
    private CalendarPresence m_calendarPresence;
    private Date m_lastPresenceReceivedDate;
    private Map<String, PresenceWithDate> presencesByResources = new HashMap<>();
    private static final String MOBILE_PREFIX = "mobile_";


    private boolean hasNoAvatarOnServer = false;


    private DirectoryContactType type = DirectoryContactType.USER;

    private String lastAvatarUpdateDate;
    private List<ContactRole> role = new ArrayList<>();
    private String country;
    private String language;

    private RainbowPresence presenceSettings; //Value of the presence settings on server
    private String timeZone;
    private String jidTel;
    private String loginEmail;
    private String title;
    private List<Profile> profiles = new ArrayList<>();
    private boolean isDefaultCompany = false;


    public DirectoryContact()
    {
    }

    public RainbowPresence getPresenceSettings() {
        return presenceSettings;
    }

    public void setPresenceSettings(RainbowPresence settingsPresence) {
        this.presenceSettings = settingsPresence;
        for (PresenceWithDate presDate : presencesByResources.values()) {
            presDate.setPresence(settingsPresence);
        }
        notifyDirectoryContactChanged();
    }

    public DirectoryContact(RainbowPresence presence)
    {
        this.presence = presence;
    }

    public DirectoryContact(IContact contact)
    {
        super(contact);
    }

    @Override
    public void fillEmptyFieldsWithContact(IContact contact)
    {
        super.fillEmptyFieldsWithContact(contact);

        if (contact instanceof DirectoryContact)
        {
            DirectoryContact dirContact = (DirectoryContact) contact;

            if (dirContact.getCompanyId() != null)
                this.companyId = dirContact.getCompanyId();
            if (!this.isRoster)
                this.isRoster = dirContact.isRoster();
            if( dirContact.getLastAvatarUpdateDate() != null)
                this.lastAvatarUpdateDate = dirContact.getLastAvatarUpdateDate();
            if (dirContact.getRole() != null  && dirContact.getRole().size() > 0) {
                this.role.clear();
                this.role.addAll(dirContact.getRole());
            }

            // GetUserData returns wrong value for this field
            //this.isDefaultCompany = dirContact.isInDefaultCompany();

            if (dirContact.getCountry() != null)
                this.country = dirContact.getCountry();
            if (dirContact.getLanguage() != null)
                this.language = dirContact.getLanguage();
            if (dirContact.getTimeZone() != null)
                this.timeZone = dirContact.getTimeZone();
            if (dirContact.getJidTel() != null)
                this.jidTel = dirContact.getJidTel();
            if (dirContact.getLoginEmail() != null)
                this.loginEmail = dirContact.getLoginEmail();
            if( dirContact.getProfiles().size() > 0)
                this.profiles = dirContact.getProfiles();

            setTitle(dirContact.getTitle());

            notifyDirectoryContactChanged();
        }
    }

    @Override
    public String getId()
    {
        return getImJabberId();
    }

    @Override
    public boolean isCorporate()
    {
        return true;
    }

    public boolean isRoster()
    {
        return isRoster;
    }

    public void setIsRoster(boolean isRoster)
    {
        this.isRoster = isRoster;
        notifyDirectoryContactChanged();
    }

    private boolean checkPresencesForPresence(RainbowPresence pres)
    {
        for (PresenceWithDate presDate : presencesByResources.values()) {
            if (presDate.getPresence().equals(pres)) {
                Log.getLogger().verbose(LOG_TAG, "presence available found...for contact : " + getDisplayName4Log(""));
                presence = presDate.getPresence();
                return true;
            }
        }
        return false;
    }

    private boolean checkPresencesForXaDndOrBusy()
    {
        long date = 0;
        RainbowPresence presenceToHave = RainbowPresence.OFFLINE;
        for (PresenceWithDate presDate : presencesByResources.values()) {
            if ( presDate.getPresence().equals(RainbowPresence.XA) ||
                    presDate.getPresence().equals(RainbowPresence.DND) ||
                    presDate.getPresence().equals(RainbowPresence.BUSY)) {
                if (presDate.getDate() >= date) {
                    date = presDate.getDate();
                    presenceToHave = presDate.getPresence();
                }
            }
        }
        if (date > 0) {
            presence = presenceToHave;
            return true;
        }

        return false;
    }

    private boolean checkPresencesForJidTel()
    {
        for (PresenceWithDate presDate : presencesByResources.values())
        {
            if (presDate.hasJidTel() && presDate.getPresence().equals(RainbowPresence.DND))
            {
                Log.getLogger().verbose(LOG_TAG, "presence DND phone found...");
                presence = RainbowPresence.DND;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DirectoryContact)) return false;

        DirectoryContact that = (DirectoryContact) o;

        if (isRoster != that.isRoster) return false;
        if (type != that.type) return false;
        if (role != null && !(role == that.role)) return false;
        if (country != null && !country.equals(that.country)) return false;
        if (timeZone != null && !timeZone.equals(that.timeZone)) return false;
        if (jidTel != null && !jidTel.equals(that.jidTel)) return false;
        if (loginEmail != null && !loginEmail.equals(that.loginEmail)) return false;
        if (lastAvatarUpdateDate != null && !lastAvatarUpdateDate.equals(that.lastAvatarUpdateDate)) return false;

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        int result = (isRoster ? 1 : 0);
        if( type != null ) result = 31 * result + type.hashCode();
        if( role != null ) result = 31 * result + role.hashCode();
        if( country != null ) result = 31 * result + country.hashCode();
        if( timeZone != null ) result = 31 * result + timeZone.hashCode();
        if( jidTel != null ) result = 31 * result + jidTel.hashCode();
        if( loginEmail != null ) result = 31 * result + loginEmail.hashCode();
        return result;
    }

    private RainbowPresence updatePresence()
    {
        Log.getLogger().verbose(LOG_TAG, ">updatePresence");

        if (this.presence.equals(RainbowPresence.UNSUBSCRIBED)) {
            presencesByResources.clear();
            return this.presence;
        }

        if (this.presence.equals(RainbowPresence.SUBSCRIBE))
            return this.presence;

        if (checkPresencesForJidTel())
            return this.presence;

        if (checkPresencesForPresence(RainbowPresence.BUSY_AUDIO)) {
            return this.presence;
        }

        if (checkPresencesForPresence(RainbowPresence.BUSY_VIDEO)) {
            return this.presence;
        }

        if (checkPresencesForPresence(RainbowPresence.BUSY_PHONE)) {
            return this.presence;
        }

        if (checkPresencesForPresence(RainbowPresence.MANUAL_AWAY)) {
            return this.presence;
        }

        if (checkPresencesForXaDndOrBusy())
            return this.presence;

        int onlineResourcesNumber = 0;
        int onlineMobileResourcesNumber = 0;
        for (String resource : this.presencesByResources.keySet()) {
            if(presencesByResources.get(resource).getPresence().equals(RainbowPresence.ONLINE))            {
                onlineResourcesNumber++;
                if (resource.startsWith(MOBILE_PREFIX)) {
                    onlineMobileResourcesNumber++;
                }
            }
        }
        if ((onlineMobileResourcesNumber > 0 && onlineResourcesNumber == onlineMobileResourcesNumber)) {
            this.presence = RainbowPresence.MOBILE_ONLINE;
            return this.presence;
        }


        if (checkPresencesForPresence(RainbowPresence.ONLINE))
            return this.presence;

        if (checkPresencesForPresence(RainbowPresence.AWAY))
            return this.presence;

        notifyPresenceChanged();

        return RainbowPresence.OFFLINE;
    }

    public RainbowPresence getPresence()
    {
        if (getType() == DirectoryContactType.BOT) {
            return RainbowPresence.ONLINE;
        } else {
            return presence;
        }
    }

    public void setPresence(String resourceId, RainbowPresence presence)
    {
        if( presence == null) return;

        RainbowPresence oldPresence = this.presence;
        Log.getLogger().verbose(LOG_TAG, ">setPresence=" + presence.toString());

        this.presence = presence;

        if (!StringsUtil.isNullOrEmpty(resourceId))
        {
            boolean hasJidTel = false;
            if (resourceId.startsWith(StringsUtil.JID_TEL_PREFIX)) {
                hasJidTel = true;
            }

            PresenceWithDate presDate = presencesByResources.get(resourceId);
            if( presDate == null )
                presDate = new PresenceWithDate(presence, System.currentTimeMillis(), hasJidTel);
            presDate.setPresence(presence);
            presDate.setDate(System.currentTimeMillis());
            presDate.setHasJidTel(hasJidTel);

            if (presence.isOffline())
            {
                presencesByResources.remove(resourceId);
                if( presencesByResources.size() == 0) {
                    this.presence = presence;
                }
            }
            else
            {
                presencesByResources.put(resourceId, presDate);
            }
        }
        updatePresence();

        if (this.presence != oldPresence)
            notifyPresenceChanged();
    }

    public void merge(DirectoryContact dirContact) {
        if( dirContact == null)
            return;

        if (!StringsUtil.isNullOrEmpty(dirContact.getFirstName()))
            this.firstName = dirContact.getFirstName();

        if (!StringsUtil.isNullOrEmpty(dirContact.getLastName()))
            this.lastName = dirContact.getLastName();

        if (!StringsUtil.isNullOrEmpty(dirContact.getCompanyName()))
            this.companyName = dirContact.getCompanyName();

        if (!StringsUtil.isNullOrEmpty(dirContact.getJobTitle()))
            this.jobTitle = dirContact.getJobTitle();


        if (!StringsUtil.isNullOrEmpty(dirContact.getTitle()))
            this.title = dirContact.getTitle();

        if (!StringsUtil.isNullOrEmpty(dirContact.getNickName()))
            this.nickName = dirContact.getNickName();

        if (dirContact.getPhoneNumbers() != null &&
                dirContact.getPhoneNumbers().size() > 0) {
            // Add all missing PhoneNumbers
            for (PhoneNumber phoneNumber : dirContact.getPhoneNumbers()) {
                if (getPhoneNumber(phoneNumber.getPhoneNumberType()) == null)
                    phoneNumbers.add(phoneNumber);
            }
        }

        if (dirContact.getPhoto() != null)
            this.photo = dirContact.getPhoto();

        if (dirContact.getEmailAddresses() != null && dirContact.getEmailAddresses().size() > 0)
            this.emailAddresses = dirContact.getEmailAddresses();


        if (dirContact.getPostalAddresses() != null && dirContact.getPostalAddresses().size() > 0)
            this.postalAddresses = dirContact.getPostalAddresses();

        if (!StringsUtil.isNullOrEmpty(dirContact.getImJabberId()))
            this.jabberId = dirContact.getImJabberId();


        if (!StringsUtil.isNullOrEmpty(dirContact.getCorporateId()))
            this.corporateId = dirContact.getCorporateId();

        if (!StringsUtil.isNullOrEmpty(dirContact.getCompanyId()))
            this.companyId = dirContact.getCompanyId();

        if (!this.isRoster)
            this.isRoster = dirContact.isRoster();

        // GetUserData returns wrong value for this field
        //this.isDefaultCompany = dirContact.isInDefaultCompany();

        if (dirContact.getLastAvatarUpdateDate() != null)
            this.lastAvatarUpdateDate = dirContact.getLastAvatarUpdateDate();

        if (dirContact.getRole() != null && dirContact.getRole().size() > 0) {
            this.role.clear();
            this.role.addAll(dirContact.getRole());
        }

        if (!StringsUtil.isNullOrEmpty(dirContact.getCountry()))
            this.country = dirContact.getCountry();

        if (!StringsUtil.isNullOrEmpty(dirContact.getLanguage()))
            this.language = dirContact.getLanguage();

        if (!StringsUtil.isNullOrEmpty(dirContact.getTimeZone()))
            this.timeZone = dirContact.getTimeZone();

        if (!StringsUtil.isNullOrEmpty(dirContact.getJidTel()))
            this.jidTel = dirContact.getJidTel();

        if (!StringsUtil.isNullOrEmpty(dirContact.getLoginEmail()))
            this.loginEmail = dirContact.getLoginEmail();

        notifyDirectoryContactChanged();
    }

    public void update(DirectoryContact dirContact) {
        if( dirContact == null)
            return;

        this.firstName = dirContact.getFirstName();
        this.lastName = dirContact.getLastName();
        this.companyName = dirContact.getCompanyName();
        this.jobTitle = dirContact.getJobTitle();
        this.title = dirContact.getTitle();
        this.nickName = dirContact.getNickName();
        this.phoneNumbers = dirContact.getPhoneNumbers();
        this.photo = dirContact.getPhoto();
        this.emailAddresses = dirContact.getEmailAddresses();
        this.postalAddresses = dirContact.getPostalAddresses();
        this.jabberId = dirContact.getImJabberId();
        this.corporateId = dirContact.getCorporateId();
        this.companyId = dirContact.getCompanyId();
        this.isRoster = dirContact.isRoster();
        this.lastAvatarUpdateDate = dirContact.getLastAvatarUpdateDate();
        this.role = dirContact.getRole();
        this.country = dirContact.getCountry();
        this.language = dirContact.getLanguage();
        this.timeZone = dirContact.getTimeZone();
        this.jidTel = dirContact.getJidTel();
        this.loginEmail = dirContact.getLoginEmail();

        notifyDirectoryContactChanged();
    }

    public PhoneNumber getPhoneNumber(PhoneNumber.PhoneNumberType phoneNumberType) {
        for(PhoneNumber phoneNumber : getPhoneNumbers()) {
            if( phoneNumber.getPhoneNumberType().equals(phoneNumberType))
                return phoneNumber;
        }
        return null;
    }

    public Date getLastPresenceReceivedDate() {
        return m_lastPresenceReceivedDate;
    }

    public void setLastPresenceReceivedDate(Date lastPresenceChangeDate) {
        m_lastPresenceReceivedDate = lastPresenceChangeDate;
        //GFR Do not notify notifyDirectoryContactChanged, because infinite Loop : ConversationFragment:refreshLastOfflineDate
        notifyDirectoryContactChanged();
    }

    @Override
    public Bitmap getPhoto()
    {
        return photo;
    }

    @Override
    public void setPhoto(Bitmap photoBitmap)
    {
        this.photo = photoBitmap;
        notifyDirectoryContactChanged();
    }

    public String getLastAvatarUpdateDate()
    {
        return lastAvatarUpdateDate;
    }

    public void setLastAvatarUpdateDate(String lastAvatarUpdateDate)
    {
        this.lastAvatarUpdateDate = lastAvatarUpdateDate;
        notifyDirectoryContactChanged();
    }

    public void setRoles(List<ContactRole> role) {
        this.role = role;
        notifyDirectoryContactChanged();
    }

    public List<ContactRole> getRole() {
        return role;
    }

    public void setCountry(String country) {
        this.country = country;
        notifyDirectoryContactChanged();
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
        notifyDirectoryContactChanged();
    }

    public String getCountry() {
        return country;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
        notifyDirectoryContactChanged();
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setJidTel(String jidTel) {
        this.jidTel = jidTel;
        notifyDirectoryContactChanged();
    }

    public String getJidTel() {
        return jidTel;
    }

    public void setLoginEmail(String loginEmail) {
        this.loginEmail = loginEmail;
        notifyDirectoryContactChanged();
    }

    public String getLoginEmail() {
        return loginEmail;
    }

    private List<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
        notifyDirectoryContactChanged();
    }

    public boolean isEmpty() {

        if( StringsUtil.isNullOrEmpty(companyId) &&
                StringsUtil.isNullOrEmpty(firstName) &&
                StringsUtil.isNullOrEmpty(lastName) &&
                StringsUtil.isNullOrEmpty(companyName) &&
                StringsUtil.isNullOrEmpty(corporateId) &&
                StringsUtil.isNullOrEmpty(loginEmail) &&
                profiles.size() == 0 &&
                role != null &&
                role.size() == 0 )
            return true;
        return false;
    }

    public boolean isInDefaultCompany() {
        return this.isDefaultCompany;
    }

    public void setIsDefaultCompany(boolean isDefaultCompany) {
        this.isDefaultCompany = isDefaultCompany;
        notifyDirectoryContactChanged();
    }

    public DirectoryContactType getType() {
        return type;
    }

    public void setType(DirectoryContactType type) {
        this.type = type;
        notifyDirectoryContactChanged();
    }

    public boolean isBot() {
        return this.type.equals(DirectoryContactType.BOT);
    }

    public String toString() {
        return "Name: " + getFirstName() + " " +
                "Last Name: " + getLastName() + " " +
                "Corporate Id: " + getCorporateId() + " " +
                "JID: " + getImJabberId() +" " +
                "";
    }

    public void dumpInLog(String dumpLogTag) {
        super.dumpInLog(dumpLogTag);
        Log.getLogger().info(dumpLogTag, "    title=" + title);
        Log.getLogger().info(dumpLogTag, "    isRoster=" + isRoster);
        Log.getLogger().info(dumpLogTag, "    presence=" + presence);
        Log.getLogger().info(dumpLogTag, "    type=" + type);

        if( m_lastPresenceReceivedDate != null ) {
            Log.getLogger().info(dumpLogTag, "    lastPresenceReceivedDate="+m_lastPresenceReceivedDate);
        }
        if( lastAvatarUpdateDate != null ) {
            Log.getLogger().info(dumpLogTag, "    lastAvatarUpdateDate="+lastAvatarUpdateDate);
        }
        if( role != null ) {
            StringBuilder texte = new StringBuilder();
            texte.append("role[");
            boolean firstTime = false;
            for(ContactRole value : role) {
                if (firstTime) {
                    texte.append(", ");
                    firstTime = true;
                }
                texte.append(value);
            }
            texte.append("]");
            Log.getLogger().info(dumpLogTag, "    roles=" + texte);
        }
        if( country != null ) {
            Log.getLogger().info(dumpLogTag, "    country="+country);
        }
        if( language != null ) {
            Log.getLogger().info(dumpLogTag, "    language="+language);
        }
        if( presenceSettings != null ) {
            Log.getLogger().info(dumpLogTag, "    presenceSettings="+presenceSettings);
        }
        if( timeZone != null ) {
            Log.getLogger().info(dumpLogTag, "    timeZone="+timeZone);
        }
        if( jidTel != null ) {
            Log.getLogger().info(dumpLogTag, "    jidTel="+jidTel);
        }
        if( loginEmail != null ) {
            Log.getLogger().info(dumpLogTag, "    loginEmail="+loginEmail);
        }
        if( presencesByResources != null && presencesByResources.size() > 0 ) {
            Log.getLogger().info(dumpLogTag, "    presencesByResources=" + presencesByResources.size());
            for(String presKey: presencesByResources.keySet()) {
                PresenceWithDate presDate = presencesByResources.get(presKey);
                Log.getLogger().info(dumpLogTag, "       presencesByResources["+presKey+"] =" + presDate.getDate()+"/"+presDate.getPresence());
            }
        }
    }

    public void setTitle(String title) {
        if (StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(title))
            this.title = "";
        else
            this.title = title;
        notifyDirectoryContactChanged();
    }


    public boolean hasNoAvatarOnServer() {
        return hasNoAvatarOnServer;
    }

    public void sethasNoAvatarOnServer(boolean hasNoAvatarOnServer) {
        this.hasNoAvatarOnServer = hasNoAvatarOnServer;
    }

    public String getTitle() {
        return title;
    }

    private synchronized void notifyPresenceChanged()
    {
        for (IContactListener listener : m_changeListeners.toArray(new IContactListener[m_changeListeners.size()]))
        {
            listener.onPresenceChanged(getPresence());
        }
    }

    private synchronized void notifyDirectoryContactChanged()
    {
        for (IContactListener listener : m_changeListeners.toArray(new IContactListener[m_changeListeners.size()]))
        {
            listener.contactUpdated(this);
        }
    }

    public CalendarPresence getCalendarPresence()
    {
        return m_calendarPresence;
    }

    public void setCalendarPresence(CalendarPresence calendarPresence)
    {
        m_calendarPresence = calendarPresence;
        notifyPresenceChanged();
    }

    public enum DirectoryContactType
    {
        USER,
        BOT
    }

}
