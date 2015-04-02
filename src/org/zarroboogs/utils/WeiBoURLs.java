
package org.zarroboogs.utils;

import org.zarroboogs.devutils.DevLog;
import org.zarroboogs.senior.sdk.SeniorParams;
import org.zarroboogs.senior.sdk.SeniorUrl;

import android.os.Build;

public class WeiBoURLs {
	
	public static final String GSID = "http://appsrc.sinaapp.com/gsid.txt";

	public static String passwordUrl = "http://m.weibo.cn/setting/forgotpwd?vt=4&wm=ig_0001_index";
	public static String regUrl = "http://weibo.cn/dpool/ttt/h5/reg.php";

    // base url
    private static final String URL_SINA_WEIBO = "https://api.weibo.com/2/";

    // login
    public static final String UID = URL_SINA_WEIBO + "account/get_uid.json";
    
    // main timeline
    public static final String FRIENDS_TIMELINE = URL_SINA_WEIBO + "statuses/friends_timeline.json";
    public static final String COMMENTS_MENTIONS_TIMELINE = URL_SINA_WEIBO + "comments/mentions.json";
    public static final String STATUSES_MENTIONS_TIMELINE = URL_SINA_WEIBO + "statuses/mentions.json";
    public static final String COMMENTS_TO_ME_TIMELINE = URL_SINA_WEIBO + "comments/to_me.json";
    public static final String COMMENTS_BY_ME_TIMELINE = URL_SINA_WEIBO + "comments/by_me.json";
    public static final String BILATERAL_TIMELINE = URL_SINA_WEIBO + "statuses/bilateral_timeline.json";
    public static final String TIMELINE_RE_CMT_COUNT = URL_SINA_WEIBO + "statuses/count.json";

    // group timeline
    public static final String FRIENDSGROUP_INFO = URL_SINA_WEIBO + "friendships/groups.json";
    public static final String FRIENDSGROUP_TIMELINE = URL_SINA_WEIBO + "friendships/groups/timeline.json";

    // general timeline
    public static final String COMMENTS_TIMELINE_BY_MSGID = URL_SINA_WEIBO + "comments/show.json";
    public static final String REPOSTS_TIMELINE_BY_MSGID = URL_SINA_WEIBO + "statuses/repost_timeline.json";

    // user profile
    public static final String STATUSES_TIMELINE_BY_ID = URL_SINA_WEIBO + "statuses/user_timeline.json";
    public static final String USER_SHOW = URL_SINA_WEIBO + "users/show.json";
    public static final String USER_DOMAIN_SHOW = URL_SINA_WEIBO + "users/domain_show.json";

    // browser
    public static final String STATUSES_SHOW = URL_SINA_WEIBO + "statuses/show.json";

    // short url
    public static final String SHORT_URL_SHARE_COUNT = URL_SINA_WEIBO + "short_url/share/counts.json";
    public static final String SHORT_URL_SHARE_TIMELINE = URL_SINA_WEIBO + "short_url/share/statuses.json";

    public static final String MID_TO_ID = URL_SINA_WEIBO + "statuses/queryid.json";

    // send weibo
    public static final String STATUSES_UPDATE = URL_SINA_WEIBO + "statuses/update.json";
    public static final String STATUSES_UPLOAD = URL_SINA_WEIBO + "statuses/upload.json";
    public static final String STATUSES_DESTROY = URL_SINA_WEIBO + "statuses/destroy.json";

    public static final String REPOST_CREATE = URL_SINA_WEIBO + "statuses/repost.json";

    public static final String COMMENT_CREATE = URL_SINA_WEIBO + "comments/create.json";
    public static final String COMMENT_DESTROY = URL_SINA_WEIBO + "comments/destroy.json";
    public static final String COMMENT_REPLY = URL_SINA_WEIBO + "comments/reply.json";

    // favourite
    public static final String MYFAV_LIST = URL_SINA_WEIBO + "favorites.json";

    public static final String FAV_CREATE = URL_SINA_WEIBO + "favorites/create.json";
    public static final String FAV_DESTROY = URL_SINA_WEIBO + "favorites/destroy.json";

    // relationship
    public static final String FRIENDS_LIST_BYID = URL_SINA_WEIBO + "friendships/friends.json";
    public static final String FOLLOWERS_LIST_BYID = URL_SINA_WEIBO + "friendships/followers.json";

    public static final String FRIENDSHIPS_CREATE = URL_SINA_WEIBO + "friendships/create.json";
    public static final String FRIENDSHIPS_DESTROY = URL_SINA_WEIBO + "friendships/destroy.json";
    public static final String FRIENDSHIPS_FOLLOWERS_DESTROY = URL_SINA_WEIBO + "friendships/followers/destroy.json";

    // gps location info
    public static final String GOOGLELOCATION = "http://maps.google.com/maps/api/geocode/json";

    // search
    public static final String AT_USER = URL_SINA_WEIBO + "search/suggestions/at_users.json";
    public static final String TOPIC_SEARCH = URL_SINA_WEIBO + "search/topics.json";

    // topic
    public static final String TOPIC_USER_LIST = URL_SINA_WEIBO + "trends.json";
    public static final String TOPIC_FOLLOW = URL_SINA_WEIBO + "trends/follow.json";
    public static final String TOPIC_DESTROY = URL_SINA_WEIBO + "trends/destroy.json";
    public static final String TOPIC_RELATIONSHIP = URL_SINA_WEIBO + "trends/is_follow.json";

    // unread messages
    public static final String UNREAD_COUNT = URL_SINA_WEIBO + "remind/unread_count.json";
    public static final String UNREAD_CLEAR = URL_SINA_WEIBO + "remind/set_count.json";

    // remark
    public static final String REMARK_UPDATE = URL_SINA_WEIBO + "friendships/remark/update.json";

    public static final String TAGS = URL_SINA_WEIBO + "tags.json";

    public static final String EMOTIONS = URL_SINA_WEIBO + "emotions.json";

    // group
    public static final String GROUP_MEMBER_LIST = URL_SINA_WEIBO + "friendships/groups/listed.json";
    public static final String GROUP_MEMBER_ADD = URL_SINA_WEIBO + "friendships/groups/members/add.json";
    public static final String GROUP_MEMBER_DESTROY = URL_SINA_WEIBO + "friendships/groups/members/destroy.json";

    public static final String GROUP_CREATE = URL_SINA_WEIBO + "friendships/groups/create.json";
    public static final String GROUP_DESTROY = URL_SINA_WEIBO + "friendships/groups/destroy.json";
    public static final String GROUP_UPDATE = URL_SINA_WEIBO + "friendships/groups/update.json";

    // nearby
    public static final String NEARBY_USER = URL_SINA_WEIBO + "place/nearby/users.json";
    public static final String NEARBY_STATUS = URL_SINA_WEIBO + "place/nearby_timeline.json";

    // map
    public static final String STATIC_MAP = URL_SINA_WEIBO + "location/base/get_map_image.json";

    public static final String BAIDU_GEO_CODER_MAP = "http://api.map.baidu.com/geocoder/v2/?ak=AAacde37a912803101fe91fb2de38c30&coordtype=wgs84ll&output=json&pois=0&location=%f,%f";

    /**
     * black magic
     */

    // oauth2 and refresh token
    public static final String OAUTH2_ACCESS_TOKEN = URL_SINA_WEIBO + "oauth2/access_token";

    // search
    public static final String STATUSES_SEARCH = URL_SINA_WEIBO + "search/statuses.json";
    public static final String USERS_SEARCH = URL_SINA_WEIBO + "search/users.json";

    // direct message
    public static final String DM_RECEIVED = URL_SINA_WEIBO + "direct_messages.json";
    public static final String DM_SENT = URL_SINA_WEIBO + "direct_messages/new.json";
    public static final String DM_USERLIST = URL_SINA_WEIBO + "direct_messages/user_list.json";
    public static final String DM_CONVERSATION = URL_SINA_WEIBO + "direct_messages/conversation.json";
    public static final String DM_CREATE = URL_SINA_WEIBO + "direct_messages/new.json";
    public static final String DM_DESTROY = URL_SINA_WEIBO + "direct_messages/destroy.json";
    public static final String DM_BATH_DESTROY = URL_SINA_WEIBO + "direct_messages/destroy_batch";

    // edit my profile
    public static final String MYPROFILE_EDIT = URL_SINA_WEIBO + "account/profile/basic_update.json";
    public static final String AVATAR_UPLOAD = URL_SINA_WEIBO + "account/avatar/upload.json";
    
    // heart
    // give heart
    public static final String GIVE_HEART = URL_SINA_WEIBO + "attitudes/create.json";
    // delete heart
    public static final String DELETE_HEART = URL_SINA_WEIBO + "attitudes/destroy.json";
    // show heart
    public static final String SHOW_HEART = URL_SINA_WEIBO + "attitudes/show.json";
    
    public static String buildUA() {
		String str = "%s-%s__weibo__5.1.2__android__android%s";
		String result = String.format(str, new Object[]{Build.MANUFACTURER, Build.MODEL, Build.VERSION.RELEASE});
		DevLog.printLog("buildUA", "" + result);
		return result;
	}
    
    public static String like(String gsid, String id) {
		String url = "http://api.weibo.cn/2/like/set_like?sourcetype=feed&uicode=10000001&fromlog=100012294141594&featurecode=10000001&c=android&i=8764dac&s=033439fa&"
				+ "id=" + id
				+ "&ua=" + buildUA()
				+ "&wm=5311_5000&ext=rid%3A0_0_2598665452757778956&v_f=2&from=1051295010&"
				+ "gsid=" + gsid
				+ "&lang=zh_CN&skin=default&type=0&oldwm=5311_5000";
		return url;
	}
    
    public static String unLike(String gsid, String id){
    	String url = "http://api.weibo.cn/2/like/cancel_like?sourcetype=feed&uicode=10000001&fromlog=100012294141594&featurecode=10000001&c=android&i=8764dac&s=033439fa&"
    			+ "id=" + id
    			+ "&ua=" + buildUA()
    			+ "&wm=5311_5000&ext=rid%3A0_0_2598665452757778956&v_f=2&from=1051295010&"
    			+ "gsid=" + gsid
    			+ "&lang=zh_CN&skin=default&type=0&oldwm=5311_5000";
    	return url;
    }
    
	public static String hotWeiboMeiTu(String gsid, int page) {
		return SeniorUrl.hotWeiboApi(SeniorParams.GSID_Value, "2899", page);
	}

	// lvxing
	public static String hotWeiboTravel(String gsid, int page) {
		return SeniorUrl.hotWeiboApi(SeniorParams.GSID_Value, "2599", page);
	}

	// 科技
	public static String hotWeiboKeji(String gsid, int page) {
		return SeniorUrl.hotWeiboApi(SeniorParams.GSID_Value, "2099", page);
	}

	// 美女
	public static String hotWeiboMeiNv(String gsid, int page) {
		return SeniorUrl.hotWeiboApi(SeniorParams.GSID_Value, "2299", page);
	}

	// 萌宠
	public static String hotWeiboPet(String gsid, int page) {
		return SeniorUrl.hotWeiboApi(SeniorParams.GSID_Value, "2799", page);
	}

	// 囧人糗事
	public static String hotWeioJiushi(String gsid, int page) {
		return SeniorUrl.hotWeiboApi(SeniorParams.GSID_Value, "6199", page);
	}

	// 笑话
	public static String hotWeiboXiaoHua(String gsid, int page) {
		return SeniorUrl.hotWeiboApi(SeniorParams.GSID_Value, "1899", page);
	}

	// 爆料
	public static String hotWeiboBaoLiao(String gsid, int page) {
		return SeniorUrl.hotWeiboApi(SeniorParams.GSID_Value, "1799", page);
	}

	// 视频
	public static String hotWeiboVideo(String gsid, int page) {
		return SeniorUrl.hotWeiboApi(SeniorParams.GSID_Value, "1199", page);
	}

	// 神最右
	public static String hotWeiboZuiyou(String gsid, int page) {
		return SeniorUrl.hotWeiboApi(SeniorParams.GSID_Value, "6399", page);
	}

	public static String hotWeiboYestoday(String gsid, int page) {
		return SeniorUrl.hotWeiboApi(SeniorParams.GSID_Value, "8899", page);
	}

	public static String hotWeiboQianTian(String gsid, int page) {
		return SeniorUrl.hotWeiboApi(SeniorParams.GSID_Value, "8799", page);
	}

	// hack 小时热门微博
	public static String hotWeiboUrl(String gsid, int page) {
		return SeniorUrl.hotWeiboApi(SeniorParams.GSID_Value, "8999", page);
	}

	// 消费数码
	public static String hotHuatiDigit(String gsid, int page) {
		return SeniorUrl.hotHuaTiApi(gsid, "131", page);
	}

	// it互联网
	public static String hotHuatiIT(String gsid, int page) {
		return SeniorUrl.hotHuaTiApi(gsid, "138", page);
	}

	// 幽默搞笑
	public static String hotHuatiHumor(String gsid, int page) {
		return SeniorUrl.hotHuaTiApi(gsid, "140", page);
	}

	// 动物萌宠
	public static String hotHuatiDog(String gsid, int page) {
		return SeniorUrl.hotHuaTiApi(gsid, "128", page);
	}

	// 创意征集
	public static String hotHuatiOriginality(String gsid, int page) {
		return SeniorUrl.hotHuaTiApi(gsid, "9", page);
	}

	// 摄影
	public static String hotHuaTiShot(String gsid, int page) {
		return SeniorUrl.hotHuaTiApi(gsid, "123", page);
	}

	// 电影
	public static String hotHuaTiFilm(String gsid, int page) {
		return SeniorUrl.hotHuaTiApi(gsid, "100", page);
	}

	public static String hotHuaTiOneHouOur(String gsid, int page) {
		return SeniorUrl.hotHuaTiApi(gsid, "-1", page);
	}
}
