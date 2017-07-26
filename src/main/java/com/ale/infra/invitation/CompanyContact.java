package com.ale.infra.invitation;

import android.graphics.Bitmap;

import com.ale.infra.data_model.IMultiSelectable;
import com.ale.infra.searcher.IDisplayable;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by trunk1 on 13/02/2017.
 */

public class CompanyContact implements IMultiSelectable, IDisplayable {
    private static final String LOG_TAG = CompanyInvitation.class.getSimpleName();

    private Set<ICompanyContactListener> m_changeListeners = new HashSet<>();

    private String m_id;
    private String m_name;
    private String m_description;
    private CompanyContactOfferType m_offerType = CompanyContactOfferType.UNDEFINED;
    private CompanyContactStatus m_status = CompanyContactStatus.UNDEFINED;
    private String m_webSite;
    private String m_country;
    private String m_organisationId;
    private CompanyContactVisibility m_visibility = CompanyContactVisibility.UNDEFINED;
    private List<String> m_visibleBy;
    private List<String> m_visibilityRequests;
    private boolean m_forceHandshake;
    private String m_adminEmail;
    private String m_supportEmail;
    private int m_numberUsers;
    private String m_companySize;
    private Date m_creationDate;
    private Date m_statusUpdateDate;
    private boolean m_clickActionInProgress = false;
    private Bitmap m_photo;
    private Bitmap m_banner;
    private Date m_lastAvatarUpdateDate;
    private Date m_lastBannerUpdateDate;
    private boolean hasNoAvatarOnServer = false;



    private String m_lastAvatarUpdateDateString;
    private String m_slogan;

    public enum CompanyContactStatus
    {
        ACTIVE("active"), ALERTING("alerting"), HOLD("hold"), TERMINATED("terminated"), UNDEFINED("undefined");
        private String value;

        CompanyContactStatus(String value) {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return value;
        }

        public static CompanyContactStatus fromString(String text) {
            if (text != null) {
                for (CompanyContactStatus status : CompanyContactStatus.values()) {
                    if (text.equalsIgnoreCase(status.value)) {
                        return status;
                    }
                }
            }
            return UNDEFINED;
        }
    }
    public enum CompanyContactOfferType
    {

        FREEMIUM("freemium"), PREMIUM("premium"), UNDEFINED("undefined");
        private String value;

        CompanyContactOfferType(String value) {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return value;
        }

        public static CompanyContactOfferType fromString(String text) {
            if (text != null) {
                for (CompanyContactOfferType status : CompanyContactOfferType.values()) {
                    if (text.equalsIgnoreCase(status.value)) {
                        return status;
                    }
                }
            }
            return UNDEFINED;
        }
    }
    public enum CompanyContactVisibility
    {

        PUBLIC("public"), PRIVATE("private"), ORGANISATION("organisation"), UNDEFINED("undefined");
        private String value;

        CompanyContactVisibility(String value) {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return value;
        }

        public static CompanyContactVisibility fromString(String text) {
            if (text != null) {
                for (CompanyContactVisibility status : CompanyContactVisibility.values()) {
                    if (text.equalsIgnoreCase(status.value)) {
                        return status;
                    }
                }
            }
            return UNDEFINED;
        }
    }

    public CompanyContact() {
    }

    public CompanyContact(String companyId) {
        m_id = companyId;
    }

    public String getId() {
        return m_id;
    }

    public void setId(String m_id) {
        this.m_id = m_id;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String companyName) {
        this.m_name = companyName;
    }

    public String getDescription() {
        return m_description;
    }

    public void setDescription(String description) {
        this.m_description = description;
    }

    public CompanyContactOfferType getOfferType() {
        return m_offerType;
    }

    public void setOfferType(CompanyContactOfferType offerType) {
        this.m_offerType = offerType;
    }

    public CompanyContactStatus getStatus() {
        return m_status;
    }

    public void setStatus(CompanyContactStatus status) {
        this.m_status = status;
    }

    public String getWebSite() {
        return m_webSite;
    }

    public void setWebSite(String webSite) {
        this.m_webSite = webSite;
    }

    public String getCountry() {
        return m_country;
    }

    public void setCountry(String country) {
        this.m_country = country;
    }

    public String getOrganisationId() {
        return m_organisationId;
    }

    public void setOrganisationId(String organisationId) {
        this.m_organisationId = organisationId;
    }

    public CompanyContactVisibility getVisibility() {
        return m_visibility;
    }

    public void setVisibility(CompanyContactVisibility visibility) {
        this.m_visibility = visibility;
    }

    public List<String> getVisibleBy() {
        return m_visibleBy;
    }

    public void setVisibleBy(List<String> visibleBy) {
        this.m_visibleBy = visibleBy;
    }

    public boolean getForceHandshake() {
        return m_forceHandshake;
    }

    public void setForceHandshake(boolean forceHandshake) {
        this.m_forceHandshake = forceHandshake;
    }

    public String getAdminEmail() {
        return m_adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.m_adminEmail = adminEmail;
    }

    public String getSupportEmail() {
        return m_supportEmail;
    }

    public void setSupportEmail(String supportEmail) {
        this.m_supportEmail = supportEmail;
    }

    public int getNumberUsers() {
        return m_numberUsers;
    }

    public void setNumberUsers(int numberUsers) {
        this.m_numberUsers = numberUsers;
    }

    public String getCompanySize() {
        return m_companySize;
    }

    public void setCompanySize(String companySize) {
        this.m_companySize = companySize;
    }

    public List<String> getVisibilityRequests() {
        return m_visibilityRequests;
    }

    public void setVisibilityRequests(List<String> visibilityRequests) {
        this.m_visibilityRequests = visibilityRequests;
    }

    public Date getCreationDate() {
        return m_creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.m_creationDate = creationDate;
    }

    public Date getStatusUpdateDate() {
        return m_statusUpdateDate;
    }

    public void setStatusUpdateDate(Date statusUpdateDate) {
        this.m_statusUpdateDate = statusUpdateDate;
    }

    public boolean isClickActionInProgress() {
        return m_clickActionInProgress;
    }

    public void setClickActionInProgress(boolean clickActionInProgress) {
        this.m_clickActionInProgress = clickActionInProgress;
    }

    public Bitmap getPhoto() {
        return m_photo;
    }

    public void setPhoto(Bitmap photo) {
        this.m_photo = photo;
        notifyCompanyContactUpdated();
    }

    public Date getLastAvatarUpdateDate() {
        return m_lastAvatarUpdateDate;
    }

    public String getLastAvatarUpdateDateToString() {
        return m_lastAvatarUpdateDate == null ? "" : m_lastAvatarUpdateDate.toString();
    }

    public void setLastAvatarUpdateDate(Date lastAvatarUpdateDate) {
        m_lastAvatarUpdateDate = lastAvatarUpdateDate;
    }

    public Bitmap getBanner() {
        return m_banner;
    }

    public void setBanner(Bitmap banner) {
        this.m_banner = banner;
        notifyCompanyContactUpdated();
    }

    public Date getLastBannerUpdateDate() {
        return m_lastBannerUpdateDate;
    }

    public String getLastBannerUpdateDateToString() {
        return m_lastAvatarUpdateDate == null ? "" : m_lastBannerUpdateDate.toString();
    }

    public void setLastBannerUpdateDate(Date lastBannerUpdateDate) {
        m_lastBannerUpdateDate = lastBannerUpdateDate;
    }

    public String getSlogan() {
        return m_slogan;
    }

    public void setSlogan(String slogan) {
        this.m_slogan = slogan;
    }

    public synchronized void update(CompanyContact updatedCompanyContact)
    {
        m_id = updatedCompanyContact.getId();
        m_name = updatedCompanyContact.getName();
        m_description = updatedCompanyContact.getDescription();
        m_offerType = updatedCompanyContact.getOfferType();
        m_status = updatedCompanyContact.getStatus();
        m_webSite = updatedCompanyContact.getWebSite();
        m_country = updatedCompanyContact.getCountry();
        m_organisationId = updatedCompanyContact.getOrganisationId();
        m_visibility = updatedCompanyContact.getVisibility();
        m_visibleBy = updatedCompanyContact.getVisibleBy();
        m_visibilityRequests = updatedCompanyContact.getVisibilityRequests();
        m_forceHandshake = updatedCompanyContact.getForceHandshake();
        m_adminEmail = updatedCompanyContact.getAdminEmail();
        m_supportEmail = updatedCompanyContact.getSupportEmail();
        m_numberUsers = updatedCompanyContact.getNumberUsers();
        m_companySize = updatedCompanyContact.getCompanySize();
        m_creationDate = updatedCompanyContact.getCreationDate();
        m_statusUpdateDate = updatedCompanyContact.getStatusUpdateDate();
        m_clickActionInProgress = updatedCompanyContact.isClickActionInProgress();

        notifyCompanyContactUpdated();
    }

    public synchronized void notifyCompanyContactUpdated()
    {
        synchronized (m_changeListeners)
        {
            for (ICompanyContactListener listener : m_changeListeners)
            {
                listener.companyContactUpdated(this);
            }
        }
    }

    public void registerChangeListener(ICompanyContactListener changeListener) {
        synchronized (m_changeListeners) {
            m_changeListeners.add(changeListener);
        }
    }

    public void unregisterChangeListener(ICompanyContactListener changeListener) {
        synchronized (m_changeListeners) {
            m_changeListeners.remove(changeListener);
        }
    }

    @Override
    public int getSelectableType() {
        return 0;
    }

    @Override
    public String getDisplayName(String unknownNameString) {
        if( !StringsUtil.isNullOrEmpty(getName()) )
            return getName();
        return unknownNameString;
    }

    public void dumpInLog(String dumpLogTag) {
        Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
        Log.getLogger().info(dumpLogTag, "    Invitation:");
        if( m_id != null ) {
            Log.getLogger().info(dumpLogTag, "    id=" + m_id);
        } else {
            Log.getLogger().info(dumpLogTag, "    id=null");
        }

        if( m_name != null ) {
            Log.getLogger().info(dumpLogTag, "    name=" + m_name);
        } else {
            Log.getLogger().info(dumpLogTag, "    name=null");
        }

        if( m_description != null ) {
            Log.getLogger().info(dumpLogTag, "    description=" + m_description);
        } else {
            Log.getLogger().info(dumpLogTag, "    description=null");
        }

        if( m_offerType != null ) {
            Log.getLogger().info(dumpLogTag, "    offerType=" + m_offerType);
        } else {
            Log.getLogger().info(dumpLogTag, "    offerType=null");
        }

        Log.getLogger().info(dumpLogTag, "    status=" + m_status);

        if( m_webSite != null ) {
            Log.getLogger().info(dumpLogTag, "    webSite=" + m_webSite);
        } else {
            Log.getLogger().info(dumpLogTag, "    webSite=null");
        }

        if( m_country != null ) {
            Log.getLogger().info(dumpLogTag, "    country=" + m_country);
        } else {
            Log.getLogger().info(dumpLogTag, "    country=null");
        }

        if( m_organisationId != null ) {
            Log.getLogger().info(dumpLogTag, "    organisationId=" + m_organisationId);
        } else {
            Log.getLogger().info(dumpLogTag, "    organisationId=null");
        }

        if( m_visibility != null ) {
            Log.getLogger().info(dumpLogTag, "    visibility=" + m_visibility);
        } else {
            Log.getLogger().info(dumpLogTag, "    visibility=null");
        }

        Log.getLogger().info(dumpLogTag, "    forceHandshake=" + String.valueOf(m_forceHandshake));
        if( m_adminEmail != null ) {
            Log.getLogger().info(dumpLogTag, "    adminEmail=" + m_adminEmail);
        } else {
            Log.getLogger().info(dumpLogTag, "    adminEmail=null");
        }

        if( m_supportEmail != null ) {
            Log.getLogger().info(dumpLogTag, "    supportEmail=" + m_supportEmail);
        } else {
            Log.getLogger().info(dumpLogTag, "    supportEmail=null");
        }
        Log.getLogger().info(dumpLogTag, "    numberUsers=" + String.valueOf(m_numberUsers));
        Log.getLogger().info(dumpLogTag, "    companySize=" + String.valueOf(m_companySize));

        if( m_creationDate != null ) {
            Log.getLogger().info(dumpLogTag, "    creationDate=" + m_creationDate.toString());
        } else {
            Log.getLogger().info(dumpLogTag, "    creationDate=null");
        }

        if( m_statusUpdateDate != null ) {
            Log.getLogger().info(dumpLogTag, "    statusUpdateDate=" + m_statusUpdateDate.toString());
        } else {
            Log.getLogger().info(dumpLogTag, "    statusUpdateDate=null");
        }

        if( m_lastAvatarUpdateDate != null ) {
            Log.getLogger().info(dumpLogTag, "    lastAvatarUpdateDate=" + m_lastAvatarUpdateDate.toString());
        } else {
            Log.getLogger().info(dumpLogTag, "    lastAvatarUpdateDate=null");
        }

        if( m_lastBannerUpdateDate != null ) {
            Log.getLogger().info(dumpLogTag, "    lastBannerUpdateDate=" + m_lastBannerUpdateDate.toString());
        } else {
            Log.getLogger().info(dumpLogTag, "    lastBannerUpdateDate=null");
        }

        Log.getLogger().info(dumpLogTag, "    clickActionInProgress=" + String.valueOf(m_clickActionInProgress));
        Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
    }


    public String getLastAvatarUpdateDateString() {
        return m_lastAvatarUpdateDateString;
    }

    public void seLastAvatarUpdateDateString(String m_lastAvatarUpdateDateString) {
        this.m_lastAvatarUpdateDateString = m_lastAvatarUpdateDateString;
    }

    public boolean hasNoAvatarOnServer() {
        return hasNoAvatarOnServer;
    }

    public void sethasNoAvatarOnServer(boolean hasNoAvatarOnServer) {
        this.hasNoAvatarOnServer = hasNoAvatarOnServer;
    }

}
