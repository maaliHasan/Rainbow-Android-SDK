package com.ale.infra.manager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ale.infra.application.IApplicationData;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.contact.EmailAddress;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.invitation.CompanyContact;
import com.ale.infra.invitation.CompanyInvitation;
import com.ale.infra.invitation.CompanyJoinRequest;
import com.ale.infra.invitation.Invitation;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.avatar.IAvatarProxy;
import com.ale.infra.proxy.company.ICompanyProxy;
import com.ale.infra.proxy.users.IUserProxy;
import com.ale.util.DateTimeUtil;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by trunk1 on 01/12/2016.
 */

public class CompanyInvitationMgr implements ICompanyInvitationMgr {
    private static final String LOG_TAG = CompanyInvitationMgr.class.getSimpleName();

    private static final String CACHE_FILE_PREFIX_AVATAR = "Company_Avatar_";
    private static final String CACHE_FILE_PREFIX_BANNER = "Company_Banner_";

    private final IContactCacheMgr m_contactCacheMgr;
    private final IPlatformServices m_platformServices;
    private IUserProxy m_usersProxy;
    private ArrayItemList<CompanyInvitation> m_receivedInvitationList;
    private ArrayItemList<CompanyJoinRequest> m_companyJoinRequestList;
    private ICompanyProxy m_companyProxy;
    private IAvatarProxy m_avatarProxy;
    private ArrayItemList<CompanyContact> m_companySearchResult;

    public CompanyInvitationMgr(IContactCacheMgr contactCacheMgr, IPlatformServices platformServices, IUserProxy userProxy, ICompanyProxy companyProxy, IAvatarProxy avatarProxy) {
        m_contactCacheMgr = contactCacheMgr;
        m_platformServices = platformServices;
        m_usersProxy = userProxy;
        m_companyProxy = companyProxy;
        m_avatarProxy = avatarProxy;

        m_receivedInvitationList = new ArrayItemList<>();
        m_companyJoinRequestList = new ArrayItemList<>();
        m_companySearchResult = new ArrayItemList<>();
    }

    @Override
    public void refreshReceivedCompanyInvitationList() {
        Log.getLogger().verbose(LOG_TAG, ">refreshReceivedInvitationList");

        m_usersProxy.getReceivedCompanyInvitations(m_platformServices.getApplicationData().getUserId(), new IUserProxy.IGetCompanyInvitationsListener() {
            @Override
            public void onSuccess(final List<CompanyInvitation> invitationList) {
                setReceivedCompanyInvitationList(invitationList);
            }

            @Override
            public void onFailure(RainbowServiceException exception) {
                Log.getLogger().warn(LOG_TAG, "> refreshReceivedInvitationList received invitation onFailure");
            }
        });
    }

    @Override
    public void refreshJoinCompanyRequestList() {
        Log.getLogger().debug(LOG_TAG, ">refreshJoinCompanyRequestList");

        Thread myThread = new Thread() {
            public void run() {
                m_usersProxy.getJoinCompanyRequests(m_platformServices.getApplicationData().getUserId(), new IUserProxy.IGetCompanyJoinRequestListener() {
                    @Override
                    public void onSuccess(final List<CompanyJoinRequest> requestList) {
                        setJoinCompanyRequestList(requestList);
                    }

                    @Override
                    public void onFailure(RainbowServiceException exception) {
                        Log.getLogger().warn(LOG_TAG, "> refreshJoinCompanyRequestList onFailure");
                    }
                });
            }
        };
        myThread.start();
    }

    @Override
    public void acceptCompanyInvitation(final CompanyInvitation invitation) {
        Log.getLogger().verbose(LOG_TAG, ">acceptInvitation");

        if (invitation == null)
            return;

        Thread myThread = new Thread() {
            public void run() {
                m_usersProxy.postAcceptCompanyInvitation(m_platformServices.getApplicationData().getUserId(), invitation.getId(), new IUserProxy.IGetCompanyInvitationsListener() {
                    @Override
                    public void onSuccess(final List<CompanyInvitation> invitationList) {
        //                Log.getLogger().verbose(LOG_TAG, ">acceptInvitation invitation Success :");
                        refreshReceivedCompanyInvitationList();
                    }

                    @Override
                    public void onFailure(RainbowServiceException exception) {
                        Log.getLogger().warn(LOG_TAG, "> acceptInvitation invitation onFailure");
                    }
                });
            }
        };
        myThread.start();
    }

    @Override
    public void declineCompanyInvitation(final CompanyInvitation invitation) {
        Log.getLogger().verbose(LOG_TAG, ">declineInvitation");

        if (invitation == null)
            return;

        Thread myThread = new Thread() {
            public void run() {
                m_usersProxy.postDeclineCompanyInvitation(m_platformServices.getApplicationData().getUserId(), invitation.getId(), new IUserProxy.IGetCompanyInvitationsListener() {
                    @Override
                    public void onSuccess(final List<CompanyInvitation> invitationList) {
        //               Log.getLogger().verbose(LOG_TAG, ">declineInvitation invitation Success :");
                        refreshReceivedCompanyInvitationList();
                    }

                    @Override
                    public void onFailure(RainbowServiceException exception) {
                        Log.getLogger().warn(LOG_TAG, "> declineInvitation invitation onFailure");
                    }
                });
            }
        };
        myThread.start();
    }

    @Override
    public void cancelJoinCompanyRequest(final CompanyJoinRequest companyJoinRequest, final IUserProxy.IGetCompanyJoinRequestListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">cancelJoinCompanyRequest");

        if (companyJoinRequest == null || companyJoinRequest.getId() == null) {
            Log.getLogger().verbose(LOG_TAG, ">cancelJoinCompanyRequest no companyJoinRequest ");
            refreshJoinCompanyRequestList();
            if (listener != null)
                listener.onFailure(new RainbowServiceException("cancelJoinCompanyRequest companyRequest is null"));
            return;
        }

        companyJoinRequest.setClickActionInProgress(true);//display progressBar
        Thread myThread = new Thread() {
            public void run() {
                m_usersProxy.cancelJoinCompanyRequest(m_platformServices.getApplicationData().getUserId(), companyJoinRequest.getId(), new IUserProxy.IGetCompanyJoinRequestListener() {
                    @Override
                    public void onSuccess(List<CompanyJoinRequest> requestList) {
                        refreshJoinCompanyRequestList();
                        companyJoinRequest.setClickActionInProgress(false);//hide progressBar
                        if (listener != null)
                            listener.onSuccess(requestList);
                    }

                    @Override
                    public void onFailure(RainbowServiceException exception) {
                        Log.getLogger().warn(LOG_TAG, "> cancelJoinCompanyRequest onFailure");
                        refreshJoinCompanyRequestList();
                        companyJoinRequest.setClickActionInProgress(false);//hide progressBar
                        if (listener != null)
                            listener.onFailure(exception);
                    }
                });
            }
        };
        myThread.start();

    }

    @Override
    public void createJoinCompanyRequest(final CompanyContact company, final IUserProxy.IGetCompanyJoinRequestListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">createJoinCompanyRequest");

        if (company == null) {
            Log.getLogger().warn(LOG_TAG, ">createJoinCompanyRequest no company selected ");
            if (listener != null)
                listener.onFailure(new RainbowServiceException("createJoinCompanyRequest companyRequest is null"));
            return;
        }

        if(!m_contactCacheMgr.getUser().getDirectoryContact().isInDefaultCompany()) {
            Log.getLogger().warn(LOG_TAG, ">createJoinCompanyRequest user not in Default company");
            if (listener != null)
                listener.onFailure(new RainbowServiceException("createJoinCompanyRequest user not in Default company cannot change"));
            return;
        }

        if( !m_contactCacheMgr.getUser().isOnlyUser() ) {
            Log.getLogger().warn(LOG_TAG, ">createJoinCompanyRequest user has wrong role (other than user)");
            if (listener != null)
                listener.onFailure(new RainbowServiceException("createJoinCompanyRequest user has wrong role (other than user) cannot change"));
            return;
        }

        company.setClickActionInProgress(true);//display progressBar
        Thread myThread = new Thread() {
            public void run() {
                CompanyJoinRequest joinRequest = findPendingCompanyJoinRequestByCompany(company);
                if ( joinRequest == null ){
                    Log.getLogger().verbose(LOG_TAG, ">createJoinCompanyRequest send");
                    m_usersProxy.sendJoinCompanyRequest(m_platformServices.getApplicationData().getUserId(), company.getId() , null, null, new IUserProxy.IGetCompanyJoinRequestListener() {
                        @Override
                        public void onSuccess(List<CompanyJoinRequest> requestList) {
                            if (listener != null)
                                listener.onSuccess(requestList);
                            refreshJoinCompanyRequestList();
                            company.setClickActionInProgress(false);//hide progressBar
                        }

                        @Override
                        public synchronized void onFailure(RainbowServiceException exception) {
                            if (listener != null)
                                listener.onFailure(exception);
                            company.setClickActionInProgress(false);//hide progressBar
                        }
                    });
                } else {
                    Log.getLogger().verbose(LOG_TAG, ">createJoinCompanyRequest re-invite");
                    resendJoinCompanyRequest(joinRequest, new IUserProxy.IGetCompanyJoinRequestListener() {
                        @Override
                        public void onSuccess(List<CompanyJoinRequest> requestList) {
                            if (listener != null)
                                listener.onSuccess(requestList);
                            company.setClickActionInProgress(false);//hide progressBar
                        }

                        @Override
                        public synchronized void onFailure(RainbowServiceException exception) {
                            if (listener != null)
                                listener.onFailure(exception);
                            company.setClickActionInProgress(false);//hide progressBar
                        }
                    });
                }
            }
        };
        myThread.start();

    }

    @Override
    public void resendJoinCompanyRequest(final CompanyJoinRequest joinRequest, final IUserProxy.IGetCompanyJoinRequestListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">resendJoinCompanyRequest");

        if (joinRequest == null) {
            Log.getLogger().verbose(LOG_TAG, ">resendJoinCompanyRequest no joinRequest selected ");
            if (listener != null)
                listener.onFailure(new RainbowServiceException("resendJoinCompanyRequest joinRequest is null"));
            return;
        }

        joinRequest.setClickActionInProgress(true);//display progressBar
        Thread myThread = new Thread() {
            public void run() {
                Log.getLogger().verbose(LOG_TAG, ">resendJoinCompanyRequest re-invite");
                m_usersProxy.resendJoinCompanyRequest(m_platformServices.getApplicationData().getUserId(), joinRequest.getId(), new IUserProxy.IGetCompanyJoinRequestListener() {
                    @Override
                    public void onSuccess(List<CompanyJoinRequest> requestList) {
                        if (listener != null)
                            listener.onSuccess(requestList);
                        joinRequest.setClickActionInProgress(false);//hide progressBar
                    }

                    @Override
                    public synchronized void onFailure(RainbowServiceException exception) {
                        if (listener != null)
                            listener.onFailure(exception);
                        joinRequest.setClickActionInProgress(false);//hide progressBar
                    }
                });
            }
        };
        myThread.start();

    }

    @Override
    public ArrayItemList<CompanyInvitation> getReceivedCompanyInvitationList() {
        return m_receivedInvitationList;
    }

    @Override
    public ArrayItemList<CompanyJoinRequest> getCompanyJoinRequestList() {
        return m_companyJoinRequestList;
    }

    private void getCompanyInvitationContactInfo(final CompanyInvitation invitation) {
        IApplicationData appliData = m_platformServices.getApplicationData();
        if (!appliData.getUserId().equalsIgnoreCase(invitation.getInvitingUserId())) {
            //set inviting contact
            Contact contact = m_contactCacheMgr.getContactFromEmail(invitation.getInvitingUserEmail());
            if (contact == null) {
                DirectoryContact dirContact = new DirectoryContact();
                dirContact.addEmailAddress(invitation.getInvitingUserEmail(), EmailAddress.EmailType.OTHER);

                contact = m_contactCacheMgr.createContactIfNotExistOrUpdate(dirContact);
            }
            invitation.setInvitingContact(contact);

            final Contact finalContact = contact;
            m_usersProxy.getUserData(invitation.getInvitingUserId(), new IUserProxy.IGetUserDataListener() {
                @Override
                public void onSuccess(Contact foundUserContact) {
                    if (foundUserContact != null) {
                        invitation.setInvitingContact(foundUserContact);
                        // update existing contact in contactCacheMgr
                        m_contactCacheMgr.setDirectoryContact(finalContact, foundUserContact.getDirectoryContact());

                        m_receivedInvitationList.fireDataChanged();
                    }
                }

                @Override
                public void onFailure(RainbowServiceException exception) {
                    Log.getLogger().warn(LOG_TAG, ">getInvitationContactInfo inviting onFailure");
                    m_receivedInvitationList.fireDataChanged();
                }
            });
        } else {
            //inviting contact is me
            invitation.setInvitingContact(m_contactCacheMgr.getUser());
            m_receivedInvitationList.fireDataChanged();
        }

        if (!appliData.getUserId().equalsIgnoreCase(invitation.getInvitedUserId())) {
            //set invited contact
            Contact contact = m_contactCacheMgr.getContactFromEmail(invitation.getInvitedUserEmail());

            if (contact == null) {
                DirectoryContact dirContact = new DirectoryContact();
                dirContact.addEmailAddress(invitation.getInvitedUserEmail(), EmailAddress.EmailType.OTHER);

                contact = m_contactCacheMgr.createContactIfNotExistOrUpdate(dirContact);
            }
            invitation.setInvitedContact(contact);

            if (!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(invitation.getInvitedUserId())) {
                m_usersProxy.getUserData(invitation.getInvitedUserId(), new IUserProxy.IGetUserDataListener() {
                    @Override
                    public void onSuccess(Contact foundUserContact) {
                        if (foundUserContact != null) {
                            invitation.setInvitedContact(foundUserContact);
                            m_receivedInvitationList.fireDataChanged();
                        }
                    }

                    @Override
                    public void onFailure(RainbowServiceException exception) {
                        Log.getLogger().warn(LOG_TAG, ">getInvitationContactInfo invited onFailure");
                        m_receivedInvitationList.fireDataChanged();
                    }
                });
            }
        } else {//inviting contact is me
            invitation.setInvitedContact(m_contactCacheMgr.getUser());
            m_receivedInvitationList.fireDataChanged();
        }
    }

    private synchronized void setReceivedCompanyInvitationList(List<CompanyInvitation> invitationList) {
        m_receivedInvitationList.clear();
        m_receivedInvitationList.addAll(invitationList);
        for (CompanyInvitation invitation : invitationList) {
            getCompanyInvitationContactInfo(invitation);
        }
    }

    private synchronized void setJoinCompanyRequestList(List<CompanyJoinRequest> requestList) {
        m_companyJoinRequestList.clear();
        m_companyJoinRequestList.addAll(requestList);
        for (CompanyJoinRequest request : requestList) {
            getCompanyInfo(request);
        }
    }

    private synchronized void getCompanyInfo(final CompanyJoinRequest request) {
        if (request == null)
            return;

        Thread myThread = new Thread() {
            public void run() {
                m_companyProxy.getCompany(request.getRequestedCompanyId(), new ICompanyProxy.IGetCompanyDataListener() {
                    @Override
                    public void onSuccess(List<CompanyContact> companyList) {
                        if (companyList.size() > 0) {
                            //update info
                            request.setCompanyContact(companyList.get(0));
                            //search company photo
                            downloadCompanyAvatar(request.getCompanyContact(), false);
                            downloadCompanyBanner(request.getCompanyContact(), false);
                            m_companyJoinRequestList.fireDataChanged();
                        }
                    }

                    @Override
                    public void onFailure(RainbowServiceException exception) {
                        Log.getLogger().warn(LOG_TAG, ">getCompanyInfo onFailure");
                        m_companyJoinRequestList.fireDataChanged();
                    }
                });
            }
        };
        myThread.start();
    }

    private synchronized void getCompanyInfo(final CompanyContact companyContact) {
        if (companyContact == null)
            return;

        Thread myThread = new Thread() {
            public void run() {
                m_companyProxy.getCompany(companyContact.getId(), new ICompanyProxy.IGetCompanyDataListener() {
                    @Override
                    public void onSuccess(List<CompanyContact> companyList) {
                        if (companyList.size() > 0) {
                            //update info
                            companyContact.update(companyList.get(0));
                            //search company photo
                            downloadCompanyAvatar(companyContact, false);
                            downloadCompanyBanner(companyContact, false);
                            m_companySearchResult.fireDataChanged();
                        }
                    }

                    @Override
                    public void onFailure(RainbowServiceException exception) {
                        Log.getLogger().warn(LOG_TAG, ">getCompanyInfo onFailure");
                        m_companySearchResult.fireDataChanged();
                    }
                });
            }
        };
        myThread.start();
    }

    private synchronized void setSearchCompanyResultList(List<CompanyContact> searchResultList) {
        m_companySearchResult.clear();
        m_companySearchResult.addAll(searchResultList);
        for (CompanyContact company : searchResultList) {
            getCompanyInfo(company);
        }
    }

    @Override
    public void searchByName(String name, final ICompanyProxy.IGetCompanyDataListener listener) {

        m_companyProxy.searchByName(name, new ICompanyProxy.IGetCompanyDataListener() {
            @Override
            public void onSuccess(List<CompanyContact> resultList) {
                Log.getLogger().info(LOG_TAG, ">searchByName onSuccess : " + resultList.size());

                setSearchCompanyResultList(resultList);
                if(listener != null)
                    listener.onSuccess(resultList);
            }

            @Override
            public void onFailure(RainbowServiceException exception) {
                Log.getLogger().warn(LOG_TAG, ">searchByName onFailure");
                if(listener != null)
                    listener.onFailure(exception);
            }
        });
    }

    @Override
    public List<CompanyInvitation> getPendingReceivedCompanyInvitationList() {
        List<CompanyInvitation> pendingInvitationList = new ArrayList<>();

        for (CompanyInvitation invitation : m_receivedInvitationList.getCopyOfDataList()) {
            if (Invitation.InvitationStatus.PENDING == invitation.getStatus()) {
                pendingInvitationList.add(invitation);
            }
        }
        return pendingInvitationList;
    }

    @Override
    public List<CompanyJoinRequest> getPendingCompanyJoinRequestList() {
        List<CompanyJoinRequest> pendingRequestList = new ArrayList<>();

        for (CompanyJoinRequest request : m_companyJoinRequestList.getCopyOfDataList()) {
            if (CompanyJoinRequest.CompanyJoinRequestStatus.PENDING == request.getStatus()) {
                pendingRequestList.add(request);
            }
        }
        return pendingRequestList;
    }


    @Override
    public CompanyJoinRequest findPendingCompanyJoinRequestByCompany(CompanyContact company) {
        for (CompanyJoinRequest request : m_companyJoinRequestList.getCopyOfDataList()){
            if ( request.getRequestedCompanyId().equalsIgnoreCase(company.getId()) && CompanyJoinRequest.CompanyJoinRequestStatus.PENDING == request.getStatus() ){
                return request;
            }
        }
        return null;
    }

    @Override
    public CompanyContact findCompanyContactById(String companyId) {
        for (CompanyContact companyContact : m_companySearchResult.getCopyOfDataList()){
            if ( companyContact.getId().equalsIgnoreCase(companyId) ){
                return companyContact;
            }
        }
        return null;
    }

    @Override
    public CompanyInvitation findReceivedCompanyInvitationWithCompanyInvitationId(String companyInvitationId) {
        if (StringsUtil.isNullOrEmpty(companyInvitationId)) return null;

        for (CompanyInvitation companyInvitation : m_receivedInvitationList.getCopyOfDataList()) {
            if ( companyInvitation.getId().equalsIgnoreCase(companyInvitationId)){
                return companyInvitation;
            }
        }
        return null;
    }


    public void downloadCompanyAvatar(final CompanyContact companyContact, boolean forceReload) {
        if( companyContact == null ) {
            Log.getLogger().warn(LOG_TAG, ">downloadCompanyAvatar; companyContact is null - skip");
            return;
        }
        //search photo on server side
        // 288 = 96*3

        if(!forceReload) {
            Log.getLogger().verbose(LOG_TAG, "Check if photo needs to be downloaded");
            if ( companyContact.getPhoto() != null ) {
                Log.getLogger().verbose(LOG_TAG, "Company has already a Photo");
                return;
            }
        }

        //float scale = m_context.getResources().getDisplayMetrics().density;

        final String simpleCacheFileName = CACHE_FILE_PREFIX_AVATAR + companyContact.getId();
        File photoFromCache = null;
        if (m_contactCacheMgr != null)
            photoFromCache = m_contactCacheMgr.getContactsCacheFile().findFileStartingBy(simpleCacheFileName);

        if( photoFromCache != null ) {
            Log.getLogger().verbose(LOG_TAG, "Photo From Cache detected for " + simpleCacheFileName);
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(photoFromCache), null, options);

                if( m_contactCacheMgr.checkPhotoCacheDateValidity(photoFromCache.getName(), DateTimeUtil.getStringStampFromDate(companyContact.getLastAvatarUpdateDate())) ) {
                    Log.getLogger().verbose(LOG_TAG, "Using Cache photo for "+ companyContact.getName());
                    companyContact.setPhoto(bitmap);
                    return;
                } else {
                    Log.getLogger().verbose(LOG_TAG, "Cached Photo of "+ companyContact.getName() +" is obsolete / delete it");
                    m_contactCacheMgr.getContactsCacheFile().deleteFile(photoFromCache.getName());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        Thread myThread = new Thread() {
            public void run() {
                if ( m_avatarProxy != null && companyContact.getLastAvatarUpdateDate() != null ) {
                    m_avatarProxy.getAvatar(companyContact.getId(), companyContact.getLastAvatarUpdateDateString(), 288, new IAvatarProxy.IAvatarListener() {
                        @Override
                        public void onAvatarSuccess(Bitmap bmp) {
                            Log.getLogger().verbose(LOG_TAG, ">onAvatarSuccess");
                            companyContact.setPhoto(bmp);

                            StringBuilder fileName = new StringBuilder();
                            fileName.append(CACHE_FILE_PREFIX_AVATAR + companyContact.getId());
                            fileName.append("_");
                            fileName.append(DateTimeUtil.getStringStampFromDate(companyContact.getLastAvatarUpdateDate()));
                            Log.getLogger().info(LOG_TAG, "company_ ImageName; " + fileName.toString());

                            if (m_contactCacheMgr != null)
                                m_contactCacheMgr.getContactsCacheFile().save(fileName.toString(), bmp);
                        }

                        @Override
                        public void onAvatarFailure() {
                            Log.getLogger().warn(LOG_TAG, ">onAvatarFailure; " + companyContact.getName());
                            companyContact.notifyCompanyContactUpdated();
                        }
                    });
                }
            }
        };
        myThread.start();
    }

    public void downloadCompanyBanner(final CompanyContact companyContact, boolean forceReload) {
        if( companyContact == null ) {
            Log.getLogger().warn(LOG_TAG, ">downloadCompanyBanner; companyContact is null - skip");
            return;
        }
        //search photo on server side
        // 288 = 96*3

        if(!forceReload) {
            Log.getLogger().verbose(LOG_TAG, "Check if banner needs to be downloaded");
            if ( companyContact.getBanner() != null ) {
                Log.getLogger().verbose(LOG_TAG, "Company has already a Banner");
                return;
            }
        }

        //float scale = m_context.getResources().getDisplayMetrics().density;

        final String simpleCacheFileName = CACHE_FILE_PREFIX_BANNER + companyContact.getId();
        File photoFromCache = null;
        if (m_contactCacheMgr != null)
            photoFromCache = m_contactCacheMgr.getContactsCacheFile().findFileStartingBy(simpleCacheFileName);

        if( photoFromCache != null ) {
            Log.getLogger().verbose(LOG_TAG, "Photo From Cache detected for " + simpleCacheFileName);
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(photoFromCache), null, options);

                if( m_contactCacheMgr.checkPhotoCacheDateValidity(photoFromCache.getName(),  DateTimeUtil.getStringStampFromDate(companyContact.getLastBannerUpdateDate())) ) {
                    Log.getLogger().verbose(LOG_TAG, "Using Cache photo for "+ companyContact.getName());
                    companyContact.setBanner(bitmap);
                    return;
                } else {
                    Log.getLogger().verbose(LOG_TAG, "Cached Photo of "+ companyContact.getName() +" is obsolete / delete it");
                    m_contactCacheMgr.getContactsCacheFile().deleteFile(photoFromCache.getName());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        Thread myThread = new Thread() {
            public void run() {
                if (companyContact.getLastBannerUpdateDate() != null) {
                    m_companyProxy.getCompanyBanner(companyContact.getId(), 288, new IAvatarProxy.IAvatarListener() {
                        @Override
                        public void onAvatarSuccess(Bitmap bmp) {
                            Log.getLogger().verbose(LOG_TAG, ">onAvatarSuccess");
                            companyContact.setBanner(bmp);

                            StringBuilder fileName = new StringBuilder();
                            fileName.append(CACHE_FILE_PREFIX_BANNER + companyContact.getId());
                            fileName.append("_");
                            fileName.append(DateTimeUtil.getStringStampFromDate(companyContact.getLastBannerUpdateDate()));
                            Log.getLogger().info(LOG_TAG, "company_ ImageName; " + fileName.toString());

                            if (m_contactCacheMgr != null)
                                m_contactCacheMgr.getContactsCacheFile().save(fileName.toString(), bmp);

                        }

                        @Override
                        public void onAvatarFailure() {
                            Log.getLogger().warn(LOG_TAG, ">onAvatarFailure; " + companyContact.getName());
                            companyContact.notifyCompanyContactUpdated();
                        }
                    });
                }
            }
        };
        myThread.start();
    }
    //CT : TODO : clear avatar and banner cache
}
