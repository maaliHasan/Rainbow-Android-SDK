package com.ale.infra.contact;

import com.ale.infra.list.ArrayItemList;
import com.ale.infra.proxy.directory.IDirectoryProxy;

import java.util.List;

/**
 * Created by georges on 02/03/16.
 */
public class ContactMerger {
    private String LOG_TAG = "ContactMerger";

    private ArrayItemList<IContact> m_contactsToResolve;
    private List<IContact> m_rosters;
    private IDirectoryProxy m_directory;

    public ContactMerger(ArrayItemList<IContact> contactsToResolve, List<IContact> rosters, IDirectoryProxy directory)
    {
        m_contactsToResolve = contactsToResolve;
        m_rosters = rosters;
        m_directory = directory;
    }

    /*
    public void searchMatchingBusinessContactInDirectoryCache() {
        Log.getLogger().verbose(LOG_TAG, ">searchMatchingBusinessContactInCache");

        for(final IContact contact : m_contactsToResolve.getCopyOfDataList())
        {
            if( contact.isNative()) {
                if( contact.isCorporate())
                {
                    Log.getLogger().verbose(LOG_TAG, " the contact "+contact.getDisplayName("")+" is already resolved");
                    continue;
                }
                IContact rosterContact = findContactFromEmails(contact.getEmailAddresses(), m_directory.getBusinessContactsCache().getCopyOfDataList());
                if( rosterContact != null)
                {
                    Log.getLogger().verbose(LOG_TAG, "contact with email "+contact.getEmailWork()+" found in Business Cache");
                    contact.fillEmptyFieldsWithContact(rosterContact);
                }
            }
        }
    }

    public void searchMatchingBusinessContactInServer() {
        Log.getLogger().verbose(LOG_TAG, ">searchMatchingBusinessContactInLocalContacts");

//        for(final IContact contact : m_contactsToResolve.getCopyOfDataList())
//        {
//            if( contact.isNative()) {
//                if( contact.isCorporate())
//                {
//                    Log.getLogger().verbose(LOG_TAG, " the contact "+contact.getDisplayName("")+" is already resolved");
//                    continue;
//                }
//                if (!StringUtils.isNullOrEmpty(contact.getEmailWork())) {
//                    Log.getLogger().verbose(LOG_TAG, ">search contact "+contact.getDisplayName("")+" by Work Email");
//                    searchContactByEmailOnServer(contact.getEmailWork(), contact);
//                } else if (!StringUtils.isNullOrEmpty(contact.getEmailHome())) {
//                    Log.getLogger().verbose(LOG_TAG, ">search contact "+contact.getDisplayName("")+" by Home Email");
//                    searchContactByEmailOnServer(contact.getEmailHome(), contact);
//                }
//            }
//        }
        List<String> emails = new ArrayList<>();
        for(IContact contact : m_contactsToResolve.getCopyOfDataList()) {
            if( contact.isNative()) {
                if (contact.isCorporate()) {
                    Log.getLogger().verbose(LOG_TAG, " the contact " + contact.getDisplayName("") + " is already resolved");
                    continue;
                }
                if (!StringUtils.isNullOrEmpty(contact.getEmailWork())) {
<<<<<<< Updated upstream
                    Log.getLogger().verbose(LOG_TAG, ">search contact "+contact.getDisplayName("")+" by Work EmailAddress");
                    searchContactByEmailOnServer(contact.getEmailWork(), contact);
                } else if (!StringUtils.isNullOrEmpty(contact.getEmailHome())) {
                    Log.getLogger().verbose(LOG_TAG, ">search contact "+contact.getDisplayName("")+" by Home EmailAddress");
                    searchContactByEmailOnServer(contact.getEmailHome(), contact);
=======
                    Log.getLogger().verbose(LOG_TAG, ">search contact " + contact.getDisplayName("") + " by Work Email");
                    emails.add(contact.getEmailWork());
                } else if (!StringUtils.isNullOrEmpty(contact.getEmailHome())) {
                    Log.getLogger().verbose(LOG_TAG, ">search contact " + contact.getDisplayName("") + " by Home Email");
                    emails.add(contact.getEmailHome());
>>>>>>> Stashed changes
                }
            }
        }
        searchContactByEmailsOnServer(emails);
    }

    private void searchContactByEmailsOnServer(final List<String> contactEmail) {
        Log.getLogger().verbose(LOG_TAG, ">searchContactByEmailsOnServer; " + contactEmail);
        m_directory.searchByMails(contactEmail, new IDirectoryProxy.IDirectoryListener() {
            @Override
            public void onCorporateSearchSuccess(ArrayItemList<IContact> searchResults) {
                Log.getLogger().verbose(LOG_TAG, ">onCorporateSearchSuccess");
                // TODO
            }

            @Override
            public void onAvatarFailure() {
                Log.getLogger().warn(LOG_TAG, ">onAvatarFailure");
            }
        });
    }

    private void searchContactByEmailOnServer(final String contactEmail, final IContact contact) {
        Log.getLogger().verbose(LOG_TAG, ">searchContactByEmailOnServer; " + contactEmail);
        m_directory.searchByMail(contactEmail, new IDirectoryProxy.IDirectoryListener() {
            @Override
            public void onCorporateSearchSuccess(List<IContact> searchResults) {
                Log.getLogger().verbose(LOG_TAG, ">onCorporateSearchSuccess");
                if (searchResults.size() > 0) {
                    Log.getLogger().verbose(LOG_TAG, "We found a matching Contact");
                    contact.fillEmptyFieldsWithContact(searchResults.get(0));
                    // Now the updated contact contains the IM Address => reparse it to check with Roster contacts
                    //parseLocalContacts();
                    //m_rosters.fireDataChanged();
                } else {
                    Log.getLogger().verbose(LOG_TAG, "No matching Contact for " + contact.getDisplayName(""));
                    // TODO save that this contact is already requested
                    contact.setType(ContactType.NATIVE);
                }
            }

            @Override
            public void onAvatarFailure() {
                Log.getLogger().warn(LOG_TAG, ">onAvatarFailure");
            }
        });
    }

    public void mergeLocalAndRosterContacts() {
        parseLocalContacts();
        parseRosterThatDontBelongToParsedLocalContacts();
//        return m_contactsToResolve;
    }

    private void parseLocalContacts()
    {
        Log.getLogger().verbose(LOG_TAG, ">parseLocalContacts");

        for (IContact contact : m_contactsToResolve.getCopyOfDataList())
        {
            IContact rosterContact = findContactFromEmails(contact.getEmailAddresses(), m_rosters);
            if( rosterContact != null)
            {
                Log.getLogger().verbose(LOG_TAG, "contact with email "+contact.getEmailWork()+" found in roster");
                contact.fillEmptyFieldsWithContact(rosterContact);
            }
            else {
                rosterContact = findContactInListFromPhone(contact.getPhoneNumbers(), m_rosters);
                if (rosterContact != null) {
                    Log.getLogger().verbose(LOG_TAG, "contact with Phone Number " + contact.getFirstAvailableNumber() + " found in roster");
                    contact.fillEmptyFieldsWithContact(rosterContact);
                }
                else
                {
                    if( StringUtils.isNullOrEmpty(contact.getEmailWork()))
                        Log.getLogger().verbose(LOG_TAG, "NO contact with EmailAddress " + contact.getEmailWork() + " found in roster");
                    else
                        Log.getLogger().verbose(LOG_TAG, "NO contact with Phone " + contact.getFirstAvailableNumber() + " found in roster");
                }
            }
        }
    }

    private void parseRosterThatDontBelongToParsedLocalContacts() {
        Log.getLogger().verbose(LOG_TAG, ">parseRosterThatDontBelongToParsedLocalContacts");

        for(IContact roster : m_rosters)
        {
            if( (roster.getEmailAddresses() == null) || (roster.getEmailAddresses().size() == 0) )
            {
                Log.getLogger().verbose(LOG_TAG, "Roster contact ; " + roster.getDisplayName("")+ " has no EmailAddress - add it to contacts");
                // Roster does not belong to merged list
                m_contactsToResolve.add(roster);
                continue;
            }
            if( null == findContactFromEmails(roster.getEmailAddresses(), m_contactsToResolve.getCopyOfDataList()) )
            {
                Log.getLogger().verbose(LOG_TAG, "Adding missing Roster contact ; " + roster.getDisplayName(""));
                // Roster does not belong to merged list
                m_contactsToResolve.add(roster);
            }
            else
            {
                Log.getLogger().verbose(LOG_TAG, "Roster contact ; " + roster.getDisplayName("")+ " is already available in Local Contacts");
            }
        }
    }

    private IContact findContactInListFromPhone(Set<PhoneNumber> phones, List<IContact> contactList) {
        if(phones== null || phones.size() == 0)
        {
            return null;
        }

        for (IContact contact : contactList)
        {
            for(PhoneNumber initialPhoneNb : phones) {
                for (PhoneNumber phoneNb : contact.getPhoneNumbers()) {
                    if (phoneNb.getPhoneNumberValue().equals(initialPhoneNb.getPhoneNumberValue()))
                        return contact;
                }
            }
        }
        return null;
    }

    private IContact findContactFromEmails(Set<EmailAddress> emails, List<IContact> contactList) {
        if(emails == null || emails.size() == 0)
        {
            return null;
        }

        for (IContact contact : contactList)
        {
            if(checkEmailsListsMatch(emails,contact.getEmailAddresses()))
                return contact;
        }
        return null;
    }

    private boolean checkEmailsListsMatch(Set<EmailAddress> emailList1,Set<EmailAddress> emailList2)
    {
        for(EmailAddress email1:emailList1)
        {
            // TODO check if contains  still work
            if (emailList2.contains(email1))
            {
                return true;
            }
        }
        return false;
    }

//    public static void filterLocalContactsWithEmail(List<IContact> localContacts) {
//        List<IContact> localContactsList = new ArrayList<>();
//        for(IContact contact : localContacts)
//        {
//            if( contact.getEmails().size() > 0 )
//            {
//                localContactsList.add(contact);
//            }
//        }
//        m_contactsToResolve.clear();
//        m_contactsToResolve.addAll(localContactsList);
//    }

    public static List<IContact> makeLocalContactsCopyWithEmailOnly(List<IContact> localContacts)
    {
        List<IContact> localContactsCopy = new ArrayList<>();

        for(IContact contact : localContacts)
        {
            localContactsCopy.add(new Contact(contact));
        }
        return localContactsCopy;
    }
    */
}
