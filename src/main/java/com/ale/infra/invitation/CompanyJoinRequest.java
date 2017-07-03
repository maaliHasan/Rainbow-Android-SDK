package com.ale.infra.invitation;

import com.ale.infra.contact.Contact;
import com.ale.infra.data_model.IMultiSelectable;
import com.ale.util.log.Log;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by trunk1 on 10/02/2017.
 */

public class CompanyJoinRequest implements IMultiSelectable, ICompanyJoinRequestListener {
    private static final String LOG_TAG = CompanyJoinRequest.class.getSimpleName();

    private String m_id;
    private String m_requestingUserId;
    private String m_requestingUserLoginEmail;
    private String m_requestedCompanyId;
    private String m_requestedCompanyName;
    private String m_requestedToCompanyAdmin;
    private String m_companyAdminId;
    private String m_companyAdminLoginEmail;
    private String m_requestedNotificationLanguage;

    private Date m_requestingDate;
    private Date m_lastNotificationDate;

    private CompanyJoinRequestStatus m_status = CompanyJoinRequestStatus.UNDEFINED;
    private CompanyContact m_companyContact;

    //    public enum DisplayListCompanyInvitationType {INVITING_CONTACT};
    private boolean m_clickActionInProgress = false;
    private Set<ICompanyJoinRequestListener> m_changeListeners = new HashSet<>();
    public enum CompanyJoinRequestStatus
    {
        PENDING("pending"), ACCEPTED("accepted"), DECLINED("declined"), CANCELED("canceled"), FAILED("failed"), UNDEFINED("undefined");
        private String value;

        CompanyJoinRequestStatus(String value) {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return value;
        }

        public static CompanyJoinRequestStatus fromString(String text) {
            if (text != null) {
                for (CompanyJoinRequestStatus status : CompanyJoinRequestStatus.values()) {
                    if (text.equalsIgnoreCase(status.value)) {
                        return status;
                    }
                }
            }
            return UNDEFINED;
        }
    }

    public CompanyJoinRequest() {
        super();
    }

    public String getId() {
        return m_id;
    }

    public void setId(String id) {
        this.m_id = id;
    }

    public String getRequestingUserId() {
        return m_requestingUserId;
    }

    public void setRequestingUserId(String userId) {
        this.m_requestingUserId = userId;
    }

    public String getRequestingUserLoginEmail() {
        return m_requestingUserLoginEmail;
    }

    public void setRequestingUserLoginEmail(String userId) {
        this.m_requestingUserLoginEmail = userId;
    }

    public String getRequestedCompanyId() {
        return m_requestedCompanyId;
    }

    public void setRequestedCompanyId(String userEmail) {
        this.m_requestedCompanyId = userEmail;
    }

    public String getRequestedCompanyName() {
        return m_requestedCompanyName;
    }

    public void setRequestedCompanyName(String userEmail) {
        this.m_requestedCompanyName = userEmail;
    }

    public String getRequestedToCompanyAdmin() {
        return m_requestedToCompanyAdmin;
    }

    public void setRequestedToCompanyAdmin(String userEmail) {
        this.m_requestedToCompanyAdmin = userEmail;
    }

    public String getCompanyAdminId() {
        return m_companyAdminId;
    }

    public void setCompanyAdminId(String userEmail) {
        this.m_companyAdminId = userEmail;
    }

    public String getCompanyAdminLoginEmail() {
        return m_companyAdminLoginEmail;
    }

    public void setCompanyAdminLoginEmail(String userEmail) {
        this.m_companyAdminLoginEmail = userEmail;
    }

    public String getRequestedNotificationLanguage() {
        return m_requestedNotificationLanguage;
    }

    public void setRequestedNotificationLanguage(String requestedNotificationLanguage) {
        this.m_requestedNotificationLanguage = requestedNotificationLanguage;
    }

    public Date getRquestingDate() {
        return m_requestingDate;
    }

    public void setRequestingDate(Date invitingDate) {
        this.m_requestingDate = invitingDate;
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

    public CompanyJoinRequestStatus getStatus() {
        return m_status;
    }

    public void setStatus(CompanyJoinRequestStatus status) {
        this.m_status = status;
    }

    public CompanyContact getCompanyContact() {
        return m_companyContact;
    }

    public void setCompanyContact(CompanyContact companyContact) {
        this.m_companyContact = companyContact;
    }

    public boolean isClickActionInProgress() {
        return m_clickActionInProgress;
    }

    public void setClickActionInProgress(boolean clickActionInProgress) {
        m_clickActionInProgress = clickActionInProgress;
        notifyCompanyJoinRequestUpdated();
    }

    public synchronized void registerChangeListener(ICompanyJoinRequestListener changeListener)
    {
        if( !m_changeListeners.contains(changeListener)) {
            m_changeListeners.add(changeListener);
        }
    }

    public synchronized void unregisterChangeListener(ICompanyJoinRequestListener changeListener)
    {
        m_changeListeners.remove(changeListener);
    }

    public synchronized void notifyCompanyJoinRequestUpdated()
    {

        for (ICompanyJoinRequestListener listener : m_changeListeners.toArray(new ICompanyJoinRequestListener[m_changeListeners.size()]))
        {
            listener.companyJoinRequestUpdated(this);
        }
    }

    @Override
    public void companyJoinRequestUpdated(CompanyJoinRequest updatedJoinRequest){

    }

    @Override
    public int getSelectableType() {
        return 0;
    }

    public void dumpInLog(String dumpLogTag) {
        Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
        Log.getLogger().info(dumpLogTag, "    CompanyJoinRequest:");
        if( m_id != null ) {
            Log.getLogger().info(dumpLogTag, "    id=" + m_id);
        } else {
            Log.getLogger().info(dumpLogTag, "    id=null");
        }

        if( m_requestingUserId != null ) {
            Log.getLogger().info(dumpLogTag, "    requestingUserId=" + m_requestingUserId);
        } else {
            Log.getLogger().info(dumpLogTag, "    requestingUserId=null");
        }

        if( m_requestingUserLoginEmail != null ) {
            Log.getLogger().info(dumpLogTag, "    requestingUserLoginEmail=" + m_requestingUserLoginEmail);
        } else {
            Log.getLogger().info(dumpLogTag, "    requestingUserLoginEmail=null");
        }

        if( m_requestedCompanyId != null ) {
            Log.getLogger().info(dumpLogTag, "    requestedCompanyId=" + m_requestedCompanyId);
        } else {
            Log.getLogger().info(dumpLogTag, "    requestedCompanyId=null");
        }

        if( m_requestedCompanyName != null ) {
            Log.getLogger().info(dumpLogTag, "    requestedCompanyName=" + m_requestedCompanyName);
        } else {
            Log.getLogger().info(dumpLogTag, "    requestedCompanyName=null");
        }

        if( m_requestedToCompanyAdmin != null ) {
            Log.getLogger().info(dumpLogTag, "    requestedToCompanyAdmin=" + m_requestedToCompanyAdmin);
        } else {
            Log.getLogger().info(dumpLogTag, "    requestedToCompanyAdmin=null");
        }

        if( m_companyAdminId != null ) {
            Log.getLogger().info(dumpLogTag, "    companyAdminId=" + m_companyAdminId);
        } else {
            Log.getLogger().info(dumpLogTag, "    companyAdminId=null");
        }

        if( m_companyAdminLoginEmail != null ) {
            Log.getLogger().info(dumpLogTag, "    companyAdminLoginEmail=" + m_companyAdminLoginEmail);
        } else {
            Log.getLogger().info(dumpLogTag, "    companyAdminLoginEmail=null");
        }

        if( m_requestedNotificationLanguage != null ) {
            Log.getLogger().info(dumpLogTag, "    requestedNotificationLanguage=" + m_requestedNotificationLanguage);
        } else {
            Log.getLogger().info(dumpLogTag, "    requestedNotificationLanguage=null");
        }

        if( m_requestingDate != null ) {
            Log.getLogger().info(dumpLogTag, "    requestingDate=" + m_requestingDate.toString());
        } else {
            Log.getLogger().info(dumpLogTag, "    requestingDate=null");
        }
        if( m_lastNotificationDate != null ) {
            Log.getLogger().info(dumpLogTag, "    lastNotificationDate=" + m_lastNotificationDate.toString());
        } else {
            Log.getLogger().info(dumpLogTag, "    lastNotificationDate=null");
        }

        Log.getLogger().info(dumpLogTag, "    status=" + m_status.toString());
        Log.getLogger().info(dumpLogTag, "    clickActionInProgress=" + String.valueOf(m_clickActionInProgress));

        if( m_companyContact != null ) {
            Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
            Log.getLogger().info(dumpLogTag, "    companyContact=");
            m_companyContact.dumpInLog(dumpLogTag);
        }
        Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
    }

}