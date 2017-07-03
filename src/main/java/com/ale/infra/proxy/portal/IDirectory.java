package com.ale.infra.proxy.portal;

import com.ale.infra.contact.DirectoryContact;

import java.util.List;

/**
 * Created by grobert on 09/11/15.
 */
public interface IDirectory {
    /**
     * UDA searchByName
     * @param input
     */
    void searchByName(String input, IDirectoryListener listener);

    void searchByMail(String input, IDirectoryListener listener);

    void searchByMails(List<String> emails, final IDirectory.IDirectoryListener listener);

    void searchByJid(String jid, IDirectoryListener listener);

    void searchByJids(List<String> jids, IDirectoryListener listener);

    void abortSearch();

    interface IDirectoryListener
    {
        void onCorporateSearchSuccess(List<DirectoryContact> searchResults);

        void onFailure();
    }
}
