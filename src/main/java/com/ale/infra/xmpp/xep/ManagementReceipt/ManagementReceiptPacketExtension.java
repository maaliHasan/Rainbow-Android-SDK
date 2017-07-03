package com.ale.infra.xmpp.xep.ManagementReceipt;

import com.ale.infra.invitation.CompanyJoinRequest;
import com.ale.infra.manager.room.RoomChange;
import com.ale.infra.invitation.CompanyInvitation;
import com.ale.infra.invitation.Invitation;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.jivesoftware.smack.packet.DefaultExtensionElement;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by wilsius on 14/06/16.
 */
public class ManagementReceiptPacketExtension extends DefaultExtensionElement {

    private static final String LOG_TAG = "ManagementReceiptPacketExtension";

    public static final String NAMESPACE = "jabber:iq:configuration";

    private String objectManaged = null;
    private String conversationId= null;
    private String m_conversationIdForMuteAction =  null;
    private String action = null ;
    private String managementOperation = "";

    private RoomChange room;
    private String groupId;
    private String userId;

    private Invitation m_invitation = null;
    private CompanyInvitation m_companyInvitation = null;
    private CompanyJoinRequest m_companyJoinRequest = null;

    public ManagementReceiptPacketExtension(XmlPullParser parser) {
        super("conversation", NAMESPACE);

        Log.getLogger().verbose(LOG_TAG, ">ManagementReceiptPacketExtension");


        try {

            String name = parser.getName();
            managementOperation = name;
            int eventType = parser.getEventType();
            String type="";

            String status="";
            String invitationId="";
            // parse xml structure until end of tag called "result"
            // do not parse the whole xml structure since this parser is used afterwards
            // warning : we have to keep the same initial depth when we have finished to parse.
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (!StringsUtil.isNullOrEmpty(name) &&
                        (eventType != XmlPullParser.END_TAG)) {
                    status="";
                    invitationId="";
                    switch (name) {
                        case "conversation":
                            objectManaged = name;
                            conversationId = parser.getAttributeValue("","id");
                            action = parser.getAttributeValue("","action");
                            break;
                        case "room":
                            objectManaged = name;
                            String roomId="";
                            String roomJid="";
                            String userJid="";
                            String topic="";
                            for (int i=0; i < parser.getAttributeCount();i++) {
                                switch (parser.getAttributeName(i) ){
                                    case "roomid":
                                        roomId = parser.getAttributeValue("","roomid");
                                        break;
                                    case "roomjid":
                                        roomJid = parser.getAttributeValue("","roomjid");
                                        break;
                                    case "status":
                                        status = parser.getAttributeValue("","status");
                                        break;
                                    case "userjid":
                                        userJid = parser.getAttributeValue("","userjid");
                                        break;
                                    case "topic":
                                        topic = parser.getAttributeValue("","topic");
                                        break;
                                    default:
                                        Log.getLogger().error(LOG_TAG, "parse room error unknown attribute" + parser.getAttributeName(i));
                                        break;
                                }
                            }
                            room = new RoomChange(roomId,roomJid);
                            room.setStatus(status);
                            room.setTopic(topic);
                            room.setUserJid(userJid);
                            break;

                        case "group":
                            objectManaged = name;
                            groupId="";
                            userId = "";
                            for (int i=0; i < parser.getAttributeCount();i++) {
                                switch (parser.getAttributeName(i)) {
                                    case "id":
                                        groupId = parser.getAttributeValue("", "id");
                                        break;
                                    case "action":
                                        action = parser.getAttributeValue("", "action");
                                        break;
                                    case "userId":
                                        userId = parser.getAttributeValue("", "userId");
                                    default:
                                        break;

                                }
                            }
                            break;

                        case "usersettings":
                            objectManaged = name;
                            for (int i=0; i < parser.getAttributeCount();i++) {
                                switch (parser.getAttributeName(i)) {
                                    case "action":
                                        action = parser.getAttributeValue("", "action");
                                        break;
                                    default:
                                        break;
                                }
                            }
                            break;

                        case "mute" :
                            objectManaged = name;
                            setConversationIdForMuteUnmute(parser);
                            break;

                        case "unmute":
                            objectManaged = name;
                            setConversationIdForMuteUnmute(parser);
                            break;

                        case "userinvite":
                            objectManaged = name;
                            type ="";
                            for (int i=0; i < parser.getAttributeCount();i++) {
                                switch (parser.getAttributeName(i)) {
                                    case "id":
                                        invitationId = parser.getAttributeValue("", "id");
                                        break;
                                    case "action":
                                        action = parser.getAttributeValue("", "action");
                                        break;
                                    case "type":
                                        type = parser.getAttributeValue("", "type");
                                        break;
                                    case "status":
                                        status = parser.getAttributeValue("", "status");
                                        break;
                                    default:
                                        break;

                                }
                            }
                            m_invitation = new Invitation();
                            m_invitation.setId(invitationId);
                            m_invitation.setSendingType(Invitation.SendingType.fromString(type));
                            m_invitation.setStatus(Invitation.InvitationStatus.fromString(status));
//                            <userinvite xmlns='jabber:iq:configuration' id='580a0a951912487aca1d4421' action='create' type='sent' status='pending'/>
                            break;

                        case "joincompanyinvite":
                            objectManaged = name;
                            for (int i=0; i < parser.getAttributeCount();i++) {
                                switch (parser.getAttributeName(i)) {
                                    case "id":
                                        invitationId = parser.getAttributeValue("", "id");
                                        break;
                                    case "action":
                                        action = parser.getAttributeValue("", "action");
                                        break;
                                    case "status":
                                        status = parser.getAttributeValue("", "status");
                                        break;
                                    default:
                                        break;

                                }
                            }
                            m_companyInvitation = new CompanyInvitation();
                            m_companyInvitation.setId(invitationId);
                            m_companyInvitation.setStatus(Invitation.InvitationStatus.fromString(status));
//                            <joincompanyinvite action="create" id='5819ed7c9547b313509237d6' status='pending' xmlns='jabber:iq:configuration'>
                            break;

                        case "joincompanyrequest":
                            objectManaged = name;
                            type = "";
                            for (int i=0; i < parser.getAttributeCount();i++) {
                                switch (parser.getAttributeName(i)) {
                                    case "id":
                                        invitationId = parser.getAttributeValue("", "id");
                                        break;
                                    case "action":
                                        action = parser.getAttributeValue("", "action");
                                        break;
                                    case "status":
                                        status = parser.getAttributeValue("", "status");
                                        break;
                                    case "type":
                                        type = parser.getAttributeValue("", "type");
                                        break;
                                    default:
                                        break;

                                }
                            }
                            m_companyJoinRequest = new CompanyJoinRequest();
                            m_companyJoinRequest.setId(invitationId);
                            m_companyJoinRequest.setStatus( CompanyJoinRequest.CompanyJoinRequestStatus.fromString(status));
                            break;


                        default:
                            break;
                    }
                }
                parser.next();
                name = parser.getName();
                eventType = parser.getEventType();
                if (!StringsUtil.isNullOrEmpty(name)&&
                        (eventType == XmlPullParser.END_TAG)) {
//                    Log.getLogger().verbose(LOG_TAG, "received END_TAG reached");
                    break;
                }
            }
        } catch (XmlPullParserException e) {
            Log.getLogger().error(LOG_TAG, "parseMamStanza; XmlPullParserException " + e.getMessage());
        } catch (IOException e) {
            Log.getLogger().error(LOG_TAG, "parseMamStanza; IOException " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "ManagementReceiptPacketExtension{" +
                " Operation= " + managementOperation +
                ", action= " + action +
                ", conversationId='" + conversationId + '\'' +

                '}';
    }

    @Override
    public String toXML() {
        // TODO : usefull ?
        return null;
    }
    public String getAction(){
        return action;
    }

    public String getConversationId(){
        return conversationId;
    }

    public String getConversationIdForMuteAction () {
        return m_conversationIdForMuteAction;
    }

    public String getManagementOperation(){
        return managementOperation;
    }

    public RoomChange getRoomChange() {return room; }

    public String getObjectManaged() { return objectManaged; }

    public String getUserId() { return userId;}

    public String getGroupId() { return groupId;}

    public Invitation getInvitation() { return m_invitation; }

    public CompanyInvitation getCompanyInvitation() { return m_companyInvitation; }

    public CompanyJoinRequest getCompanyJoinRequest() { return m_companyJoinRequest; }

    private void setConversationIdForMuteUnmute(XmlPullParser parser) {
        m_conversationIdForMuteAction = parser.getAttributeValue("", "conversation");
    }
}
