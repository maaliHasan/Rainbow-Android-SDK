package com.ale.infra.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.contact.EmailAddress;
import com.ale.infra.contact.IContact;
import com.ale.infra.contact.PhoneNumber;
import com.ale.infra.contact.RainbowPresence;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by wilsius on 24/11/16.
 */

public class ContactDataSource {
    private static final String LOG_TAG = "ContactDataSource";
    // Database fields
    private SQLiteDatabase database;
    private Object syncObject;

    public ContactDataSource(SQLiteDatabase database, Object syncObject) {
        this.database = database;
        this.syncObject = syncObject;
    }

    public void createOrUpdateContact(Contact contact) {
        synchronized (syncObject) {
            if (!database.isOpen())
                return;

            if (contact.getDirectoryContact() == null)
                return;

            if (StringsUtil.isNullOrEmpty(contact.getCorporateId()) && StringsUtil.isNullOrEmpty(contact.getImJabberId())) {
                Log.getLogger().warn(LOG_TAG, "No JID and no ID for contact");
                return;
            }

            if (!StringsUtil.isNullOrEmpty(contact.getImJabberId()) && contact.getImJabberId().startsWith("room_")) {
                Log.getLogger().debug(LOG_TAG, "Not a contact - skip");
                return;
            }

            ContentValues values;

            try {
                database.beginTransaction();
                Cursor cursor = getContactIndex(contact.getCorporateId(), contact.getImJabberId());
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.close();
                    deleteContact(contact);
                } else if (cursor != null) {
                    cursor.close();
                }
                deleteContact(contact);

                //Log.getLogger().verbose(LOG_TAG, "Created contact is" + contact.getDirectoryContact().toString());
                values = getContentValue(contact.getDirectoryContact());
                database.insert(DatabaseHelper.TABLE_CONTACT, null, values);
                database.setTransactionSuccessful();
            } catch (Exception e) {
                Log.getLogger().warn(LOG_TAG, "Exception: " + e.getMessage());
            } finally {
                database.endTransaction();
            }
        }
    }

    private DirectoryContact getContact(Cursor cursor, boolean addToContactCache) {
        if (cursor.getCount() == 0) return null;

        DirectoryContact dirContact = new DirectoryContact();

        if (cursor.getLong(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_IS_ROSTER)) == 0L) {
            dirContact.setIsRoster(false);
        } else {
            dirContact.setIsRoster(true);
            dirContact.setPresence(null, RainbowPresence.OFFLINE);
        }

        dirContact.setImJabberId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_JID)));
        dirContact.setCountry(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_COUNTRY)));
        dirContact.setLanguage(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_LANGUAGE)));
        dirContact.setLoginEmail(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_LOGIN_EMAIL)));
        dirContact.setJidTel(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_JIDTEL)));

        String val = cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_TYPE));
        if (val.equals("BOT"))
            dirContact.setType(DirectoryContact.DirectoryContactType.BOT);
        else
            dirContact.setType(DirectoryContact.DirectoryContactType.USER);

        if (cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_ROLE_ADMIN) != -1) {
            val = cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_ROLE_ADMIN));
            if (!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(val))
                dirContact.getRole().add(IContact.ContactRole.fromString(val));
        }
        if (cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_ROLE_USER) != -1) {
            val = cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_ROLE_USER));
            if (!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(val))
                dirContact.getRole().add(IContact.ContactRole.fromString(val));
        }
        if (cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_ROLE_GUEST) != -1) {
            val = cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_ROLE_GUEST));
            if (!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(val))
                dirContact.getRole().add(IContact.ContactRole.fromString(val));
        }

        dirContact.setFirstName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_FIRSTNAME)));
        dirContact.setLastName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_LASTNAME)));
        dirContact.setCompanyName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_COMPANYNAME)));
        dirContact.setJobTitle(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_JOBTITILE)));
        dirContact.setTitle(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_TITILE)));
        dirContact.setNickName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_NICKNAME)));

        dirContact.addPhoneNumber(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_PERSONAL_PHONE_NUMBER)), null, PhoneNumber.PhoneNumberType.HOME);
        dirContact.addPhoneNumber(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_MOBILE_PHONE_NUMBER)), null, PhoneNumber.PhoneNumberType.MOBILE);
        dirContact.addPhoneNumber(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_OFFICE_PHONE_NUMBER)), null, PhoneNumber.PhoneNumberType.WORK);
        dirContact.addPhoneNumber(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_PERSONAL_MOBILE_PHONE_NUMBER)), null, PhoneNumber.PhoneNumberType.WORK_MOBILE);

        dirContact.addEmailAddress(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_EMAILADDRESS)), EmailAddress.EmailType.WORK);
        dirContact.setCorporateId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_CORPORATEID)));
        dirContact.setCompanyId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_COMPANYID)));
        String lastAvatarUpdate = cursor.getString(cursor.getColumnIndex(DatabaseHelper.DIRCONTACT_LASTAVATARUPDATE));
        if (StringsUtil.isNullOrEmpty(lastAvatarUpdate))
            dirContact.setLastAvatarUpdateDate(null);
        else
            dirContact.setLastAvatarUpdateDate(lastAvatarUpdate);

        //Resolve Contact
        if( addToContactCache ) {
            RainbowContext.getInfrastructure().getContactCacheMgr().resolveContactFromDB(dirContact);
        }

        return dirContact;
    }

    public boolean isEmpty() {
        synchronized (syncObject) {
            if (!database.isOpen())
                return true;

            String selectQuery = "SELECT  * FROM " + DatabaseHelper.TABLE_CONTACT;
            Cursor cursor = database.rawQuery(selectQuery, null);

            boolean isEmpty = cursor.getCount() <= 0;
            cursor.close();

            return isEmpty;
        }


    }

    public List<DirectoryContact> getAllContacts(boolean addToContactCache, boolean logEnabled) {
        List<DirectoryContact> contacts = new ArrayList<>();
        synchronized (syncObject) {
            String selectQuery = "SELECT  * FROM " + DatabaseHelper.TABLE_CONTACT;
            Cursor cursor = null;
            Log.getLogger().info(LOG_TAG, "getAllContacts BEG");
            try {
                if (!database.isOpen())
                    return contacts;

                cursor = database.rawQuery(selectQuery, null);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    DirectoryContact contact;

                    do {
                        contact = getContact(cursor, addToContactCache);
                        if (logEnabled)
                            Log.getLogger().info(LOG_TAG, "Dumped contact is" + contact.toString());
                        contacts.add(contact);
                    } while (cursor.moveToNext());
                }
                // make sure to disconnect the cursor
                cursor.close();
                //Log.getLogger().info(LOG_TAG, " database contains :" + contacts.size());
                return contacts;
            } catch (Exception e) {
                Log.getLogger().error(LOG_TAG, " getAllContacts exception " + e.toString());
                if (cursor != null) cursor.close();
                return new ArrayList<>();
            } finally {
                Log.getLogger().info(LOG_TAG, "getAllContacts END: " + contacts.size());
            }
        }
    }

    private Cursor getContactIndex(String id, String jid) {
        if( StringsUtil.isNullOrEmpty(id) && StringsUtil.isNullOrEmpty(jid)) {
            return null;
        }

        Cursor cursor = null;
        List<String> whereArgs = new ArrayList<>();
        StringBuilder selectionClause = new StringBuilder();
        String selectionId = "";
        String selectionJid = "";

        try {
            if (!database.isOpen())
                return null;

            if (!StringsUtil.isNullOrEmpty((id))) {
                selectionId = DatabaseHelper.DIRCONTACT_CORPORATEID + " = ?";
                whereArgs.add(id.toString());
            }

            if (!StringsUtil.isNullOrEmpty((jid))) {
                selectionJid = DatabaseHelper.DIRCONTACT_JID + " = ?";
                whereArgs.add(jid.toString());
            }

            if (!StringsUtil.isNullOrEmpty((id)) && !StringsUtil.isNullOrEmpty((jid))) {
                selectionClause.append("( ");
                selectionClause.append(selectionId);
                selectionClause.append(" ) OR ( ");
                selectionClause.append(selectionJid);
                selectionClause.append(" )");
            } else {
                selectionClause.append(selectionId);
                selectionClause.append(selectionJid);
            }

            cursor = database.query(DatabaseHelper.TABLE_CONTACT, null,
                    selectionClause.toString(),
                    whereArgs.toArray(new String[whereArgs.size()]),
                    null, null, null);

            return cursor;
        } catch (Exception e) {
            Log.getLogger().error(LOG_TAG, " getContactIndex exception " + e.toString());
            return null;
        }
    }


    public void deleteContact(Contact contact) {
        synchronized (syncObject) {
            if (!database.isOpen())
                return;
            Cursor cursor = getContactIndex(contact.getCorporateId(), contact.getImJabberId());
            if (cursor != null) {
                try {
                    while (cursor.moveToNext()) {
                        int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
                        String[] whereArgs = new String[]{String.valueOf(id),};
                        database.delete(DatabaseHelper.TABLE_CONTACT, DatabaseHelper.COLUMN_ID + " = ?", whereArgs);
                    }
                    cursor.close();
                } catch (Exception e) {
                    Log.getLogger().error(LOG_TAG, " deleteContact exception " + e.toString());
                }
            }
        }
    }


    public void setIsRoster(Contact contact, boolean b) {
        synchronized (syncObject) {
            if (!database.isOpen())
                return;
            ContentValues values = new ContentValues();

            DirectoryContact dirContact = contact.getDirectoryContact();
            if (dirContact != null) {
                values.put(DatabaseHelper.DIRCONTACT_IS_ROSTER,
                        b ? 1L : 0L);

                try {
                    Cursor cursor = getContactIndex(contact.getCorporateId(), contact.getImJabberId());
                    if (cursor != null) {
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
                            String[] whereArgs = new String[]{
                                    String.valueOf(id),
                            };
                            database.update(DatabaseHelper.TABLE_CONTACT, values,
                                    DatabaseHelper.COLUMN_ID + " = ?", whereArgs);
                        }
                        cursor.close();
                    }
                } catch (Exception e) {
                    Log.getLogger().error(LOG_TAG, " setIsRoster exception " + e.toString());
                }
            }
        }
    }

    public void logInformations() {
        List<DirectoryContact> dbContacts = getAllContacts(false,true);

    }


    private ContentValues getContentValue (DirectoryContact dirContact) {
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.DIRCONTACT_IS_ROSTER,
                dirContact.isRoster() ? 1L : 0L);
        values.put(DatabaseHelper.DIRCONTACT_JID,
                StringsUtil.isNullOrEmpty(dirContact.getImJabberId()) ? "" : dirContact.getImJabberId());

        values.put(DatabaseHelper.DIRCONTACT_COUNTRY,
                StringsUtil.isNullOrEmpty(dirContact.getCountry()) ? "" : dirContact.getCountry());

        values.put(DatabaseHelper.DIRCONTACT_LANGUAGE,
                StringsUtil.isNullOrEmpty(dirContact.getLanguage()) ? "" : dirContact.getLanguage());

        values.put(DatabaseHelper.DIRCONTACT_LOGIN_EMAIL,
                StringsUtil.isNullOrEmpty(dirContact.getLoginEmail()) ? "" : dirContact.getLoginEmail());

        values.put(DatabaseHelper.DIRCONTACT_JIDTEL,
                StringsUtil.isNullOrEmpty(dirContact.getJidTel()) ? "" : dirContact.getJidTel());

        values.put(DatabaseHelper.CONVERSATION_TYPE,
                StringsUtil.isNullOrEmpty(dirContact.getType().toString()) ? "" : dirContact.getType().toString());

        values.put(DatabaseHelper.DIRCONTACT_ROLE,
                dirContact.getRole().size() == 0 ? "" : dirContact.getRole().toString());

        values.put(DatabaseHelper.DIRCONTACT_FIRSTNAME,
                StringsUtil.isNullOrEmpty(dirContact.getFirstName()) ? "" : dirContact.getFirstName());

        values.put(DatabaseHelper.DIRCONTACT_LASTNAME,
                StringsUtil.isNullOrEmpty(dirContact.getLastName()) ? "" : dirContact.getLastName());

        values.put(DatabaseHelper.DIRCONTACT_COMPANYNAME,
                StringsUtil.isNullOrEmpty(dirContact.getCompanyName()) ? "" : dirContact.getCompanyName());

        values.put(DatabaseHelper.DIRCONTACT_JOBTITILE,
                StringsUtil.isNullOrEmpty(dirContact.getJobTitle()) ? "" : dirContact.getJobTitle());

        values.put(DatabaseHelper.DIRCONTACT_TITILE,
                StringsUtil.isNullOrEmpty(dirContact.getTitle()) ? "" : dirContact.getTitle());


        values.put(DatabaseHelper.DIRCONTACT_NICKNAME,
                StringsUtil.isNullOrEmpty(dirContact.getNickName()) ? "" : dirContact.getNickName());

        values.put(DatabaseHelper.DIRCONTACT_PERSONAL_PHONE_NUMBER,
                StringsUtil.isNullOrEmpty(dirContact.getFirstPersonalPhoneNumber()) ? "" : dirContact.getFirstPersonalPhoneNumber());

        values.put(DatabaseHelper.DIRCONTACT_MOBILE_PHONE_NUMBER,
                StringsUtil.isNullOrEmpty(dirContact.getFirstMobilePhoneNumber()) ? "" : dirContact.getFirstMobilePhoneNumber());

        PhoneNumber officePhone = dirContact.getFirstOfficePhoneNumber();
        if (officePhone != null)
            values.put(DatabaseHelper.DIRCONTACT_OFFICE_PHONE_NUMBER,
                    StringsUtil.isNullOrEmpty(officePhone.getPhoneNumberValue()) ? "" : officePhone.getPhoneNumberValue());
        else
            values.put(DatabaseHelper.DIRCONTACT_OFFICE_PHONE_NUMBER, "");

        values.put(DatabaseHelper.DIRCONTACT_PERSONAL_MOBILE_PHONE_NUMBER,
                StringsUtil.isNullOrEmpty(dirContact.getFirstPersonalMobilePhoneNumber()) ? "" : dirContact.getFirstPersonalMobilePhoneNumber());

        values.put(DatabaseHelper.DIRCONTACT_EMAILADDRESS,
                StringsUtil.isNullOrEmpty(dirContact.getFirstEmailAddress()) ? "" : dirContact.getFirstEmailAddress());

// TODO fill postal address
        values.put(DatabaseHelper.DIRCONTACT_POSTALADDRESS,
                "");

        values.put(DatabaseHelper.DIRCONTACT_CORPORATEID,
                StringsUtil.isNullOrEmpty(dirContact.getCorporateId()) ? "" : dirContact.getCorporateId());

        values.put(DatabaseHelper.DIRCONTACT_COMPANYID,
                StringsUtil.isNullOrEmpty(dirContact.getCompanyId()) ? "" : dirContact.getCompanyId());

        values.put(DatabaseHelper.DIRCONTACT_LASTAVATARUPDATE,
                StringsUtil.isNullOrEmpty(dirContact.getLastAvatarUpdateDate()) ? "" : dirContact.getLastAvatarUpdateDate());

        values.put(DatabaseHelper.LAST_UPDATED,new Date().getTime());

        return values;
    }

}

