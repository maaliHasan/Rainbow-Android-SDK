package com.ale.infra.proxy.directory;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.contact.IContact;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.rainbow.api.IDirectoryService;
import com.ale.infra.rainbow.api.IServicesFactory;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Directory proxy class
 */
public class DirectoryProxy implements IDirectoryProxy {
    private static final String LOG_TAG = "DirectoryProxy";
    public static final int SEARCHJID_LIMIT = 100;

    private IDirectoryService m_directoryService;

    public DirectoryProxy(IServicesFactory servicesFactory, IRESTAsyncRequest httpClientFactory, IPlatformServices platformService) {
        Log.getLogger().info(LOG_TAG, "initialization");
        if( servicesFactory != null)
            m_directoryService = servicesFactory.createDirectoryService(httpClientFactory, platformService);
    }

    @Override
    public void searchByName(final String input, final IDirectoryProxy.IDirectoryListener listener) {
        Log.getLogger().verbose(LOG_TAG, "directory searchByName with : " + input);

        if (!isSearchAvailable(listener)) return;

        m_directoryService.searchByName(input, new IAsyncServiceResultCallback<SearchResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<SearchResponse> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "Error while trying to searchByName on directory :" + asyncResult.getException().getMessage());

                    if (listener != null)
                        listener.onFailure();

                    return;
                }

                SearchResponse response = asyncResult.getResult();
                Log.getLogger().verbose(LOG_TAG, "directory searchByName success:" + response.toString());

                if (listener != null)
                    listener.onCorporateSearchSuccess(response.getContacts());
            }
        });
    }

    @Override
    public void searchByMails(final List<String> emails,
                              final IDirectoryProxy.IDirectoryListener listener) {
        Log.getLogger().verbose(LOG_TAG, "directory searchByMail with several emails: " + emails);

        if (!isSearchAvailable(listener)) return;

        m_directoryService.searchByMails(emails, new IAsyncServiceResultCallback<SearchResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<SearchResponse> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "Error while trying to searchByMails on directory :" + asyncResult.getException().getMessage());

                    if (listener != null)
                        listener.onFailure();

                    return;
                }

                SearchResponse response = asyncResult.getResult();
                Log.getLogger().verbose(LOG_TAG, "directory searchByMail success:" + response.toString());

                if (listener != null)
                    listener.onCorporateSearchSuccess(response.getContacts());
            }
        });
    }

    @Override
    public void searchByJid(String userid, String jid, final IDirectoryListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">searchByJid : " + jid);

        if (!isSearchAvailable(listener)) return;

        m_directoryService.searchByJid(userid, jid, new IAsyncServiceResultCallback<SearchResponseByJid>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<SearchResponseByJid> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "Error while trying to searchByJid on directory :" + asyncResult.getException().getMessage());

                    if (listener != null)
                        listener.onFailure();

                    return;
                }

                ArrayItemList<IContact> results = new ArrayItemList<>();

                SearchResponseByJid response = asyncResult.getResult();
                Log.getLogger().verbose(LOG_TAG, "directory searchByJid success:" + response.toString());

                for (IContact currentContact : response.getContacts()) {
                    results.add(currentContact);
                }

                if (listener != null)
                    listener.onCorporateSearchSuccess(response.getContacts());
            }
        });
    }

    @Override
    public void searchByJids(final List<String> jids, final IDirectoryListener listener) {
        Log.getLogger().verbose(LOG_TAG, "directory searchByJids with several jids: " + jids.size());

        if (!isSearchAvailable(listener)) return;

        m_directoryService.searchByJids(jids, new IAsyncServiceResultCallback<SearchResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<SearchResponse> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "Error while trying to searchByJids on directory :" + asyncResult.getException().getMessage());

                    if (listener != null)
                        listener.onFailure();
                } else {
                    SearchResponse response = asyncResult.getResult();
                    Log.getLogger().verbose(LOG_TAG, "directory searchByJids success:" + response.toString());

                    if (listener != null)
                        listener.onCorporateSearchSuccess(response.getContacts());
                }
            }
        });
    }

    public boolean doesListContactContainsJid(List<DirectoryContact> directoryContactList, String curJid) {

        for(DirectoryContact contact : directoryContactList) {
            if( !StringsUtil.isNullOrEmpty(contact.getImJabberId()) && contact.getImJabberId().equals(curJid))
                return true;
        }
        return false;
    }

    private boolean isSearchAvailable(IDirectoryListener listener) {

        if (!RainbowContext.getInfrastructure().isRestConnected()) {

            Log.getLogger().warn(LOG_TAG, "Search not possible Rest not connected ");

            List<DirectoryContact> response = new ArrayList<>();
            if (listener != null)
                listener.onCorporateSearchSuccess(response);
            return false;
        }
        return true;
    }


    @Override
    public void abortSearch() {
        m_directoryService.abortSearch();
    }

    @Override
    public void searchNetwork(int limit, final IDirectoryListener listener) {
        Log.getLogger().verbose(LOG_TAG, "searchNetwork....");

        if (!isSearchAvailable(listener)) return;

        m_directoryService.searchNetwork(limit, new IAsyncServiceResultCallback<SearchResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<SearchResponse> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "Error while trying to searchNetwork on directory :" + asyncResult.getException().getMessage());

                    if (listener != null)
                        listener.onFailure();
                } else {
                    SearchResponse response = asyncResult.getResult();
                    Log.getLogger().verbose(LOG_TAG, "directory searchNetwork success:" + response.toString());

                    if (listener != null)
                        listener.onCorporateSearchSuccess(response.getContacts());
                }
            }
        });
    }

}
