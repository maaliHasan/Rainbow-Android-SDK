package com.ale.infra.rainbow.api;


public class ApisConstants
{

    public static final String LOGIN_APPLICATION = "/api/rainbow/applications/v1.0/authentication/login";
    public static final String LOGIN_API = "/api/rainbow/authentication/v1.0/login";
    public static final String LOGOUT_API = "/api/rainbow/authentication/v1.0/logout";
    public static final String SEARCH_USER_BY_NAME = "/api/rainbow/search/v1.0/users";
    public static final String USERS = "/api/rainbow/enduser/v1.0/users/";
    public static final String SEND_USER_INVITE = "/invitations";
    public static final String RE_SEND = "/re-send";
    public static final String SELF_REGISTER_INVITE = "/api/rainbow/enduser/v1.0/notifications/emails/self-register";
    public static final String SELF_RESET_PWD_EMAIL = "/api/rainbow/enduser/v1.0/notifications/emails/reset-password";
    public static final String SELF_REGISTER_USER = "/api/rainbow/enduser/v1.0/users/self-register";
    public static final String SELF_RESET_USER_PWD = "/api/rainbow/enduser/v1.0/users/reset-password";
    public static final String ICE_SERVERS = "/api/rainbow/enduser/v1.0/settings/iceservers";
    public static final String FILE_STORAGE = "/api/rainbow/filestorage/v1.0/files";
    public static final String FILE_STORAGE_CONSUMTION = "/api/rainbow/filestorage/v1.0/users/consumption";
    public static final String FILE_SERVER = "/api/rainbow/fileserver/v1.0/files";

    public static final String AVATAR = "/api/avatar";
    public static final String ROOMS = "/api/rainbow/enduser/v1.0/rooms";
    public static final String BOTS = "/api/rainbow/enduser/v1.0/bots";
    public static final String USERSROOM = "/users";
    public static final String USERS_ABOUT = "/api/rainbow/enduser/v1.0/about";
    public static final String CONVERSATION = "/conversations";
    public static final String SETTINGS = "/settings";

    public static final String RECEIVED_INVITATIONS = "/invitations/received";
    public static final String SENT_INVITATIONS = "/invitations/sent";
    public static final String INVITATIONS = "/invitations/";
    public static final String ACCEPT = "/accept";
    public static final String DECLINE = "/decline";
    public static final String CANCEL = "/cancel";

    public static final String JOIN_COMPANY_RECEIVED_INVITATIONS = "/join-companies/invitations";
    public static final String JOIN_COMPANY_REQUESTED_INVITATIONS = "/join-companies/requests";

    public static final String COMPANY= "/api/rainbow/enduser/v1.0/companies";
    public static final String FEATURES = "/profiles/features";

    public static final String FILE_STORAGE_VIEWERS = "/viewers";
    public static final String BANNER= "/api/banner";

    public static final String PGI_CONFPROVISIONNING = "/api/rainbow/confprovisioning/v1.0/conferences";
    public static final String PGI_CONFERENCES = "/api/rainbow/conference/v1.0/conferences/";
    public static final String CONFERENCESROOM = "/conferences";
    public static final String START_PGICONFERENCE = "start";
    public static final String STOP_PGICONFERENCE = "stop";
    public static final String JOIN_PGICONFERENCE = "join";
    public static final String SNAPSHOT_PGICONFERENCE = "snapshot";
    public static final String PGI_PARTICIPANTS = "participants";
    public static final String RESUME_RECORDING = "resume-recording";

    public static final String JIDS = "jids";
    public static final String LOGINEMAILS = "loginemails";

    public static final String NETWORK = "/api/rainbow/enduser/v1.0/users/networks";
}
