/******************************************************************************
 * Copyright � 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : LocalContactResolver.java
 * Summary :
 * *****************************************************************************
 * History
 * <p/>
 * Date       Who       CR            Comment
 * 2010/09/23 LDO       crms00261725  voiceMail crashes when no email found
 * 2010/10/07 CBR       crms00265380  [error] "add to contact " feature crashes the contact application
 * 2010/10/28 Francisco crms00270367  SearchByName ; add an other filter to avoid to have a result from email field
 * 2010/10/29 Francisco crms00270726  startSearchInitialInContacts ; correction when using same letter twice
 * 2010/10/29 m.geyer                 Handle Klocwork warnings
 * 2010/12/02 cebruckn  crms00277878  crash of the application after searching a name with one character
 * 2011/08/23 Francisco crms00332706  merge all SQL queries in one result IContact in searchByInitials
 * 2012/01/03 cebruckn  crms00353993  Crash at start when favorites are initialized
 * 2012/03/22 cebruckn  crms00367959  [OXO] My IC Android  -  Not dispalying Company Details and Home phone number
 * 2012/04/17 cebruckn  crms00371718  [OXO] MyIC Android - Wrong handling of favorite numbers
 * 2012/07/11 cebruckn  crms00385286  Bad photo displayed on a favorite
 * 2012/11/06 cebruckn crms00405500 MyIC Android 4.1 - Crash of the MyIC Android client when dialing from numpad of MyIC
 * 2013/01/09 cebruckn crms00414373 Call Grouping not 100% accurate
 */
package com.ale.infra.searcher.localContact;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;

import com.ale.infra.contact.EmailAddress;
import com.ale.infra.contact.IBitmapConverter;
import com.ale.infra.contact.IContact;
import com.ale.infra.contact.LocalContact;
import com.ale.infra.contact.PhoneNumber;
import com.ale.infra.contact.PostalAddress;
import com.ale.infra.contact.WebSite;
import com.ale.util.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LocalContactSearcher implements ILocalContactSearcher
{
    private static final String LOG_TAG = "LocalContactSearcher";

    private final String MIME_TYPE = ContactsContract.Data.MIMETYPE;

    // ContactsContract.Contacts:
    private final String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
    private final String LOOKUP_KEY = ContactsContract.Contacts.LOOKUP_KEY;
    private final String PHOTO_THUMBNAIL_URI = ContactsContract.Contacts.PHOTO_THUMBNAIL_URI;
    private final String PHOTO_URI = ContactsContract.Contacts.PHOTO_URI;
    private final String PHOTO_ID = ContactsContract.Contacts.PHOTO_ID;
    // EmailAddress:
    private final String EMAIL_MIME = ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE;
    private final String EMAIL_TYPE = ContactsContract.CommonDataKinds.Email.TYPE;
    private final String EMAIL_ADDRESS = ContactsContract.CommonDataKinds.Email.ADDRESS;
    private final int EMAIL_TYPE_WORK = ContactsContract.CommonDataKinds.Email.TYPE_WORK;
    private final int EMAIL_TYPE_MOBILE = ContactsContract.CommonDataKinds.Email.TYPE_MOBILE;
    private final int EMAIL_TYPE_OTHER = ContactsContract.CommonDataKinds.Email.TYPE_OTHER;
    private final int EMAIL_TYPE_CUSTOM = ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM;
    private final int EMAIL_TYPE_HOME = ContactsContract.CommonDataKinds.Email.TYPE_HOME;
    // names:
    private final String NAME_MIME = ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE;
    private final String FAMILY_NAME = ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME;
    private final String GIVEN_NAME = ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME;
    private final String MIDDLE_NAME = ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME;
    // nickname:
    private final String NICKNAME_MIME = ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE;
    private final String NICKNAME = ContactsContract.CommonDataKinds.Nickname.NAME;
    // organization:
    private final String ORGANIZATION_MIME = ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE;
    private final String COMPANY = ContactsContract.CommonDataKinds.Organization.COMPANY;
    private final String JOB_TITLE = ContactsContract.CommonDataKinds.Organization.TITLE;
    // IM:
    private final String IM_MIME = ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE;
    private final String IM_ADDRESS = ContactsContract.CommonDataKinds.Im.DATA;
    private final String IM_TYPE = ContactsContract.CommonDataKinds.Im.TYPE;
    private final int IM_TYPE_WORK = ContactsContract.CommonDataKinds.Im.TYPE_WORK;
    // phones:
    private final String PHONE_MIME = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
    private final String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    private final String PHONE_TYPE = ContactsContract.CommonDataKinds.Phone.TYPE;
    private final int PHONE_TYPE_WORK = ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
    private final int PHONE_TYPE_HOME = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
    private final int PHONE_TYPE_FAX_WORK = ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK;
    private final int PHONE_TYPE_FAX_HOME = ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME;
    private final int PHONE_TYPE_MOBILE = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
    private final int PHONE_TYPE_WORK_MOBILE = ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE;
    private final int PHONE_TYPE_MAIN = ContactsContract.CommonDataKinds.Phone.TYPE_MAIN;
    private final int PHONE_TYPE_COMPANY_MAIN = ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN;
    private final int PHONE_TYPE_CUSTOM = ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM;
    private final int PHONE_TYPE_OTHER = ContactsContract.CommonDataKinds.Phone.TYPE_OTHER;
    // address:
    private final String ADDRESS_MIME = ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE;
    private final String ADDRESS_TYPE = ContactsContract.CommonDataKinds.StructuredPostal.TYPE;
    private final int ADDRESS_TYPE_WORK = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK;
    private final int ADDRESS_TYPE_HOME = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME;
    private final int ADDRESS_TYPE_CUSTOM = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM;
    private final int ADDRESS_TYPE_OTHER = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER;
    private final String ADDRESS = ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS;
    // web sites:
    private final String WEB_MIME = ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE;
    private final String WEB_SITE = ContactsContract.CommonDataKinds.Website.DATA;
    private final String WEB_TYPE = ContactsContract.CommonDataKinds.Website.TYPE;
    private final int WEB_TYPE_WORK = ContactsContract.CommonDataKinds.Website.TYPE_WORK;
    private final int WEB_TYPE_HOME = ContactsContract.CommonDataKinds.Website.TYPE_HOME;
    private final int WEB_TYPE_HOMEPAGE = ContactsContract.CommonDataKinds.Website.TYPE_HOMEPAGE;
    private final int WEB_TYPE_BLOG = ContactsContract.CommonDataKinds.Website.TYPE_BLOG;
    private final int WEB_TYPE_CUSTOM = ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM;
    private final int WEB_TYPE_OTHER = ContactsContract.CommonDataKinds.Website.TYPE_OTHER;
    // photos:
    private final String PHOTO_MIME = ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE;
    private final String PHOTO = ContactsContract.CommonDataKinds.Photo.PHOTO;

    private Context context;
    private IBitmapConverter m_bitmapConverter;
    private Map<String, String> m_visibleGroups = new HashMap<>();

    /**
     * Construct a new ContactResolver with the specified context.
     */
    public LocalContactSearcher(Context context, IBitmapConverter bitmapConverter)
    {
        this.context = context;
        m_bitmapConverter = bitmapConverter;
    }


    @Override
    public List<IContact> searchByName(String query)
    {
        Log.getLogger().verbose(LOG_TAG, "searchContactsByName(" + query + ") : START.");
        if (isContactPermissionNotAllowed())
            return new ArrayList<>();

        // split the query using space separator:
        // We can search a contact using, both, first letters of last & first name:
        // example : for Arielle GRASSER, we can have :
        // 'ari g ' , 'a ' , 'a grass ' ...
        String[] queries = query.split("[ ]+");

        // SORT ORDER: by family name
        String sortOrder = FAMILY_NAME + " ASC";
        // SELECTION CLAUSE: query on family name OR given name OR middle name:
        String selectionClause = "";
        // SELECTION ARGS: query%
        String[] selectionArgs = null;

        if (queries.length == 1)
        {
            // search the string in all 3 name fields
            if (queries[0].matches("[0-9]"))
            { // search with one digit on given or middle name does not work as requested ...
                selectionClause = "(( " + FAMILY_NAME + " LIKE ? ) AND ( " + MIME_TYPE + " = ? ))";
                selectionArgs = new String[]{queries[0] + "%", String.valueOf(NAME_MIME)};
            }
            else
            {
                selectionClause = "((( " + FAMILY_NAME + " LIKE ? ) OR ( " + MIDDLE_NAME + " LIKE ? ) OR ( " + GIVEN_NAME + " LIKE ? )) AND ( " + MIME_TYPE + " = ? ))";
                selectionArgs = new String[]{queries[0] + "%", queries[0] + "%", queries[0] + "%", String.valueOf(NAME_MIME)};
            }
        }
        else if (queries.length == 2)
        {
            // search by first letters of first and last name (in any order)
            // In this case we don't search in the middle name
            selectionClause = "(((( " + FAMILY_NAME + " LIKE ? ) AND ( " + GIVEN_NAME + " LIKE ? ))  OR (( " + FAMILY_NAME + " LIKE ? )  AND ( " + GIVEN_NAME + " LIKE ? )))  AND ( " + MIME_TYPE + " = ? ))";
            selectionArgs = new String[]{queries[0] + "%", queries[1] + "%", queries[1] + "%", queries[0] + "%", String.valueOf(NAME_MIME)};
        }

        List<IContact> contacts = searchContacts(ContactsContract.Data.CONTENT_URI, selectionClause, selectionArgs, sortOrder, false);

        Log.getLogger().verbose(LOG_TAG, "searchContactsByName(" + query + ") : END.");


        return contacts;
    }

    @Override
    public List<IContact> searchAllWithEmail()
    {
        Log.getLogger().verbose(LOG_TAG, "searchAllWithEmail() : START.");
        if (isContactPermissionNotAllowed())
            return new ArrayList<>();

        String selectionClause = "(( " + EMAIL_ADDRESS + " NOT LIKE '' ) AND ( " + MIME_TYPE + " = ? ))";
        String[] selectionArgs = new String[]{String.valueOf(EMAIL_MIME)};

        List<IContact> contacts = searchContacts(ContactsContract.Data.CONTENT_URI, selectionClause, selectionArgs, null, false);

        Log.getLogger().verbose(LOG_TAG, "searchAllWithEmail() : END.");
        return contacts;
    }

    @Override
    public List<IContact> searchAndroidFilteredAndWithEmail() {
        Log.getLogger().verbose(LOG_TAG, "searchAndroidFilteredAndWithEmail() : START.");
        if (isContactPermissionNotAllowed())
            return new ArrayList<>();

        String selectionClause = "(( " + EMAIL_ADDRESS + " NOT LIKE '' ) AND ( " + MIME_TYPE + " = ? ))";
        String[] selectionArgs = new String[]{String.valueOf(EMAIL_MIME)};

        List<IContact> contacts = searchContacts(ContactsContract.Data.CONTENT_URI, selectionClause, selectionArgs, null, true);

        Log.getLogger().verbose(LOG_TAG, "searchAndroidFilteredAndWithEmail() : END.");

        if (contacts.size() > 0)
            return contacts;
        else
            return getAllVisibleContacts();
    }

    private boolean isContactPermissionNotAllowed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ( context.checkSelfPermission(Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.getLogger().warn(LOG_TAG, "searchContactsByName ACCESS FORBIDDEN");
                return true;
            }
        }
        return false;
    }


    private List<IContact> searchContacts(Uri uri, String selectionClause, String[] selectionArgs, String sortOrder, boolean filterVisibleContact) {
        List<IContact> contacts = new ArrayList<>();
        String error = "";

        // PROJECTION: get the LOOKUP_KEY (unique identifier of the contact even if multiple accounts)
        String[] projection = new String[]{ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_URI, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI};

        if (context == null)
            return contacts;


        Cursor contactCursor = context.getContentResolver().query(uri, projection, selectionClause, selectionArgs, sortOrder);
        try {
            if (contactCursor != null) {
                while (contactCursor.moveToNext()) {
                    String contactDisplayName = getString(contactCursor, DISPLAY_NAME);
                    if (filterVisibleContact) {
                        int lookupKeyIndex = contactCursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY);
                        if (lookupKeyIndex >= 0) {
                            String lookupKey = contactCursor.getString(lookupKeyIndex);
                            if (!checkContactInVisibleGroup(lookupKey)) {
                                Log.getLogger().verbose(LOG_TAG, "Contact is not in Visible Group");
                                continue;
                            }
                        }
                    }

                    LocalContact c = getLocalContactfromAndroid(contactCursor);
                    if (c != null)
                        contacts.add(c);
                }
            }
        } catch (Exception e) {
            Log.getLogger().warn(LOG_TAG, "searchContacts exception: " + e.toString());
            return contacts;

        } finally {
            if (contactCursor != null)
                contactCursor.close();
        }
        return contacts;
    }

    LocalContact getLocalContactfromAndroid(Cursor contactCursor) {

        LocalContact contact = new LocalContact();

        // set the lookup key:
        contact.setLookupKey(getString(contactCursor, LOOKUP_KEY));

        // photos management:
        // get the thumbnail bitmap:
        if (getString(contactCursor, PHOTO_THUMBNAIL_URI) != null) {
            Uri thumbnailUri = Uri.parse(getString(contactCursor, PHOTO_THUMBNAIL_URI));
            contact.setThumbnailUri(thumbnailUri);
        } else {
            contact.setThumbnailUri(null);
        }

        // Build the entity URI on LOOKUP_KEY:
        Uri.Builder b = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, contact.getLookupKey()).buildUpon();
        b.appendPath(ContactsContract.Contacts.Entity.CONTENT_DIRECTORY);
        Uri contactUri = b.build();
        // create the projection (select all columns)
        String[] projectionSt = {};
        String sortOrderSt = ContactsContract.Contacts.Entity.RAW_CONTACT_ID + " ASC";

        // start to query in all data tables of the contact:
        Cursor detailsCursor = context.getContentResolver().query(contactUri, projectionSt, null, null, sortOrderSt);
        if (detailsCursor != null) {
            String mime;
            int mimeIdx = detailsCursor.getColumnIndex(ContactsContract.Contacts.Entity.MIMETYPE);
            while (detailsCursor.moveToNext()) {
                mime = detailsCursor.getString(mimeIdx);
                // get email addresses:
                if (mime != null && mime.equalsIgnoreCase(EMAIL_MIME)) {
                    EmailAddress.EmailType enumType = EmailAddress.EmailType.UNKNOWN;
                    int type = getInt(detailsCursor, EMAIL_TYPE);
                    if (type == (EMAIL_TYPE_WORK)) {
                        enumType = EmailAddress.EmailType.WORK;
                    } else if (type == (EMAIL_TYPE_MOBILE)) {
                        enumType = EmailAddress.EmailType.MOBILE;
                    } else if (type == (EMAIL_TYPE_HOME)) {
                        enumType = EmailAddress.EmailType.HOME;
                    } else if (type == (EMAIL_TYPE_CUSTOM)) {
                        enumType = EmailAddress.EmailType.CUSTOM;
                    } else {
                        enumType = EmailAddress.EmailType.OTHER;
                    }
                    contact.addEmailAddress(getString(detailsCursor, EMAIL_ADDRESS), enumType);
                }

                // get last and first name:
                if (mime != null && mime.equalsIgnoreCase(NAME_MIME)) {
                    if (getString(detailsCursor, FAMILY_NAME) != null) {
                        contact.setLastName(getString(detailsCursor, FAMILY_NAME));
                    }
                    String firstName = "";
                    if (getString(detailsCursor, GIVEN_NAME) != null) {
                        firstName = getString(detailsCursor, GIVEN_NAME);
                    }
                    if (getString(detailsCursor, MIDDLE_NAME) != null) {
                        firstName = firstName + " " + getString(detailsCursor, MIDDLE_NAME);
                    }
                    contact.setFirstName(firstName);
                }

                // get nickname:
                if (mime != null && mime.equalsIgnoreCase(NICKNAME_MIME)) {
                    if (getString(detailsCursor, NICKNAME) != null) {
                        contact.setNickName(getString(detailsCursor, NICKNAME));
                    }
                }

                // get company name and job title:
                if (mime != null && mime.equalsIgnoreCase(ORGANIZATION_MIME)) {
                    if (getString(detailsCursor, JOB_TITLE) != null) {
                        contact.setJobTitle(getString(detailsCursor, JOB_TITLE));
                    }
                    if (getString(detailsCursor, COMPANY) != null) {
                        contact.setCompanyName(getString(detailsCursor, COMPANY));
                    }
                }

                // get postal addresses:
                if (mime != null && mime.equalsIgnoreCase(ADDRESS_MIME)) {
                    PostalAddress.AddressType enumType = PostalAddress.AddressType.UNKNOWN;
                    int type = getInt(detailsCursor, ADDRESS_TYPE);
                    if (type == (ADDRESS_TYPE_WORK)) {
                        enumType = PostalAddress.AddressType.WORK;
                    } else if (type == (ADDRESS_TYPE_HOME)) {
                        enumType = PostalAddress.AddressType.HOME;
                    } else if (type == (ADDRESS_TYPE_CUSTOM)) {
                        enumType = PostalAddress.AddressType.CUSTOM;
                    } else {
                        enumType = PostalAddress.AddressType.OTHER;
                    }
                    contact.addPostalAddress(getString(detailsCursor, ADDRESS), enumType);
                }

                // get web site addresses:
                if (mime != null && mime.equalsIgnoreCase(WEB_MIME)) {
                    WebSite.WebSiteType enumType = WebSite.WebSiteType.UNKNOWN;
                    int type = getInt(detailsCursor, WEB_TYPE);
                    if (type == (WEB_TYPE_WORK)) {
                        enumType = WebSite.WebSiteType.WORK;
                    } else if (type == (WEB_TYPE_HOME)) {
                        enumType = WebSite.WebSiteType.HOME;
                    } else if (type == (WEB_TYPE_HOMEPAGE)) {
                        enumType = WebSite.WebSiteType.HOMEPAGE;
                    } else if (type == (WEB_TYPE_BLOG)) {
                        enumType = WebSite.WebSiteType.BLOG;
                    } else if (type == (WEB_TYPE_CUSTOM)) {
                        enumType = WebSite.WebSiteType.CUSTOM;
                    } else {
                        enumType = WebSite.WebSiteType.OTHER;
                    }
                    contact.addWebSite(getString(detailsCursor, WEB_SITE), enumType);
                }

                // get phone numbers:
                if (mime != null && mime.equalsIgnoreCase(PHONE_MIME)) {
                    PhoneNumber.PhoneNumberType enumType;
                    int type = getInt(detailsCursor, PHONE_TYPE);
                    if (type == (PHONE_TYPE_COMPANY_MAIN)) {
                        enumType = PhoneNumber.PhoneNumberType.COMPANY_MAIN;
                    } else if (type == (PHONE_TYPE_WORK)) {
                        enumType = PhoneNumber.PhoneNumberType.WORK;
                    } else if (type == (PHONE_TYPE_HOME)) {
                        enumType = PhoneNumber.PhoneNumberType.HOME;
                    } else if (type == (PHONE_TYPE_MOBILE)) {
                        enumType = PhoneNumber.PhoneNumberType.MOBILE;
                    } else if (type == (PHONE_TYPE_FAX_WORK)) {
                        enumType = PhoneNumber.PhoneNumberType.FAX_WORK;
                    } else if (type == (PHONE_TYPE_FAX_HOME)) {
                        enumType = PhoneNumber.PhoneNumberType.FAX_HOME;
                    } else if (type == (PHONE_TYPE_WORK_MOBILE)) {
                        enumType = PhoneNumber.PhoneNumberType.WORK_MOBILE;
                    } else if (type == (PHONE_TYPE_MAIN)) {
                        enumType = PhoneNumber.PhoneNumberType.MAIN;
                    } else if (type == (PHONE_TYPE_CUSTOM)) {
                        enumType = PhoneNumber.PhoneNumberType.CUSTOM;
                    } else {
                        enumType = PhoneNumber.PhoneNumberType.OTHER;
                    }

                    String number = getString(detailsCursor, PHONE_NUMBER);
                    contact.addPhoneNumber(number, null, enumType);
                }
            }
            detailsCursor.close();
        }

        Log.getLogger().verbose(LOG_TAG, "Contact " + contact.getDisplayName4Log("") + " completely filled");

        return contact;
    }

    @Override
    public synchronized void retrieveAllVisibleGroups() {
        Log.getLogger().verbose(LOG_TAG, ">retrieveAllVisibleGroups");
        if (isContactPermissionNotAllowed())
            return;

        m_visibleGroups.clear();

        Cursor groupsCursor= context.getContentResolver().query(ContactsContract.Groups.CONTENT_URI,
                new String[]{
                        ContactsContract.Groups._ID,
                        ContactsContract.Groups.TITLE,
                        ContactsContract.Groups.GROUP_VISIBLE
                }, null, null, null
        );
        if(groupsCursor!=null){
            while(groupsCursor.moveToNext()){
                String group_title = groupsCursor.getString(groupsCursor.getColumnIndex(ContactsContract.Groups.TITLE));
                String id = groupsCursor.getString(groupsCursor.getColumnIndex(ContactsContract.Groups._ID));
                int visible = groupsCursor.getInt(groupsCursor.getColumnIndex(ContactsContract.Groups.GROUP_VISIBLE));
                Log.getLogger().verbose(LOG_TAG, "Group " + group_title + "("+ id +") visibility = " + visible);
                if( visible > 0 ) {
                    Log.getLogger().verbose(LOG_TAG, "Group " + group_title + " is visible");
                    m_visibleGroups.put(id, group_title);
                }
            }
            groupsCursor.close();
        }
    }

    private synchronized boolean checkContactInVisibleGroup(String lookupKey) {

        List<String> contactGroupsId = getGroupIdForContact(lookupKey);
        Log.getLogger().verbose(LOG_TAG, "Contact is in groups ;" +String.valueOf(contactGroupsId));

        List<String> visibleGroupsNameList = new ArrayList<>(m_visibleGroups.keySet());
        for(String visibleGroupId : visibleGroupsNameList) {
            for(String contactGroupId : contactGroupsId) {
                if (contactGroupId.equalsIgnoreCase(visibleGroupId)) {
                    Log.getLogger().verbose(LOG_TAG, "Contact is in Visible group");
                    return true;
                }
            }
        }

        return false;
    }

    public List<String> getGroupIdForContact(String lookupKey) {
        List<String> groupsId = new ArrayList<>();

        String where = String.format("(( %s = ? ) AND ( %s = ? ))", ContactsContract.Data.LOOKUP_KEY, ContactsContract.Contacts.Data.MIMETYPE);
        String[] whereArgs = {lookupKey, ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE};

        Cursor cursor = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID},
                where, whereArgs, null);
        if (cursor!=null) {
            while(cursor.moveToNext()) {
                int groupIdIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID);
                groupsId.add(cursor.getString(groupIdIndex));
            }
            cursor.close();
        }

        return groupsId;
    }

    private String getString(Cursor cursor, String identifier)
    {
        return cursor.getString(cursor.getColumnIndex(identifier));
    }

    private int getInt(Cursor cursor, String identifier)
    {
        return cursor.getInt(cursor.getColumnIndex(identifier));
    }

    List<IContact> getAllVisibleContacts() {
        List<IContact> contacts = new ArrayList<>();
        Cursor cursor = getVisibleLocalAndroidContacts();

        if (cursor!=null) {
            try {
                while (cursor.moveToNext()) {
                    LocalContact contact = getLocalContactfromAndroid(cursor);
                    if (contact != null && !contact.getEmailAddresses().isEmpty())
                        contacts.add(contact);
                }
                return contacts;
            } catch (Exception e) {
                Log.getLogger().warn(LOG_TAG, "getAllVisibleContacts exception: " + e.toString());
                return contacts;

            } finally {
                cursor.close();
            }
        }
        return contacts;
    }

    private Cursor getVisibleLocalAndroidContacts() {
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_URI, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI};
        String selectionClause = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '" + ("1") + "'";
        String[] selectionArgs = null;

        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME+ " COLLATE LOCALIZED ASC";

        return context.getContentResolver().query(uri, projection, selectionClause, selectionArgs, sortOrder);
    }
    
}
