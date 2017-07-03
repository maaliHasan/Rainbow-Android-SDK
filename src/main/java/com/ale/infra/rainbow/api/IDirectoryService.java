package com.ale.infra.rainbow.api;

import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.proxy.directory.SearchResponse;
import com.ale.infra.proxy.directory.SearchResponseByJid;

import java.util.List;

/**
 * Created by grobert on 09/11/15.
 */
public interface IDirectoryService extends IRainbowService
{
    void searchByName(String input, IAsyncServiceResultCallback<SearchResponse> callback);

    void searchByMails(List<String> emails, IAsyncServiceResultCallback<SearchResponse> iAsyncServiceResultCallback);

    void abortSearch();

    void searchByJid(String userid, String jid, IAsyncServiceResultCallback<SearchResponseByJid> callback);

    void searchNetwork(int limit, IAsyncServiceResultCallback<SearchResponse> callback);

    void searchByJids(List<String> jids, IAsyncServiceResultCallback<SearchResponse> callback);
}
