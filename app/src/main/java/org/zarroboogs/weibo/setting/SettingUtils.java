
package org.zarroboogs.weibo.setting;

import org.zarroboogs.weibo.BeeboApplication;
import org.zarroboogs.weibo.R;
import org.zarroboogs.weibo.setting.activity.SettingActivity;
import org.zarroboogs.weibo.support.utils.AppConfig;
import org.zarroboogs.weibo.support.utils.Utility;

import android.content.Context;

public class SettingUtils {

    private static final String FIRSTSTART = "firststart";

    private static final String LAST_FOUND_WEIBO_ACCOUNT_LINK = "last_found_weibo_account_link";

    private static final String BLACK_MAGIC = "black_magic";

    private static final String CLICK_TO_TOP_TIP = "click_to_top_tip";

    private SettingUtils() {

    }

    public static boolean isDebug() {
        return false;
    }

    public static void setDefaultAccountId(String id) {
        SettingHelper.setEditor(getContext(), "id", id);
    }

    public static String getDefaultAccountId() {
        return SettingHelper.getSharedPreferences(getContext(), "id", "");
    }

    private static Context getContext() {
        return BeeboApplication.getInstance();
    }

    public static boolean firstStart() {
        boolean value = SettingHelper.getSharedPreferences(getContext(), FIRSTSTART, true);
        if (value) {
            SettingHelper.setEditor(getContext(), FIRSTSTART, false);
        }
        return value;
    }

    public static boolean isEnableFilter() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.FILTER, false);
    }

    public static int getFontSize() {
        String value = SettingHelper.getSharedPreferences(getContext(), SettingActivity.FONT_SIZE, "15");
        return Integer.valueOf(value);
    }

    public static int getAppTheme() {
        String value = SettingHelper.getSharedPreferences(getContext(), SettingActivity.THEME, "1");

        switch (Integer.valueOf(value)) {
//            case 1:
//                return R.style.AppTheme_Light;
//
//            case 2:
//                return R.style.AppTheme_Dark;

            default:
                return R.style.AppTheme_Light;

        }
    }

    public static void switchToAnotherTheme() {
        String value = SettingHelper.getSharedPreferences(getContext(), SettingActivity.THEME, "1");
        switch (Integer.valueOf(value)) {
            case 1:
                SettingHelper.setEditor(getContext(), SettingActivity.THEME, "2");
                break;
            case 2:
                SettingHelper.setEditor(getContext(), SettingActivity.THEME, "1");
                break;
            default:
                SettingHelper.setEditor(getContext(), SettingActivity.THEME, "1");
                break;

        }
    }

    public static int getHighPicMode() {
        String value = SettingHelper.getSharedPreferences(getContext(), SettingActivity.LIST_HIGH_PIC_MODE, "2");
        return Integer.valueOf(value);
    }

    public static int getCommentRepostAvatar() {
        String value = SettingHelper.getSharedPreferences(getContext(), SettingActivity.COMMENT_REPOST_AVATAR, "1");
        return Integer.valueOf(value);
    }

    public static int getListAvatarMode() {
        String value = SettingHelper.getSharedPreferences(getContext(), SettingActivity.LIST_AVATAR_MODE, "1");
        return Integer.valueOf(value);
    }

    public static int getListPicMode() {
        String value = SettingHelper.getSharedPreferences(getContext(), SettingActivity.LIST_PIC_MODE, "3");
        return Integer.valueOf(value);
    }

    public static void setEnableCommentRepostAvatar(boolean value) {
        SettingHelper.setEditor(getContext(), SettingActivity.SHOW_COMMENT_REPOST_AVATAR, value);
    }

    public static boolean getEnableCommentRepostListAvatar() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.SHOW_COMMENT_REPOST_AVATAR, true);
    }

    public static int getNotificationStyle() {
        String value = SettingHelper.getSharedPreferences(getContext(), SettingActivity.JBNOTIFICATION_STYLE, "1");

        switch (Integer.valueOf(value)) {
            case 1:
                return 1;

            case 2:
                return 2;

            default:
                return 1;

        }
    }

    public static boolean isEnablePic() {
        return !SettingHelper.getSharedPreferences(getContext(), SettingActivity.DISABLE_DOWNLOAD_AVATAR_PIC, false);
    }

    public static boolean getEnableBigPic() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.SHOW_BIG_PIC, false);
    }

    public static boolean getEnableFetchMSG() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.ENABLE_FETCH_MSG, true);
    }

    // water mark setting
    public static boolean getEnableWaterMark() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.WATER_MARK_ENABLE, true);
    }

    /*
     * public static final String WATER_MARK_SCREEN_NAME = "water_mark_screen_name"; public static
     * final String WATER_MARK_WEIBO_ICON = "water_mark_weibo_icon"; public static final String
     * WATER_MARK_WEIBO_URL = "water_mark_weibo_url"; public static final String WATER_MARK_POS =
     * "water_mark_pos"; public static final String WATER_MARK_ENABLE = "water_mark_enable";
     */

    public static boolean isWaterMarkScreenNameShow() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.WATER_MARK_SCREEN_NAME, true);
    }

    public static boolean isWaterMarkWeiboICONShow() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.WATER_MARK_WEIBO_ICON, true);
    }

    public static boolean isWaterMarkWeiboURlShow() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.WATER_MARK_WEIBO_URL, true);
    }

    public static String getWaterMarkPos() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.WATER_MARK_POS, "1");
    }

    // end water mark setting

    public static boolean getEnableAutoRefresh() {
    	return false;
//        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.AUTO_REFRESH, false);
    }

    public static boolean getEnableBigAvatar() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.SHOW_BIG_AVATAR, false);
    }

    public static boolean getEnableSound() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.SOUND_OF_PULL_TO_FRESH, true)
                && Utility.isSystemRinger(getContext());
    }

    public static boolean disableFetchAtNight() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.DISABLE_FETCH_AT_NIGHT, true)
                && Utility.isSystemRinger(getContext());
    }

    public static String getFrequency() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.FREQUENCY, "1");
    }

    public static void setEnableBigPic(boolean value) {
        SettingHelper.setEditor(getContext(), SettingActivity.SHOW_BIG_PIC, value);
    }

    public static void setEnableBigAvatar(boolean value) {
        SettingHelper.setEditor(getContext(), SettingActivity.SHOW_BIG_AVATAR, value);
    }

    public static void setEnableFilter(boolean value) {
        SettingHelper.setEditor(getContext(), SettingActivity.FILTER, value);
    }

    public static void setEnableFetchMSG(boolean value) {
        SettingHelper.setEditor(getContext(), SettingActivity.ENABLE_FETCH_MSG, value);
    }

    public static void setEnableWaterMark(boolean value) {
        SettingHelper.setEditor(getContext(), SettingActivity.WATER_MARK_ENABLE, value);
    }

    public static boolean allowVibrate() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.ENABLE_VIBRATE, false);

    }

    public static boolean allowLed() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.ENABLE_LED, false);

    }

    public static String getRingtone() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.ENABLE_RINGTONE, "");

    }

    public static boolean allowFastScroll() {
        return false;
        // return SettingHelper
        // .getSharedPreferences(getContext(), SettingActivity.LIST_FAST_SCROLL,
        // true);

    }

    public static boolean allowMentionToMe() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.ENABLE_MENTION_TO_ME, true);

    }

    public static boolean allowCommentToMe() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.ENABLE_COMMENT_TO_ME, true);

    }

    public static boolean allowMentionCommentToMe() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.ENABLE_MENTION_COMMENT_TO_ME, true);

    }

    public static String getMsgCount() {
        String value = SettingHelper.getSharedPreferences(getContext(), SettingActivity.MSG_COUNT, "3");

        switch (Integer.valueOf(value)) {
            case 1:
                return String.valueOf(AppConfig.DEFAULT_MSG_COUNT_25);

            case 2:
                return String.valueOf(AppConfig.DEFAULT_MSG_COUNT_50);

            case 3:
                if (Utility.isConnected(getContext())) {
                    if (Utility.isWifi(getContext())) {
                        return String.valueOf(AppConfig.DEFAULT_MSG_COUNT_50);
                    } else {
                        return String.valueOf(AppConfig.DEFAULT_MSG_COUNT_25);
                    }
                }

        }
        return String.valueOf(AppConfig.DEFAULT_MSG_COUNT_25);

    }

    public static boolean disableHardwareAccelerated() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.DISABLE_HARDWARE_ACCELERATED, false);

    }

    public static int getUploadQuality() {
        String result = SettingHelper.getSharedPreferences(getContext(), SettingActivity.UPLOAD_PIC_QUALITY, "2");
        return Integer.valueOf(result);
    }

    public static void setDefaultSoftKeyBoardHeight(int height) {
        SettingHelper.setEditor(getContext(), "default_softkeyboard_height", height);
    }

    public static int getDefaultSoftKeyBoardHeight() {
        return SettingHelper.getSharedPreferences(getContext(), "default_softkeyboard_height", 400);
    }

    public static String getLastFoundWeiboAccountLink() {
        return SettingHelper.getSharedPreferences(getContext(), LAST_FOUND_WEIBO_ACCOUNT_LINK, "");
    }

    public static void setLastFoundWeiboAccountLink(String url) {
        SettingHelper.setEditor(getContext(), LAST_FOUND_WEIBO_ACCOUNT_LINK, url);
    }

    public static boolean isReadStyleEqualWeibo() {

        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.READ_STYLE, "1").equals("0");
    }

    public static boolean isWifiUnlimitedMsgCount() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.WIFI_UNLIMITED_MSG_COUNT, true);
    }

    public static boolean isWifiAutoDownloadPic() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.WIFI_AUTO_DOWNLOAD_PIC, true);
    }

    public static boolean allowInternalWebBrowser() {
        return false;// SettingHelper.getSharedPreferences(getContext(),
                     // SettingActivity.ENABLE_INTERNAL_WEB_BROWSER, true);

    }

    public static boolean allowClickToCloseGallery() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.ENABLE_CLICK_TO_CLOSE_GALLERY, true);

    }

    public static boolean isBlackMagicEnabled() {
        return true;//SettingHelper.getSharedPreferences(getContext(), BLACK_MAGIC, false);
    }

    public static void setBlackMagicEnabled() {
        SettingHelper.setEditor(getContext(), BLACK_MAGIC, true);
    }

    public static boolean isClickToTopTipFirstShow() {
        boolean result = SettingHelper.getSharedPreferences(getContext(), CLICK_TO_TOP_TIP, true);
        SettingHelper.setEditor(getContext(), CLICK_TO_TOP_TIP, false);
        return result;
    }

    public static boolean isFilterSinaAd() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.FILTER_SINA_AD, false);
    }

    public static boolean isUploadBigPic() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.UPLOAD_BIG_PIC, true);
    }

    public static boolean isNaviGationBarIm(){
    	return SettingHelper.getSharedPreferences(getContext(), SettingActivity.SETTING_PREF_NAVIGATIONBAR_MD, true);
    }
    
    public static String[] getHotWeiboSelected(){
    	return SettingHelper.getStringSetPreferences(getContext(), SettingActivity.HOT_WEIBO_LIST_KEY, R.array.hot_weibo_multi_select_value_def);
    }
    
    public static String[] getHotHuaTioSelected(){
    	return SettingHelper.getStringSetPreferences(getContext(), SettingActivity.HOT_HUATI_LIST_KEY, R.array.hot_huati_multi_select_value_def);
    }
}
