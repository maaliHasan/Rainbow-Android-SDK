package com.ale.infra.rainbow.api;

import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.proxy.portal.GetVersionResponse;

/**
 * Created by georges on 29/08/2016.
 */
public interface IPortalVersionService extends IRainbowService {

    void getEndUserVersion(IAsyncServiceResultCallback<GetVersionResponse> callback);
}
