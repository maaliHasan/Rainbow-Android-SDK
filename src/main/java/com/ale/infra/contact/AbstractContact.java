package com.ale.infra.contact;

import android.graphics.Bitmap;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.user.IUserPreferences;
import com.ale.infra.xmpp.XmppUtils;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Created by grobert on 18/05/16.
 */
public abstract class AbstractContact implements IContact
{
    private static final String LOG_TAG = "AbstractContact";

    protected final Set<IContactListener> m_changeListeners = new HashSet<>();
    protected String firstName;
    protected String lastName;
    protected String companyName;
    protected String jobTitle;
    protected String nickName;
    protected Bitmap photo;
    protected Set<PhoneNumber> phoneNumbers;
    protected Set<EmailAddress> emailAddresses;
    protected Set<PostalAddress> postalAddresses;
    protected String jabberId;
    protected String corporateId;
    protected String companyId;

    public AbstractContact()
    {
        phoneNumbers = new HashSet<>();
        emailAddresses = new HashSet<>();
        postalAddresses = new HashSet<>();
    }

    public AbstractContact(IContact contact)
    {
        this.firstName = contact.getFirstName();
        this.lastName = contact.getLastName();
        this.companyName = contact.getCompanyName();
        this.jobTitle = contact.getJobTitle();
        this.nickName = contact.getNickName();
        this.photo = contact.getPhoto();
        this.phoneNumbers = contact.getPhoneNumbers();
        this.emailAddresses = contact.getEmailAddresses();
        this.postalAddresses = contact.getPostalAddresses();
        this.jabberId = contact.getImJabberId();
        this.corporateId = contact.getCorporateId();
        this.companyId = contact.getCompanyId();
    }

    @Override
    public void fillEmptyFieldsWithContact(IContact contact)
    {
        if (contact.getFirstName() != null)
            this.firstName = contact.getFirstName();
        if (contact.getLastName() != null)
            this.lastName = contact.getLastName();
        if (contact.getCompanyName() != null)
            this.companyName = contact.getCompanyName();

        setJobTitle(contact.getJobTitle());
        setNickName(contact.getNickName());

        if (contact.getPhoto() != null)
            this.photo = contact.getPhoto();
        if (contact.getPhoneNumbers() != null) //case no phoneNumber
            this.phoneNumbers = contact.getPhoneNumbers();
        if (contact.getEmailAddresses() != null && contact.getEmailAddresses().size() > 0)
            this.emailAddresses = contact.getEmailAddresses();
        if (contact.getPostalAddresses() != null && contact.getPostalAddresses().size() > 0)
            this.postalAddresses = contact.getPostalAddresses();
        if (contact.getImJabberId() != null)
            this.jabberId = contact.getImJabberId();
        if (contact.getCorporateId() != null)
            this.corporateId = contact.getCorporateId();
        if (contact.getCompanyId() != null)
            this.companyId = contact.getCompanyId();
    }

    @Override
    public boolean isCorporate()
    {
        return false;
    }

    @Override
    public boolean isNative()
    {
        return false;
    }


    public String getFirstName()
    {
        return firstName;
    }

    @Override
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    @Override
    public String getLastName()
    {
        return lastName;
    }

    @Override
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    @Override
    public String getNickName()
    {
        return nickName;
    }

    @Override
    public void setNickName(String nickname)
    {
        if (StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(nickname))
            this.nickName = "";
        else
            this.nickName = nickname;
    }

    @Override
    public String getDisplayName(String unknownNameString)
    {
        if (StringsUtil.isNullOrEmpty(getFirstName()) && StringsUtil.isNullOrEmpty(getLastName()))
        {
            Set<EmailAddress> emails = getEmailAddresses();
            if (emails.size() > 0)
            {
                EmailAddress firstAddress = emails.iterator().next();
                return firstAddress.getValue();
            }
            else
                return unknownNameString;
        }

        final String normalizedLastName = StringsUtil.normalize(getLastName());
        final String normalizedFirstName = StringsUtil.normalize(getFirstName());

        IUserPreferences userPreferences = RainbowContext.getPlatformServices().getUserPreferences();
        String displayNameFormat = userPreferences.getDisplayNameFormat();
        String displayFullName = null;
        if (displayNameFormat != null)
        {
            // Log.getLogger().verbose(LOG_TAG, "DisplayName format ; " + displayNameFormat);
            if (displayNameFormat.equals("0"))
            {
                // Log.getLogger().verbose(LOG_TAG, "It is FirstName LastName format");
                displayFullName = StringsUtil.normalize(normalizedFirstName + " " + normalizedLastName);
            }
            else if (displayNameFormat.equals("1"))
            {
                // Log.getLogger().verbose(LOG_TAG, "It is LastName FirstName format");
                displayFullName = StringsUtil.normalize(normalizedLastName + " " + normalizedFirstName);
            }
        }

        if (StringsUtil.isNullOrEmptyOrSpaces(displayFullName))
        {
            return unknownNameString;
        }
        else
        {
            return displayFullName;
        }

    }

    @Override
    public String getDisplayName4Log(String unknownNameString) {
        String displayName = getInitials();
        if (StringsUtil.isNullOrEmpty(displayName))
            return unknownNameString;
        else
            return displayName;

    }

    @Override
    public String getInitials()
    {
        StringBuilder initials = new StringBuilder();

        if (StringsUtil.isNullOrEmpty(getFirstName()) && StringsUtil.isNullOrEmpty(getLastName()))
        {
            String emailAddress = getFirstEmailAddress();
            return StringsUtil.isNullOrEmpty(emailAddress)? "" : emailAddress.substring(0, 1).toUpperCase();
        }


        IUserPreferences userPreferences = RainbowContext.getPlatformServices().getUserPreferences();
        String displayNameFormat = userPreferences.getDisplayNameFormat();
        if (displayNameFormat != null)
        {
            // Log.getLogger().verbose(LOG_TAG, "DisplayName format ; " + displayNameFormat);
            if (displayNameFormat.equals("0"))
            {
                // Log.getLogger().verbose(LOG_TAG, "It is FirstName LastName format");
                if (!StringsUtil.isNullOrEmpty(getFirstName()))
                {
                    initials.append(getFirstName().toUpperCase().charAt(0));
                }
                if (!StringsUtil.isNullOrEmpty(getLastName()))
                {
                    initials.append(getLastName().toUpperCase().charAt(0));
                }
            }
            else if (displayNameFormat.equals("1"))
            {
                // Log.getLogger().verbose(LOG_TAG, "It is LastName FirstName format");
                if (!StringsUtil.isNullOrEmpty(getLastName()))
                {
                    initials.append(getLastName().toUpperCase().charAt(0));
                }
                if (!StringsUtil.isNullOrEmpty(getFirstName()))
                {
                    initials.append(getFirstName().toUpperCase().charAt(0));
                }
            }
        }

        return initials.toString();
    }

    @Override
    public void addPhoneNumber(String phoneNumber, String phoneNbE164, PhoneNumber.PhoneNumberType type)
    {
        if (StringsUtil.isNullOrEmpty(phoneNumber))
            return;

        PhoneNumber pNumber = new PhoneNumber(formatNumberIfNeeded(phoneNumber), type);
        if( !StringsUtil.isNullOrEmpty(phoneNbE164) )
            pNumber.setPhoneNumberE164(phoneNbE164);

        synchronized (phoneNumbers)
        {
            phoneNumbers.add(pNumber);
        }
    }

    @Override
    public void addPhoneNumber(PhoneNumber phoneNumber)
    {
        if (phoneNumber == null)
            return;

        synchronized (phoneNumbers)
        {
            phoneNumbers.add(phoneNumber);
        }
    }

    @Override
    public synchronized String getFirstAvailableNumber()
    {
        Set<PhoneNumber> phoneNumbers = getPhoneNumbers();

        if (!phoneNumbers.isEmpty())
        {
            String number = getFirstOfficePhoneNumber().getPhoneNumberValue();
            if (!StringsUtil.isNullOrEmpty(number))
            {
                return number;
            }

            number = getFirstMobilePhoneNumber();
            if (!StringsUtil.isNullOrEmpty(number))
            {
                return number;
            }

            return phoneNumbers.iterator().next().getPhoneNumberValue();
        }
        return "";
    }

    @Override
    public PhoneNumber getFirstOfficePhoneNumber()
    {
        synchronized (phoneNumbers)
        {
            for (PhoneNumber number : phoneNumbers)
            {
                switch (number.getPhoneNumberType()) {
                    case COMPANY_MAIN:
                    case OFFICE:
                    case MAIN:
                    case WORK:
                        if (!StringsUtil.isNullOrEmpty(number.getPhoneNumberValue()))
                            return number;
                    default:
                        break;
                }

            }

            return null;
        }
    }

    @Override
    public String getFirstPersonalMobilePhoneNumber()
    {
        synchronized (phoneNumbers)
        {
            for (PhoneNumber number : phoneNumbers)
            {
                switch (number.getPhoneNumberType()) {
                    case MOBILE:
                        if (!StringsUtil.isNullOrEmpty(number.getPhoneNumberValue()))
                            return number.getPhoneNumberValue();
                    default:
                        break;
                }

            }

            return null;
        }
    }

    @Override
    public String getFirstPersonalPhoneNumber()
    {
        synchronized (phoneNumbers)
        {
            for (PhoneNumber number : phoneNumbers)
            {
                switch (number.getPhoneNumberType()) {
                    case HOME:
                        if (!StringsUtil.isNullOrEmpty(number.getPhoneNumberValue()))
                            return number.getPhoneNumberValue();
                    default:
                        break;
                }

            }

            return null;
        }
    }


    @Override
    public String getFirstMobilePhoneNumber()
    {
        synchronized (phoneNumbers)
        {
            for (PhoneNumber number : phoneNumbers)
            {
                switch (number.getPhoneNumberType()) {
                    case WORK_MOBILE:
                        if (!StringsUtil.isNullOrEmpty(number.getPhoneNumberValue()))
                            return number.getPhoneNumberValue();
                    default:
                        break;
                }

            }

            return null;
        }
    }

    @Override
    public Set<PhoneNumber> getPhoneNumbers()
    {
        Set<PhoneNumber> copyOfNumbers = new HashSet<>();

        synchronized (phoneNumbers)
        {
            copyOfNumbers.addAll(phoneNumbers);
        }

        return copyOfNumbers;
    }

    @Override
    public String getMainEmailAddress()
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

    @Override
    public String getFirstEmailAddress()
    {
        for (EmailAddress email : getEmailAddresses())
        {
            return email.getValue();
        }

        return "";
    }

    @Override
    public Set<EmailAddress> getEmailAddresses()
    {
        Set<EmailAddress> copyOfEmails = new HashSet<>();

        synchronized (emailAddresses)
        {
            copyOfEmails.addAll(emailAddresses);
        }

        return copyOfEmails;
    }

    @Override
    public void clearEmailAddresses()
    {
        synchronized (emailAddresses)
        {
            emailAddresses.clear();
        }
    }

    @Override
    public String getEmailWithType(EmailAddress.EmailType type)
    {
        synchronized (emailAddresses)
        {
            for (EmailAddress email : this.emailAddresses.toArray(new EmailAddress[this.emailAddresses.size()])) {
                if( email.getType().equals(type)) {
                    return email.getValue();
                }
            }
        }

        return null;
    }

    @Override
    public void addEmailAddress(String email, EmailAddress.EmailType type)
    {
        if (StringsUtil.isNullOrEmpty(email))
            return;

        EmailAddress emailAddress = new EmailAddress(type, email);

        synchronized (emailAddresses)
        {
            emailAddresses.add(emailAddress);
        }
    }

    @Override
    public String getImJabberId()
    {
        return jabberId;
    }

    @Override
    public void setImJabberId(String jabberId)
    {
        if( jabberId != null) {
            this.jabberId = XmppUtils.parseBareJid(jabberId);
        }
    }

    @Override
    public String getCorporateId()
    {
        return corporateId;
    }

    @Override
    public void setCorporateId(String corporateId)
    {
        this.corporateId = corporateId;
    }

    @Override
    public String getCompanyName()
    {
        return this.companyName;
    }

    @Override
    public void setCompanyName(String companyName)
    {
        this.companyName = companyName;
    }

    @Override
    public String getCompanyId()
    {
        return companyId;
    }

    @Override
    public void setCompanyId(String companyId)
    {
        this.companyId = companyId;
    }

    @Override
    public String getJobTitle()
    {
        return this.jobTitle;
    }

    @Override
    public void setJobTitle(String jobTitle)
    {
        if (StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(jobTitle))
            this.jobTitle = "";
        else
            this.jobTitle = jobTitle;
    }

    @Override
    public String getWorkAddress()
    {
        return null;
    }

    @Override
    public Set<PostalAddress> getPostalAddresses()
    {
        Set<PostalAddress> postalAddresses = new HashSet<>();
        synchronized (this.postalAddresses)
        {
            postalAddresses.addAll(this.postalAddresses);
        }

        return postalAddresses;
    }

    @Override
    public void addPostalAddress(String address, PostalAddress.AddressType type)
    {
        if (StringsUtil.isNullOrEmpty(address))
            return;

        PostalAddress postalAddress = new PostalAddress(type, address);

        synchronized (postalAddresses)
        {
            postalAddresses.add(postalAddress);
        }
    }

    @Override
    public synchronized Bitmap getPhoto()
    {
        return this.photo;
    }

    @Override
    public synchronized void setPhoto(Bitmap photoBitmap)
    {
        this.photo = photoBitmap;

        if (this.photo != null)
        {
            // call the callback
            notifyContactUpdated();
        }
    }

    public synchronized void notifyContactUpdated()
    {
        for (IContactListener listener : m_changeListeners.toArray(new IContactListener[m_changeListeners.size()]))
        {
            listener.contactUpdated(this);
        }
    }

    @Override
    public synchronized void registerChangeListener(IContactListener changeListener)
    {
        m_changeListeners.add(changeListener);
    }

    @Override
    public synchronized void unregisterChangeListener(IContactListener changeListener)
    {
        m_changeListeners.remove(changeListener);
    }

    private String formatNumberIfNeeded(String number)
    {
        if (StringsUtil.isNullOrEmpty(number))
            return number;

        if (number.startsWith("\"") && number.endsWith(">"))
        {
            String formatedNumber = number.substring(1);
            formatedNumber = formatedNumber.replace("\"<", " (");
            formatedNumber = formatedNumber.replace(">", ")");
            return formatedNumber;
        }

        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractContact)) return false;

        AbstractContact that = (AbstractContact) o;

        if ( firstName!= null && !firstName.equals(that.firstName)) return false;
        if ( lastName!= null && !lastName.equals(that.lastName)) return false;
        if ( companyName!= null && !companyName.equals(that.companyName)) return false;
        if ( jobTitle!= null && !jobTitle.equals(that.jobTitle)) return false;
        if ( nickName!= null && !nickName.equals(that.nickName)) return false;
        if ( photo!= null && !photo.equals(that.photo)) return false;
        if ( phoneNumbers!= null && !phoneNumbers.equals(that.phoneNumbers)) return false;
        if ( emailAddresses!= null && !emailAddresses.equals(that.emailAddresses)) return false;
        if ( postalAddresses!= null && !postalAddresses.equals(that.postalAddresses)) return false;
        if ( jabberId!= null && !jabberId.equals(that.jabberId)) return false;
        if ( corporateId!= null && !corporateId.equals(that.corporateId)) return false;
        return ( companyId!= null && companyId.equals(that.companyId) );

    }

    @Override
    public int hashCode() {
        int result = 0;
        if( firstName != null) result = 31 * result + firstName.hashCode();
        if( lastName != null) result = 31 * result + lastName.hashCode();
        if( companyName != null) result = 31 * result + companyName.hashCode();
        if( jobTitle != null) result = 31 * result + jobTitle.hashCode();
        if( nickName != null) result = 31 * result + nickName.hashCode();
        if( photo != null) result = 31 * result + photo.hashCode();
        if( phoneNumbers != null) result = 31 * result + phoneNumbers.hashCode();
        if( emailAddresses != null) result = 31 * result + emailAddresses.hashCode();
        if( postalAddresses != null) result = 31 * result + postalAddresses.hashCode();
        if( jabberId != null) result = 31 * result + jabberId.hashCode();
        if( corporateId != null) result = 31 * result + corporateId.hashCode();
        if( companyId != null) result = 31 * result + companyId.hashCode();
        return result;
    }

    public void dumpInLog(String dumpLogTag) {
        if( firstName != null ) {
            Log.getLogger().info(dumpLogTag, "    FirstName="+firstName);
        }
        if( lastName != null ) {
            Log.getLogger().info(dumpLogTag, "    LastName="+lastName);
        }
        if( companyName != null ) {
            Log.getLogger().info(dumpLogTag, "    companyName="+companyName);
        }
        if( jobTitle != null ) {
            Log.getLogger().info(dumpLogTag, "    jobTitle="+jobTitle);
        }
        if( nickName != null ) {
            Log.getLogger().info(dumpLogTag, "    nickName="+nickName);
        }
        if( photo != null ) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object
            byte[] bitmapBytes = baos.toByteArray();

            Checksum checksum = new CRC32();
            checksum.update(bitmapBytes, 0, bitmapBytes.length);
            long checksumValue = checksum.getValue();
            Log.getLogger().info(dumpLogTag, "    photo Checksum: " + checksumValue);
        }
        if( jabberId != null ) {
            Log.getLogger().info(dumpLogTag, "    jabberId="+jabberId);
        }
        if( corporateId != null ) {
            Log.getLogger().info(dumpLogTag, "    corporateId="+corporateId);
        }
        if( companyId != null ) {
            Log.getLogger().info(dumpLogTag, "    companyId="+companyId);
        }
        if( phoneNumbers != null ) {
            Log.getLogger().info(dumpLogTag, "    phoneNumbers=" + phoneNumbers.size());
            for(PhoneNumber phoneNb: phoneNumbers) {
                Log.getLogger().info(dumpLogTag, "       phoneNumber=" + phoneNb.getPhoneNumberType()+"/"+phoneNb.getPhoneNumberValue());
            }
        }
        if( emailAddresses != null ) {
            Log.getLogger().info(dumpLogTag, "    emailAddresses=" + emailAddresses.size());
            for(EmailAddress email: emailAddresses) {
                Log.getLogger().info(dumpLogTag, "       email=" + email.getType()+"/"+email.getValue());
            }
        }
        if( postalAddresses != null && postalAddresses.size() > 0 ) {
            Log.getLogger().info(dumpLogTag, "    postalAddresses=" + postalAddresses.size());
            for(PostalAddress postal: postalAddresses) {
                Log.getLogger().info(dumpLogTag, "       postalAddress=" + postal.getType()+"/"+postal.getValue());
            }
        }
    }
}

