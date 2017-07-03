package com.ale.infra.proxy.directory;

import com.ale.infra.contact.DirectoryContact;

import java.util.List;

/**
 * Created by grobert on 09/11/15.
 */
public interface IDirectoryProxy {
    /**
     * UDA searchByName
     * @param input
     */
    void searchByName(String input, IDirectoryListener listener);

    void searchByMails(List<String> emails, final IDirectoryProxy.IDirectoryListener listener);

    void searchByJid(String userid, String jid, IDirectoryListener listener);

    void searchByJids(List<String> jids, IDirectoryListener listener);

    void abortSearch();

    void searchNetwork(int limit, IDirectoryListener listener);

    interface IDirectoryListener
    {
        void onCorporateSearchSuccess(List<DirectoryContact> searchResults);

        void onFailure();
    }
}
