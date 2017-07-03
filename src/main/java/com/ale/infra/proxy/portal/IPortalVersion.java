package com.ale.infra.proxy.portal;


/**
 * Created by georges on 29/08/2016.
 */
public interface IPortalVersion {

    void getEndUserVersion(IPortalListener listener);

    interface IPortalListener
    {
        void onSuccess(String portalVersion);

        void onFailure();
    }
}
