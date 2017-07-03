package com.ale.infra.manager;

import android.content.Context;
import android.content.Intent;

import com.ale.infra.contact.Contact;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.xmpp.AbstractRainbowXMPPConnection;
import com.ale.infra.xmpp.xep.calllog.CallLog;
import com.ale.infra.xmpp.xep.calllog.CallLogGroup;
import com.ale.infra.xmpp.xep.calllog.CallLogIQResult;
import com.ale.infra.xmpp.xep.calllog.CallLogMessagePacketExtension;
import com.ale.infra.xmpp.xep.calllog.CallLogRequestIQ;
import com.ale.infra.xmpp.xep.calllog.DeleteCallLogIQResult;
import com.ale.infra.xmpp.xep.calllog.DeleteCallLogRequestIQ;
import com.ale.infra.xmpp.xep.calllog.DeletedCallLogMessagePacketExtension;
import com.ale.infra.xmpp.xep.calllog.ReadCallLogMessagePacketExtension;
import com.ale.infra.xmpp.xep.calllog.UpdatedCallLogMessagePacketExtension;
import com.ale.util.log.Log;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.DefaultExtensionElement;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.packet.id.StanzaIdUtil;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by cebruckn on 17/05/2017.
 */

public class CallLogMgr implements ChatMessageListener, ChatManagerListener
{
    public static final String CALLLOG_COUNTER_CHANGED = "CALLLOG_COUNTER_CHANGED";
    public static final String COUNTER = "COUNTER";
    private final static int MAX_LOGS = 75;
    private static final String LOG_TAG = "CallLogMgr";
    private static final int TIMEOUT_30S = 30000;
    private final AbstractRainbowXMPPConnection m_connection;
    private final IContactCacheMgr m_contactCacheMgr;
    private final String m_userJid;
    private final Context m_context;
    private ArrayItemList<CallLogGroup> m_callLogs = new ArrayItemList<>();
    private List<CallLog> m_downloadedLogs = new ArrayList<>();
    private boolean m_isRetrievingCallLogs = false;
    private Set<Chat> m_chats = new HashSet<>();

    public CallLogMgr(AbstractRainbowXMPPConnection connection, Context applicationContext, IContactCacheMgr contactCacheMgr, IPlatformServices platformServices)
    {
        m_connection = connection;
        m_context = applicationContext;
        m_contactCacheMgr = contactCacheMgr;
        m_userJid = platformServices.getApplicationData().getUserJidIm();

        ProviderManager.addExtensionProvider(CallLogMessagePacketExtension.ELEMENT, CallLogMessagePacketExtension.NAMESPACE, new ExtensionElementProvider<ExtensionElement>()
        {
            @Override
            public DefaultExtensionElement parse(XmlPullParser parser, int initialDepth) throws org.xmlpull.v1.XmlPullParserException, IOException
            {
                return new CallLogMessagePacketExtension(parser);
            }
        });

        ProviderManager.addExtensionProvider(UpdatedCallLogMessagePacketExtension.ELEMENT, UpdatedCallLogMessagePacketExtension.NAMESPACE, new ExtensionElementProvider<ExtensionElement>()
        {
            @Override
            public DefaultExtensionElement parse(XmlPullParser parser, int initialDepth) throws org.xmlpull.v1.XmlPullParserException, IOException
            {
                return new UpdatedCallLogMessagePacketExtension(parser);
            }
        });

        ProviderManager.addExtensionProvider(DeletedCallLogMessagePacketExtension.ELEMENT, DeletedCallLogMessagePacketExtension.NAMESPACE, new ExtensionElementProvider<ExtensionElement>()
        {
            @Override
            public DefaultExtensionElement parse(XmlPullParser parser, int initialDepth) throws org.xmlpull.v1.XmlPullParserException, IOException
            {
                return new DeletedCallLogMessagePacketExtension(parser);
            }
        });

        ProviderManager.addExtensionProvider(ReadCallLogMessagePacketExtension.ELEMENT, ReadCallLogMessagePacketExtension.NAMESPACE, new ExtensionElementProvider<ExtensionElement>()
        {
            @Override
            public DefaultExtensionElement parse(XmlPullParser parser, int initialDepth) throws org.xmlpull.v1.XmlPullParserException, IOException
            {
                return new ReadCallLogMessagePacketExtension(parser);
            }
        });

        ProviderManager.addIQProvider(CallLogIQResult.ELEMENT, CallLogMessagePacketExtension.NAMESPACE, new IQProvider<IQ>()
        {
            @Override
            public IQ parse(XmlPullParser xmlPullParser, int i) throws XmlPullParserException, IOException, SmackException
            {
                return new CallLogIQResult(xmlPullParser, CallLogIQResult.ELEMENT, CallLogMessagePacketExtension.NAMESPACE);
            }
        });

        ProviderManager.addIQProvider(DeleteCallLogIQResult.ELEMENT, CallLogMessagePacketExtension.NAMESPACE, new IQProvider<IQ>()
        {
            @Override
            public IQ parse(XmlPullParser xmlPullParser, int i) throws XmlPullParserException, IOException, SmackException
            {
                return new DeleteCallLogIQResult(xmlPullParser, DeleteCallLogIQResult.ELEMENT, CallLogMessagePacketExtension.NAMESPACE);
            }
        });

        ChatManager.getInstanceFor(m_connection).addChatListener(this);
    }


    public void disconnect()
    {
        ProviderManager.removeExtensionProvider(CallLogMessagePacketExtension.ELEMENT, CallLogMessagePacketExtension.NAMESPACE);
        ProviderManager.removeExtensionProvider(UpdatedCallLogMessagePacketExtension.ELEMENT, UpdatedCallLogMessagePacketExtension.NAMESPACE);
        ProviderManager.removeExtensionProvider(DeletedCallLogMessagePacketExtension.ELEMENT, DeletedCallLogMessagePacketExtension.NAMESPACE);
        ProviderManager.removeExtensionProvider(ReadCallLogMessagePacketExtension.ELEMENT, ReadCallLogMessagePacketExtension.NAMESPACE);
        ProviderManager.removeIQProvider(CallLogIQResult.ELEMENT, CallLogMessagePacketExtension.NAMESPACE);
        ProviderManager.removeIQProvider(DeleteCallLogIQResult.ELEMENT, CallLogMessagePacketExtension.NAMESPACE);
        ChatManager.getInstanceFor(m_connection).removeChatListener(this);

        for (Chat chat : m_chats)
            chat.removeMessageListener(this);

        m_chats.clear();
    }


    public ArrayItemList<CallLogGroup> getCallLogs()
    {
        return m_callLogs;
    }

    public void retrieveCallLogs(final ICallLogListener listener)
    {
        if (m_isRetrievingCallLogs)
        {
            if (listener != null)
                listener.onSuccess();

            return;
        }

        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Log.getLogger().info(LOG_TAG, "retrieveCallLogs");

                m_downloadedLogs.clear();

                m_isRetrievingCallLogs = true;

                CallLogRequestIQ callLogRequestIQ = new CallLogRequestIQ(MAX_LOGS);
                callLogRequestIQ.setType(IQ.Type.set);
                callLogRequestIQ.setStanzaId(StanzaIdUtil.newStanzaId());
                try
                {
                    StanzaListener packetListener = new StanzaListener()
                    {
                        @Override
                        public void processPacket(Stanza packet) throws SmackException.NotConnectedException
                        {
                            CallLogIQResult result = (CallLogIQResult) packet;

                            Log.getLogger().info(LOG_TAG, "retrieveCallLogs done - available : " + result.getCount());

                            List<CallLogGroup> callLogGroups = new ArrayList<>();

                            for (CallLog callLog : m_downloadedLogs)
                            {
                                CallLogGroup callLogGroup = getCallLogGroup(callLogGroups, callLog.getContact());
                                callLogGroup.getCallLogs().add(callLog);
                            }

                            for (CallLogGroup callLogGroup : callLogGroups)
                            {
                                Collections.sort(callLogGroup.getCallLogs(), Collections.reverseOrder());
                            }

                            Collections.sort(callLogGroups, Collections.reverseOrder());

                            m_callLogs.replaceAll(callLogGroups);

                            if (listener != null)
                                listener.onSuccess();

                            m_connection.removeSyncStanzaListener(this);

                            m_isRetrievingCallLogs = false;

                            notifyCounterChanged();
                        }
                    };

                    m_connection.addSyncStanzaListener(packetListener, new StanzaTypeFilter(CallLogIQResult.class));
                    m_connection.createPacketCollectorAndSend(callLogRequestIQ).nextResultOrThrow(TIMEOUT_30S);
                }
                catch (Exception e)
                {
                    Log.getLogger().error(LOG_TAG, "Error while retrieving the callLogs", e);

                    if (listener != null)
                        listener.onFailure();
                }
            }
        });
        thread.start();
    }

    private CallLogGroup getCallLogGroup(List<CallLogGroup> callLogGroups, Contact contact)
    {
        for (CallLogGroup callLogGroup : callLogGroups)
        {
            if (callLogGroup.getContact() == contact)
                return callLogGroup;
        }

        CallLogGroup callLogGroup = new CallLogGroup(contact);
        callLogGroups.add(callLogGroup);

        return callLogGroup;
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally)
    {
        chat.addMessageListener(this);

        m_chats.add(chat);
    }

    @Override
    public void processMessage(Chat chat, Message message)
    {
        CallLogMessagePacketExtension callLogMessagePacketExtension = message.getExtension(CallLogMessagePacketExtension.ELEMENT, CallLogMessagePacketExtension.NAMESPACE);
        UpdatedCallLogMessagePacketExtension updatedCallLogMessagePacketExtension = message.getExtension(UpdatedCallLogMessagePacketExtension.ELEMENT, UpdatedCallLogMessagePacketExtension.NAMESPACE);
        DeletedCallLogMessagePacketExtension deletedCallLogMessagePacketExtension = message.getExtension(DeletedCallLogMessagePacketExtension.ELEMENT, DeletedCallLogMessagePacketExtension.NAMESPACE);
        ReadCallLogMessagePacketExtension readCallLogMessagePacketExtension = message.getExtension(ReadCallLogMessagePacketExtension.ELEMENT, ReadCallLogMessagePacketExtension.NAMESPACE);

        if (callLogMessagePacketExtension != null)
        {
            CallLog callLog = new CallLog(callLogMessagePacketExtension, m_contactCacheMgr, m_userJid);

            if (callLog.getContact().getImJabberId().contains("@"))
                m_downloadedLogs.add(callLog);
        }

        if (updatedCallLogMessagePacketExtension != null)
        {
            CallLog callLog = new CallLog(updatedCallLogMessagePacketExtension, m_contactCacheMgr, m_userJid);

            if (callLog.getContact().getImJabberId().contains("@"))
            {
                List<CallLogGroup> callLogGroups = m_callLogs.getCopyOfDataList();

                CallLogGroup callLogGroup = getCallLogGroup(callLogGroups, callLog.getContact());
                callLogGroup.getCallLogs().add(callLog);

                Collections.sort(callLogGroup.getCallLogs(), Collections.reverseOrder());
                Collections.sort(callLogGroups, Collections.reverseOrder());

                m_callLogs.replaceAll(callLogGroups);

                notifyCounterChanged();
            }
        }

        if (deletedCallLogMessagePacketExtension != null)
        {
            List<CallLogGroup> callLogGroups = m_callLogs.getCopyOfDataList();

            for (CallLogGroup callLogGroup : callLogGroups)
            {
                if (callLogGroup.getContact().getImJabberId().equals(deletedCallLogMessagePacketExtension.getPeer()))
                {
                    m_callLogs.delete(callLogGroup);
                    notifyCounterChanged();
                    break;
                }
            }
        }

        if (readCallLogMessagePacketExtension != null)
        {
            List<CallLogGroup> callLogGroups = m_callLogs.getCopyOfDataList();

            for (CallLogGroup callLogGroup : callLogGroups)
            {
                for (CallLog log : callLogGroup.getCallLogs())
                {
                    if (log.getCallId().equals(readCallLogMessagePacketExtension.getCallId()))
                    {
                        log.setIsAck();
                        notifyCounterChanged();
                        break;
                    }
                }
            }
        }
    }

    public void deleteCallLogs(final List<CallLogGroup> conversationList, final ICallLogListener listener)
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Log.getLogger().info(LOG_TAG, "deleteCallLogs");

                for (final CallLogGroup callLogGroup : conversationList)
                {
                    DeleteCallLogRequestIQ deleteCallLogRequestIQ = new DeleteCallLogRequestIQ(callLogGroup.getContact().getImJabberId());
                    deleteCallLogRequestIQ.setType(IQ.Type.set);
                    deleteCallLogRequestIQ.setStanzaId(StanzaIdUtil.newStanzaId());
                    try
                    {
                        StanzaListener packetListener = new StanzaListener()
                        {
                            @Override
                            public void processPacket(Stanza packet) throws SmackException.NotConnectedException
                            {
                                DeleteCallLogIQResult result = (DeleteCallLogIQResult) packet;

                                Log.getLogger().info(LOG_TAG, "deleteCallLogs done - deleted : " + result.getCount());

                                m_callLogs.delete(callLogGroup);

                                m_connection.removeSyncStanzaListener(this);

                                notifyCounterChanged();
                            }
                        };

                        m_connection.addSyncStanzaListener(packetListener, new StanzaTypeFilter(DeleteCallLogIQResult.class));
                        m_connection.createPacketCollectorAndSend(deleteCallLogRequestIQ).nextResultOrThrow(TIMEOUT_30S);
                    }
                    catch (Exception e)
                    {
                        Log.getLogger().error(LOG_TAG, "Error while deleting the callLogs", e);

                        if (listener != null)
                            listener.onFailure();

                        return;

                    }
                }

                if (listener != null)
                    listener.onSuccess();
            }
        });
        thread.start();
    }

    public void markAllAsRead()
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Log.getLogger().info(LOG_TAG, "markAllAsRead");

                List<CallLog> logsToAck = new ArrayList<>();

                for (CallLogGroup group : m_callLogs.getCopyOfDataList())
                {
                    for (CallLog log : group.getCallLogs())
                    {
                        if (log.isMissed())
                            logsToAck.add(log);
                    }
                }

                for (CallLog log : logsToAck)
                {
                    Message message = new Message(m_userJid);
                    ReadCallLogMessagePacketExtension readCallLogMessagePacketExtension = new ReadCallLogMessagePacketExtension(log.getCallId());
                    message.addExtension(readCallLogMessagePacketExtension);

                    try
                    {
                        m_connection.sendStanza(message);
                        notifyCounterChanged();
                    }
                    catch (SmackException.NotConnectedException e)
                    {
                        Log.getLogger().error(LOG_TAG, "Not able to ack a callLog: ", e);
                    }
                }
            }
        });
        thread.start();
    }

    private void notifyCounterChanged()
    {
        int counter = 0;

        for (CallLogGroup group : m_callLogs.getCopyOfDataList())
        {
            for (CallLog log : group.getCallLogs())
            {
                if (log.isMissed())
                    counter++;
            }
        }

        Intent intent = new Intent(CALLLOG_COUNTER_CHANGED);
        intent.putExtra(COUNTER, counter);

        m_context.sendBroadcast(intent);
    }

    public interface ICallLogListener
    {
        void onSuccess();

        void onFailure();
    }
}
