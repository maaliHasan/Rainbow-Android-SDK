package com.ale.infra.searcher.localContact;

import android.database.ContentObserver;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.util.log.Log;

/**
 * Created by georges on 02/03/16.
 */
public class LocalContactContentObserver extends ContentObserver
{
    private String LOG_TAG = "LocalContactContentObserver";

    private long m_lastTimeofCall = 0L;
    private long m_lastTimeofUpdate = 0L;
    private static long THRESHOLD_TIME = 5000;


    public LocalContactContentObserver() {
        super(null);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Log.getLogger().debug(LOG_TAG, "LocalContact has changed; " + selfChange);

        m_lastTimeofCall = System.currentTimeMillis();

        if(m_lastTimeofCall - m_lastTimeofUpdate > THRESHOLD_TIME){
            Log.getLogger().debug(LOG_TAG, "LocalContact has changed and will be processed");

            //write your code to find updated contacts here
            IContactCacheMgr contactCache = RainbowContext.getInfrastructure().getContactCacheMgr();
            if( contactCache != null) {
                contactCache.clearMobileLocalContacts();
                contactCache.retrieveMobileLocalContacts();
            }

            m_lastTimeofUpdate = System.currentTimeMillis();
        }

    }


    @Override
    public boolean deliverSelfNotifications() {
        return true;
    }
}
