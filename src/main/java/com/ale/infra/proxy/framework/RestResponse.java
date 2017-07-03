package com.ale.infra.proxy.framework;

/**
 * Created by grobert on 27/10/15.
 */
public class RestResponse {

    // Rainbow application authentication
    public static final String LOGGEDINAPPLICATION = "loggedInApplication";
    public static final String APP_ID = "appId";
    public static final String APP_SECRET = "appSecret";
    public static final String APP_NAME = "name";
    public static final String DATE_OF_CREATION = "dateOfCreation";

    // Rainbow authentication:
    public static final String TOKEN = "token";
    public static final String LOGGEDINUSER = "loggedInUser";
    public static final String ID = "id";
    public static final String COMPANY_ID = "companyId";
    public static final String LOGIN = "loginEmail";
    public static final String JID_IM = "jid_im";
    public static final String JID_TEL = "jid_tel";
    public static final String JID_PASSWORD = "jid_password";
    public static final String INITIALIZED = "isInitialized";
    public static final String JID = "jid";
    public static final String PROFILES = "profiles";

    // Profile:
    public static final String SUBSCRIPTION_ID = "subscriptionId";
    public static final String OFFER_ID = "offerId";
    public static final String OFFER_NAME = "offerName";
    public static final String PROFILE_ID = "profileId";
    public static final String PROFILE_NAME = "profileName";
    public static final String ASSIGNATION_DATE = "assignationDate";
    public static final String PROFILE_STATUS = "status";
    public static final String ISDEFAULT = "isDefault";
    public static final String ISCOMPANYDEFAULT = "isInDefaultCompany";

    // About
    public static final String VERSION = "version"; //Room name

    // Rainbow conversation:
    public static final String PEER_ID = "peerId";
    public static final String TYPE = "type";
    public static final String TYPE_USER = "user";
    public static final String TYPE_BOT = "bot";
    public static final String TYPE_ROOM = "room";
    public static final String UNRECEIVED_MSG_NUMBER = "unreceivedMessageNumber";
    public static final String UNREAD_MSG_NUMBER = "unreadMessageNumber";
    public static final String TOPIC = "topic"; //Room topic
    public static final String NAME = "name"; //Room name
    public static final String MUTE = "mute"; // mute state for conversations and rooms
    public static final String LAST_MSG_CONTENT = "lastMessageText";
    public static final String LAST_MSG_DATE = "lastMessageDate";
    public static final String LAST_MSG_SENDER_ID = "lastMessageSender";
    public static final String LAST_MSG_CALL = "call";
    public static final String LAST_MSG_CALL_STATE = "state";
    public static final String LAST_MSG_CALL_DURATION = "duration";

    // Rainbow directory Search:
    public static final String FIRSTNAME = "firstName";
    public static final String LASTNAME = "lastName";
    public static final String DISPLAYNAME = "displayName";
    public static final String NICKNAME = "nickName";
    public static final String COUNTRY = "country";
    public static final String LANGUAGE = "language";
    public static final String TIMEZONE = "timezone";
    public static final String TITLE = "title";
    public static final String ROLES = "roles";
    public static final String JOBTITLE = "jobTitle";
    public static final String LOGINEMAIL = "loginEmail";
    public static final String EMAILS = "emails";
    public static final String EMAILS_VALUE = "email";
    public static final String EMAILS_TYPE = "type";
    public static final String EMAILS_TYPE_WORK = "work";
    public static final String EMAILS_TYPE_HOME = "home";
    public static final String EMAILS_TYPE_OTHER = "other";
    public static final String PHONE_NUMBERS = "phoneNumbers";
    public static final String NUMBER = "number";
    public static final String NUMBER_E164 = "numberE164";
    public static final String PHONE_NUMBER_ID = "phoneNumberId";
    public static final String IS_FROM_SYSTEM = "isFromSystem";
    public static final String SYSTEM_ID = "systemId";
    public static final String PBX_ID = "pbxId";
    public static final String DEVICE_TYPE = "deviceType";
    public static final String PHONE_NUMBER_TYPE_HOME = "home";
    public static final String PHONE_NUMBER_TYPE_WORK = "work";
    public static final String PHONE_NUMBER_TYPE_OTHER = "other";
    public static final String PHONE_NUMBER_DEVICE_TYPE_LANDLINE = "landline";
    public static final String PHONE_NUMBER_DEVICE_TYPE_MOBILE = "mobile";
    public static final String PHONE_NUMBER_DEVICE_TYPE_FAX = "fax";
    public static final String PHONE_NUMBER_DEVICE_TYPE_OTHER = "other";
    public static final String COMPANY_NAME = "companyName";
    public static final String LAST_AVATAR_UPDATE_DATE = "lastAvatarUpdateDate";
    public static final String LAST_BANNER_UPDATE_DATE = "lastBannerUpdateDate";

    public static final String IN_DEFAULT_COMPAGNY = "isInDefaultCompany";

    //misc
    public static final String OFFSET = "offset";
    public static final String LIMIT = "limit";

    //invitation
    public static final String INVITED_USER_ID = "invitedUserId";
    public static final String INVITED_USER_EMAIL = "invitedUserEmail";
    public static final String INVITING_USER_ID = "invitingUserId";
    public static final String INVITING_USER_EMAIL = "invitingUserEmail";
    public static final String INVITING_DATE = "invitingDate";
    public static final String INVITATION_REQUESTED_NOTIFICATION_LANGUAGE = "requestedNotificationLanguage";
    public static final String INVITATION_LAST_NOTIFICATION_DATE = "lastNotificationDate";
    public static final String INVITATION_ACCEPTANCE_DATE = "acceptationDate";
    public static final String INVITATION_DECLINATION_DATE = "declinationDate";
    public static final String INVITATION_STATUS = "status";
    public static final String INVITATION_TYPE = TYPE;

    //company invitation
    public static final String INVITING_ADMIN_LOGIN_EMAIL = "invitingAdminLoginEmail";
    public static final String INVITING_ADMIN_ID = "invitingAdminId";
    public static final String INVITATION_DATE = "invitationDate";

    // File Server
    public static final String FILE_DESCRIPTOR_DATA = "data";
    public static final String FILE_DESCRIPTOR_FILENAME = "fileName";
    public static final String FILE_DESCRIPTOR_EXTENSION = "extension";
    public static final String FILE_DESCRIPTOR_OWNERID = "ownerId";
    public static final String FILE_DESCRIPTOR_ISUPLOADED = "isUploaded";
    public static final String FILE_DESCRIPTOR_REGISTRATIONDATE = "registrationDate";
    public static final String FILE_DESCRIPTOR_UPLOADEDDATE = "uploadedDate";
    public static final String FILE_DESCRIPTOR_SIZE = "size";
    public static final String FILE_DESCRIPTOR_TYPEMIME = "typeMIME";
    public static final String FILE_DESCRIPTOR_ID = "id";
    public static final String FILE_DESCRIPTOR_VIEWERS = "viewers";
    public static final String FILE_DESCRIPTOR_VIEWER_ID = "viewerId";
    public static final String FILE_DESCRIPTOR_VIEWER_TYPE = "type";
    public static final String FILE_DESCRIPTOR_DATE_SORT = "dateToSort";
    //company join request
    public static final String JOIN_REQUEST_USER_ID = "requestingUserId";
    public static final String JOIN_REQUEST_USER_LOGIN_EMAIL = "requestingUserLoginEmail";
    public static final String JOIN_REQUEST_COMPANY_ID = "requestedCompanyId";
    public static final String JOIN_REQUEST_COMPANY_ADMIN_ID = "companyAdminId";
    public static final String JOIN_REQUEST_COMPANY_NAME = "requestedCompanyName";
    public static final String JOIN_REQUEST_TO_COMPANY_ADMIN = "requestedToCompanyAdmin";
    public static final String JOIN_REQUEST_COMPANY_ADMIN_LOGIN_EMAIL = "companyAdminLoginEmail";
    public static final String JOIN_REQUEST_REQUESTING_DATE = "requestingDate";
    //company contact
    public static final String DESCRITPION = "description";
    public static final String OFFER_TYPE = "offerType";
    public static final String STATUS = "status";
    public static final String WEB_SITE = "website";
    public static final String ORGANISATION_ID = "organisationId";
    public static final String VISIBILITY = "visibility";
    public static final String VISIBILITY_BY = "visibleBy";
    public static final String FORCE_HANDSHAKE = "forceHandshake";
    public static final String ADMIN_EMAIL = "adminEmail";
    public static final String SUPPORT_EMAIL = "supportEmail";
    public static final String NUMBER_USERS = "numberUsers";
    public static final String COMPANY_SIZE = "size";
    public static final String CREATION_DATE = "creationDate";
    public static final String STATUS_UPDATE_DATE = "statusUpdatedDate";
    public static final String VISIBILITY_REQUESTS = "visibilityRequests";
    public static final String SLOGAN = "slogan";

    // Features
    public static final String FEATURES_DATA = "data";
    public static final String FEATURES_ID = "featureId";
    public static final String FEATURES_UNIQUEREF = "featureUniqueRef";
    public static final String FEATURES_NAME = "featureName";
    public static final String FEATURES_TYPE = "featureType";
    public static final String FEATURES_ISENABLED = "isEnabled";
    public static final String FEATURES_LIMIT_MIN = "limitMin";
    public static final String FEATURES_LIMIT_MAX = "limitMax";
    public static final String FEATURES_ADDED_DATE = "addedDate";
    public static final String FEATURES_LASTUPDATEDATE = "lastUpdateDate";

    // PGI Conferences
    public static final String PROVIDER_USERID = "providerUserId";
    public static final String PROVIDER_CONFID = "providerConfId";
    public static final String PROVIDER_TYPE = "providerType";
    public static final String CONFERENCE_USERID = "confUserId";
    public static final String USERID = "userId";
    public static final String COMPANYID = "companyId";
    public static final String MEDIATYPE = "mediaType";
    public static final String JIDIM = "jid_im";
    public static final String JIDTEL = "jid_tel";
    public static final String PARTICIPANT_ROLE = "role";
    public static final String PARTICIPANT_MUTE = "mute";
    public static final String PARTICIPANT_HOLD = "held";
    public static final String CONFERENCE_STARTDATE = "confStartDate";
    public static final String STARTDATE = "startDate";
    public static final String PHONENUMBER = "phoneNumber";
    public static final String STATE = "state";
}
