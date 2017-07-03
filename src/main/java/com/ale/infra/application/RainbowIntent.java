/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Summary :
 * *****************************************************************************
 * History
 * 23/09/2010 cebruckn crms00261849 bad display in privatecommunication after an business incoming call
 * 04/10/2010 m.geyer crms00264357 No more events if connection cut by Reverse Proxy
 * 04/10/2010 LDO crms00262797 Voice Mail button invisible
 * 20/10/2010 LDO       crms00258643  If the list is empty, return to the home activity
 * 2010/11/10 cebruckn crms00273420 [TEL] incoming call while making an outgoing call is not displayed
 * 2011/02/23 ldouguet crms00296379 My Current Phone and second business outgoing call
 * 2011/09/01 cebruckn crms00334461 [voice mail] counter is increased but an action is necessary to see the new message
 * 2011/09/01 M.Geyer  crms00339590 bad message indication after playing a voice message  (in communication established).
 * 2011/10/18 cebruckn crms00342193 "New call" must go to the Communications tab
 * 2011/11/29 cebruckn crms00348152 [GUI]-Certificate installation prompt hidden
 * 2012/01/12 franci11 crms00355333 show eventTab animation when fillEmptyFieldsWithContact is in progress
 * 2012/03/08 cebruckn crms00365607 (Android) Callback voicemail sender does not work with OXO
 * 2012/09/07 cebruckn crms00395146 Android 4.1 crash report analysis and corrections
 * 2013/06/13 cebruckn crms00441527 [enhancement]-Use proximity sensor in Call screens + VM screens
 */
package com.ale.infra.application;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * List of intent.
 *
 */
public final class RainbowIntent
{
    public static final String ACTION_RAINBOW_LOGIN_SUCCESS = "act_rainbow_login_success";
    public static final String ACTION_RAINBOW_LOGOUT = "act_rainbow_logout";
    public static final String ACTION_RAINBOW_LOGIN_ERROR = "act_rainbow_login_failed";
    public static final String ACTION_RAINBOW_LOGIN_AUTHENTICATION_FAILED = "act_rainbow_login_authentication_failed";
	public static final String ACTION_RAINBOW_XMPP_AUTHENTICATION_SUCCESS = "act_rainbow_xmpp_authentication_success";

    public static final String ACTION_HOME = "act_rainbow_home";
    public static final String ACTION_QUIT = "act_rainbow_quit";


    public static final String ACTION_RAINBOW_PROVISIONING_FAILED = "act_rainbow_provisioning_failed";


    public static final String ACTION_UPDATE_IN_PROGRESS = "act_rainbow_update_in_progress";

    public static final String ACTION_VOIP_SETTING_CHANGED = "act_rainbow_voip_setting_changed";
    public static final String ACTION_DISPLAYNAMEFORMAT_CHANGED = "act_rainbow_display_name_format_setting_changed";
    public static final String ACTION_CELLULAR_VOIP_SETTING_CHANGED = "act_rainbow_cellular_voip_setting_changed";


    public static final String ACTION_ACCOUNT_REMOVED = "act_rainbow_account_removed";
    public static final String ACTION_BRING_APP_TO_FRONT = "act_rainbow_bring_app_to_front";
    public static final String ACTION_OPEN_CONTACT_TAB_IN_SEARCH_MODE = "act_rainbow_open_contact_tab_in_search_mode";
    public static final String ACTION_OPEN_ROOM_TAB_IN_NOTIF_MODE = "act_rainbow_open_room_tab_in_notif_mode";
    public static final String ACTION_ACCEPT_INVITATION_ROOM_NOTIF = "act_rainbow_accept_invitation_room_notif";
    public static final String ACTION_REJECT_INVITATION_ROOM_NOTIF = "act_rainbow_reject_invitation_room_notif";
    public static final String ACTION_TAB_CHANGED = "act_rainbow_tab_changed";
    public static final String ACTION_GETCONVERSATION_ERROR = "act_rainbow_getconversation_error";
    public static final String ACTION_ACCEPT_IM_NOTIF = "act_rainbow_accept_invitation_im_notif";
    public static final String ACTION_MUTE_IM_NOTIF = "act_rainbow_mute_im_notif";
    public static final String ACTION_SUBSCRIPTION_REQUEST = "act_rainbow_subscription_request";
    public static final String ACTION_REFERRER_RECEIVED = "act_rainbow_referrer_received";
    public static final String DISPLAY_IM = "com.ale.rainbow.display_im";
    public static final String DISPLAY_TAB_CONTACT_ROSTER = "com.ale.rainbow.hometab.contact.roster";
    public static final String ACTION_WIDGET_MANUAL_UPDATE = "act_rainbow_appwidget_manual_update";

    public static final String TAB_SELECTED = "TabSelected";
    public static final String ACTION_CALL_HANG_UP = "act_rainbow_call_hang_up";
    public static final String ACTION_CALL_PICK_UP = "act_rainbow_call_pick_up";
    public static final String ACTION_CALL_REJECT = "act_rainbow_call_reject";
    public static final String ACTION_DISPLAY_CALL_SCREEN = "act_rainbow_display_call_screen";
    public static final String ACTION_DISMISS_CALL_SCREEN = "act_rainbow_dismiss_call_screen";

	public static final String ACTION_REQUEST_FILE_DESCRIPTOR = "act_rainbow_request_file_descriptor";

    public static final String ACTION_OPEN_CONTACT_TAB_IN_NOTIF_MODE = "act_rainbow_open_contact_tab_in_notif_mode";
    public static final String ACTION_ACCEPT_INVITATION_NOTIF = "act_rainbow_accept_invitation_notif";
    public static final String ACTION_REJECT_INVITATION_NOTIF = "act_rainbow_reject_invitation_notif";
    public static final String ACTION_ACCEPT_COMPANY_INVITATION_NOTIF = "act_rainbow_accept_company_invitation_notif";
    public static final String ACTION_REJECT_COMPANY_INVITATION_NOTIF = "act_rainbow_reject_company_invitation_notif";


    public static final String ACTION_RAINBOW_PGI_JOIN_SUCCESS = "act_rainbow_pgi_join_success";

    /**
     * Instantiating utility classes does not make sense. Hence the constructors should either be
     * private or (if you want to allow subclassing) protected. A common mistake is forgetting to
     * hide the default constructor.
     */
    private RainbowIntent()
    {
        throw new UnsupportedOperationException();
    }

    public static Intent getLauncherIntent(Context context, Class activity)
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(new ComponentName(context, activity));
        return intent;
    }
}
