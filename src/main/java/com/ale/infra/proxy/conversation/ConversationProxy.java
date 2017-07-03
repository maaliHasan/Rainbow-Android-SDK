package com.ale.infra.proxy.conversation;

import com.ale.infra.application.IApplicationData;
import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.contact.IContactSearchListener;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseVoid;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceVoidCallback;
import com.ale.infra.manager.ChatMgr;
import com.ale.infra.manager.Conversation;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.directory.DirectoryProxy;
import com.ale.infra.proxy.directory.IDirectoryProxy;
import com.ale.infra.rainbow.api.IRainbowConversationService;
import com.ale.infra.rainbow.api.IServicesFactory;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.util.log.Log;

import java.util.List;

/**
 * Created by georges on 10/05/16.
 */
public class ConversationProxy implements IConversationProxy
{
    private static final String LOG_TAG = "ConversationProxy";

    private final DirectoryProxy m_directoryProxy;
    private final IContactCacheMgr m_contactCacheMgr;
    private IApplicationData m_applicationData;
    private IRainbowConversationService m_conversationService;

    public ConversationProxy(IServicesFactory servicesFactory, IRESTAsyncRequest httpClientFactory, IPlatformServices platformService,
                             IContactCacheMgr contactCacheMgr, DirectoryProxy directoryProxy)
    {
        Log.getLogger().info(LOG_TAG, "initialization");
        m_conversationService = servicesFactory.createConversationService(httpClientFactory, platformService, contactCacheMgr);
        m_applicationData = platformService.getApplicationData();
        m_directoryProxy = directoryProxy;
        m_contactCacheMgr = contactCacheMgr;
    }

    @Override
    public void getAllConversations(final ChatMgr chatMgr, final IGetAllConversationListener listener)
    {
        Log.getLogger().info(LOG_TAG, ">getAllConversations");

        m_conversationService.getAllConversations(m_applicationData.getUserId(), new IAsyncServiceResultCallback<GetAllConversationsResponse>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetAllConversationsResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().info(LOG_TAG, "GetAllConversations SUCCESS");

                    GetAllConversationsResponse convsResponse = asyncResult.getResult();
                    List<Conversation> conversations = convsResponse.getConversations();
                    chatMgr.onConversationsAdded(conversations);

                    m_contactCacheMgr.resolveDirectoryContacts(convsResponse.getUnresolvedContacts());

                    if (listener != null)
                        listener.onGetConversationsSuccess(conversations);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "GetAllConversations FAILURE", asyncResult.getException());

                    if (listener != null)
                        listener.onGetConversationsError();
                }
            }
        });
    }

    @Override
    public void createConversation(final Conversation conv, final ICreateConversationListener listener) {
        Log.getLogger().info(LOG_TAG, ">createConversation");

        if (conv.isRoomType()) {

            m_conversationService.createConversation(m_applicationData.getUserId(), conv.getRoom().getId(), "room", new IAsyncServiceResultCallback<String>() {
                @Override
                public void handleResult(AsyncServiceResponseResult<String> asyncResult) {
                    if (!asyncResult.exceptionRaised()) {
                        Log.getLogger().info(LOG_TAG, "CreateConversation SUCCESS");
                        if( listener != null)
                            listener.onCreationSuccess(asyncResult.getResult());
                    } else {
                        Log.getLogger().info(LOG_TAG, "CreateConversation FAILURE", asyncResult.getException());
                        if( listener != null)
                            listener.onCreationError();
                    }
                }
            });
        } else if (conv.getContact().isBot()) {
            //Bot cannot be searched
            m_conversationService.createConversation(m_applicationData.getUserId(), conv.getContact().getCorporateId(), "bot", new IAsyncServiceResultCallback<String>() {
                @Override
                public void handleResult(AsyncServiceResponseResult<String> asyncResult) {
                    if (!asyncResult.exceptionRaised()) {
                        Log.getLogger().info(LOG_TAG, "CreateConversation SUCCESS");
                        if( listener != null)
                            listener.onCreationSuccess(asyncResult.getResult());
                    } else {
                        Log.getLogger().info(LOG_TAG, "CreateConversation FAILURE", asyncResult.getException());
                        if( listener != null)
                            listener.onCreationError();
                    }
                }
            });

        } else {

            // TODO optimize the code - verify if contact is already known in ContactCache

            RainbowSdk.instance().contacts().searchByJid(conv.getJid(), new IContactSearchListener() {
                @Override
                public void searchStarted() {

                }

                @Override
                public void searchFinished(List<DirectoryContact> contactsFounded) {
                    if (contactsFounded == null || contactsFounded.isEmpty()) {
                        Log.getLogger().error(LOG_TAG, "CreateConversation FAILURE: empty search result");
                        if( listener != null)
                            listener.onCreationError();
                        return;
                    }

                    DirectoryContact dirContact = contactsFounded.get(0);
                    if (dirContact == null) {
                        Log.getLogger().error(LOG_TAG, "CreateConversation FAILURE: null search result");
                        if( listener != null)
                            listener.onCreationError();
                        return;
                    }

                    Contact contact = m_contactCacheMgr.getContactFromEmail(dirContact.getEmailAddresses());

                    if (contact != null) {
                        m_contactCacheMgr.setDirectoryContact(contact, dirContact);
                    } else {
                        contact = m_contactCacheMgr.createContactIfNotExistOrUpdate(dirContact);
                    }

                    m_conversationService.createConversation(m_applicationData.getUserId(), contact.getCorporateId(), "user", new IAsyncServiceResultCallback<String>() {
                        @Override
                        public void handleResult(AsyncServiceResponseResult<String> asyncResult) {
                            if (!asyncResult.exceptionRaised()) {
                                Log.getLogger().info(LOG_TAG, "CreateConversation SUCCESS");
                                if( listener != null)
                                    listener.onCreationSuccess(asyncResult.getResult());
                            } else {
                                Log.getLogger().info(LOG_TAG, "CreateConversation FAILURE", asyncResult.getException());
                                if( listener != null)
                                    listener.onCreationError();
                            }
                        }
                    });
                }

                @Override
                public void searchError() {
                    Log.getLogger().error(LOG_TAG, "CreateConversation FAILURE: search failure");
                    if( listener != null)
                        listener.onCreationError();
                }
            });

        }
    }

    @Override
    public void deleteConversation(String conversationId, final IDeleteConversationListener listener)
    {
        Log.getLogger().info(LOG_TAG, ">deleteConversation");

        m_conversationService.deleteConversation(m_applicationData.getUserId(), conversationId, new IAsyncServiceVoidCallback()
        {
            @Override
            public void handleResult(AsyncServiceResponseVoid asyncResult)
            {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "DeleteConversation SUCCESS");
                    if( listener != null)
                        listener.onDeletionSuccess();
                }
                else {
                    Log.getLogger().info(LOG_TAG, "DeleteConversation FAILURE", asyncResult.getException());
                    if( listener != null)
                        listener.onDeletionError();
                }
            }
        });
    }

    @Override
    public void updateConversation(final String userId, final String conversationId, final boolean muteState, final IUpdateConversationListener listener) {
        Log.getLogger().info(LOG_TAG, ">updateConversation");
        if (m_conversationService != null) {
            m_conversationService.updateConversation(userId, conversationId, muteState, new IAsyncServiceResultCallback(){

                @Override
                public void handleResult(AsyncServiceResponseResult asyncResult) {
                    if (!asyncResult.exceptionRaised())
                    {
                        Log.getLogger().info(LOG_TAG, "updateConversation SUCCESS");
                        Conversation conv = RainbowContext.getInfrastructure().getChatMgr().getConversationFromId(conversationId);
                        if( conv != null) {
                            conv.setMuteValue(muteState);
                            conv.notifyConversationUpdated();
                        }
                        if( listener != null)
                            listener.onUpdateConversationSuccess(conversationId);
                    } else {
                        Log.getLogger().info(LOG_TAG, "updateConversation FAILURE "+ asyncResult.getException() + " for userId : " + userId + " and conversationId = " + conversationId);
                        if( listener != null)
                            listener.onUpdateConversationFailed(conversationId);
                    }
                }

            });
        }
    }

    @Override
    public void muteAllConversations(final String userId, final boolean muteState, final IUpdateConversationListener listener) {
        Log.getLogger().info(LOG_TAG, ">muteAllConversations; "+muteState);

        ChatMgr chatMgr = RainbowContext.getInfrastructure().getChatMgr();
        if( chatMgr == null ) {
            Log.getLogger().warn(LOG_TAG, "No XmppConnection available");
            return;
        }

        if (m_conversationService != null) {
            final int[] successCtr = {0};
            final List<Conversation> convs = chatMgr.getConversations().getCopyOfDataList();
            for(Conversation conv : convs) {
                final String conversationId = conv.getId();
                m_conversationService.updateConversation(userId, conversationId, muteState, new IAsyncServiceResultCallback() {

                    @Override
                    public void handleResult(AsyncServiceResponseResult asyncResult) {
                        if (!asyncResult.exceptionRaised()) {
                            Log.getLogger().info(LOG_TAG, "updateConversation SUCCESS");
                            successCtr[0]++;
                            Conversation conv = RainbowContext.getInfrastructure().getChatMgr().getConversationFromId(conversationId);
                            if (conv != null) {
                                conv.setMuteValue(muteState);
                                conv.notifyConversationUpdated();
                            }
                            if (listener != null) {
                                if( successCtr[0] == convs.size())
                                    listener.onUpdateConversationSuccess(null);
                                else
                                    listener.onUpdateConversationFailed(null);
                            }

                        } else {
                            Log.getLogger().info(LOG_TAG, "updateConversation FAILURE " + asyncResult.getException() + " for userId : " + userId + " and conversationId = " + conversationId);
                        }
                    }

                });
            }
        }
    }

    @Override
    public void downLoadConversation(String userId, String conversationId, final IDownloadConversationListener downloadConversationListener) {
        Log.getLogger().info(LOG_TAG, "downLoadConversation");
        if (m_conversationService != null)
        {
            m_conversationService.downloadConversation(userId, conversationId, new IAsyncServiceResultCallback<String>() {
                @Override
                public void handleResult(AsyncServiceResponseResult<String> asyncResult) {
                    if (!asyncResult.exceptionRaised()) {
                        if (downloadConversationListener != null) {
                            downloadConversationListener.onDownloadConversationSuccess();
                        }
                    } else {
                        Log.getLogger().warn(LOG_TAG, "exception : " + asyncResult.getException().getMessage());
                        if (downloadConversationListener != null) {
                            downloadConversationListener.onDownloadConversationFailed(asyncResult.getException());
                        }
                    }
                }
            });
        }
    }
}
