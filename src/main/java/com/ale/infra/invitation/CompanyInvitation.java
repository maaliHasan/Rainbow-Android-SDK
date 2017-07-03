package com.ale.infra.invitation;

import com.ale.infra.contact.Contact;
import com.ale.infra.data_model.IMultiSelectable;
import com.ale.util.log.Log;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by trunk1 on 01/12/2016.
 */

public class CompanyInvitation implements IMultiSelectable, ICompanyInvitationListener {
    private static final String LOG_TAG = CompanyInvitation.class.getSimpleName();

    private String m_id;
    private String m_invitedUserId;
    private String m_invitedUserEmail;
    private String m_invitingUserId;
    private String m_invitingUserEmail;
    private String m_requestedNotificationLanguage;
    private Date m_invitingDate;
    private Date m_lastNotificationDate;
    private Date m_acceptationDate;
    private Date m_declinationDate;
    private Invitation.InvitationStatus m_status = Invitation.InvitationStatus.UNDEFINED;
//    private Invitation.InvitationType m_type;
    private Contact m_invitingContact;
    private Contact m_invitedContact;
    private String m_companyId;

    public enum DisplayListCompanyInvitationType {INVITING_CONTACT};
    private boolean m_clickActionInProgress = false;
    private Set<ICompanyInvitationListener> m_changeListeners = new HashSet<>();

    @Override
    public int getSelectableType() {
        return 0;
    }

    public CompanyInvitation() {
        super();
    }

    public String getId() {
        return m_id;
    }

    public void setId(String id) {
        this.m_id = id;
    }

    public String getInvitedUserId() {
        return m_invitedUserId;
    }

    public void setInvitedUserId(String userId) {
        this.m_invitedUserId = userId;
    }

    public String getInvitingUserId() {
        return m_invitingUserId;
    }

    public void setInvitingUserId(String userId) {
        this.m_invitingUserId = userId;
    }

    public String getInvitedUserEmail() {
        return m_invitedUserEmail;
    }

    public void setInvitedUserEmail(String userEmail) {
        this.m_invitedUserEmail = userEmail;
    }

    public String getInvitingUserEmail() {
        return m_invitingUserEmail;
    }

    public void setInvitingUserEmail(String userEmail) {
        this.m_invitingUserEmail = userEmail;
    }

    public String getRequestedNotificationLanguage() {
        return m_requestedNotificationLanguage;
    }

    public void setRequestedNotificationLanguage(String requestedNotificationLanguage) {
        this.m_requestedNotificationLanguage = requestedNotificationLanguage;
    }

    public Date getInvitingDate() {
        return m_invitingDate;
    }

    public void setInvitingDate(Date invitingDate) {
        this.m_invitingDate = invitingDate;
    }

    public Date getLastNotificationDate() {
        return m_lastNotificationDate;
    }

    public String getLastNotificationDateToString() {
        return m_lastNotificationDate == null ? "" : m_lastNotificationDate.toString();
    }

    public void setLastNotificationDate(Date lastNotificationDate) {
        this.m_lastNotificationDate = lastNotificationDate;
    }

    public Date getAcceptationDate() {
        return m_acceptationDate;
    }

    public String getAcceptationDateToString() {
        return m_acceptationDate == null ? "" : m_acceptationDate.toString();
    }

    public void setAcceptationDate(Date acceptationDate) {
        this.m_acceptationDate = acceptationDate;
    }

    public Date getDeclinationDate() {
        return m_declinationDate;
    }

    public String getDeclinationDateToString() {
        return m_declinationDate == null ? "" : m_declinationDate.toString();
    }

    public void setDeclinationDate(Date declinationDate) {
        this.m_declinationDate = declinationDate;
    }

    public Invitation.InvitationStatus getStatus() {
        return m_status;
    }

    public void setStatus(Invitation.InvitationStatus status) {
        this.m_status = status;
    }

//    public Invitation.InvitationType getType() {
//        return m_type;
//    }
//
//    public void setType(Invitation.InvitationType type) {
//        this.m_type = type;
//    }

    public Contact getInvitingContact() {
        return m_invitingContact;
    }

    public void setInvitingContact(Contact contact) {
        this.m_invitingContact = contact;
    }

    public Contact getInvitedContact() {
        return m_invitedContact;
    }

    public void setInvitedContact(Contact contact) {
        this.m_invitedContact = contact;
    }

    public String getCompanyId() {
        return m_companyId;
    }

    public void setCompanyId(String companyId) {
        this.m_companyId = companyId;
    }

    public boolean isClickActionInProgress() {
        return m_clickActionInProgress;
    }

    public void setClickActionInProgress(boolean clickActionInProgress) {
        m_clickActionInProgress = clickActionInProgress;
        notifyCompanyInvitationUpdated();
    }

    public synchronized void registerChangeListener(ICompanyInvitationListener changeListener)
    {
        if( !m_changeListeners.contains(changeListener)) {
            m_changeListeners.add(changeListener);
        }
    }

    public synchronized void unregisterChangeListener(ICompanyInvitationListener changeListener)
    {
        m_changeListeners.remove(changeListener);
    }

    @Override
    public void companyInvitationUpdated(CompanyInvitation updatedInvitation) {

    }

    public synchronized void notifyCompanyInvitationUpdated()
    {

        for (ICompanyInvitationListener listener : m_changeListeners.toArray(new ICompanyInvitationListener[m_changeListeners.size()]))
        {
            listener.companyInvitationUpdated(this);
        }
    }

    public void dumpInLog(String dumpLogTag) {
        Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
        Log.getLogger().info(dumpLogTag, "    CompanyInvitation:");

        if( m_id != null ) {
            Log.getLogger().info(dumpLogTag, "    id=" + m_id);
        } else {
            Log.getLogger().info(dumpLogTag, "    id=null");
        }

        if( m_companyId != null ) {
            Log.getLogger().info(dumpLogTag, "    companyId=" + m_companyId);
        } else {
            Log.getLogger().info(dumpLogTag, "    companyId=null");
        }

        if( m_invitedUserId != null ) {
            Log.getLogger().info(dumpLogTag, "    invitedUserId=" + m_invitedUserId);
        } else {
            Log.getLogger().info(dumpLogTag, "    invitedUserId=null");
        }

        if( m_invitedUserEmail != null ) {
            Log.getLogger().info(dumpLogTag, "    invitedUserEmail=" + m_invitedUserEmail);
        } else {
            Log.getLogger().info(dumpLogTag, "    invitedUserEmail=null");
        }

        if( m_invitingUserId != null ) {
            Log.getLogger().info(dumpLogTag, "    invitingUserId=" + m_invitingUserId);
        } else {
            Log.getLogger().info(dumpLogTag, "    invitingUserId=null");
        }

        if( m_invitingUserEmail != null ) {
            Log.getLogger().info(dumpLogTag, "    invitingUserEmail=" + m_invitingUserEmail);
        } else {
            Log.getLogger().info(dumpLogTag, "    invitingUserEmail=null");
        }

        if( m_requestedNotificationLanguage != null ) {
            Log.getLogger().info(dumpLogTag, "    requestedNotificationLanguage=" + m_requestedNotificationLanguage);
        } else {
            Log.getLogger().info(dumpLogTag, "    requestedNotificationLanguage=null");
        }

        if( m_invitingDate != null ) {
            Log.getLogger().info(dumpLogTag, "    invitingDate=" + m_invitingDate.toString());
        } else {
            Log.getLogger().info(dumpLogTag, "    invitingDate=null");
        }

        if( m_lastNotificationDate != null ) {
            Log.getLogger().info(dumpLogTag, "    lastNotificationDate=" + m_lastNotificationDate.toString());
        } else {
            Log.getLogger().info(dumpLogTag, "    lastNotificationDate=null");
        }

        if( m_acceptationDate != null ) {
            Log.getLogger().info(dumpLogTag, "    acceptationDate=" + m_acceptationDate.toString());
        } else {
            Log.getLogger().info(dumpLogTag, "    acceptationDate=null");
        }

        if( m_declinationDate != null ) {
            Log.getLogger().info(dumpLogTag, "    declinationDate=" + m_declinationDate.toString());
        } else {
            Log.getLogger().info(dumpLogTag, "    declinationDate=null");
        }

        Log.getLogger().info(dumpLogTag, "    status=" + m_status.toString());
//        Log.getLogger().info(dumpLogTag, "    type=" + m_type.toString());

        Log.getLogger().info(dumpLogTag, "    clickActionInProgress=" + String.valueOf(m_clickActionInProgress));

        if( m_invitingContact != null ) {
            Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
            Log.getLogger().info(dumpLogTag, "    invitingContact=");
            m_invitingContact.dumpInLog(dumpLogTag);
        }

        if( m_invitedContact != null ) {
            Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
            Log.getLogger().info(dumpLogTag, "    invitedContact=");
            m_invitedContact.dumpInLog(dumpLogTag);
        }

        Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
    }
}