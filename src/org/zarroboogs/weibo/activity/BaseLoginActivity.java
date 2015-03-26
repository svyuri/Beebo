
package org.zarroboogs.weibo.activity;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import lib.org.zarroboogs.weibo.login.httpclient.SinaLoginHelper;
import lib.org.zarroboogs.weibo.login.httpclient.UploadHelper;
import lib.org.zarroboogs.weibo.login.httpclient.UploadHelper.OnUpFilesListener;
import lib.org.zarroboogs.weibo.login.httpclient.WaterMark;
import lib.org.zarroboogs.weibo.login.javabean.RequestResultParser;
import lib.org.zarroboogs.weibo.login.javabean.SendResultBean;
import lib.org.zarroboogs.weibo.login.utils.Constaces;
import lib.org.zarroboogs.weibo.login.utils.LogTool;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.zarroboogs.weibo.GlobalContext;
import org.zarroboogs.weibo.R;
import org.zarroboogs.weibo.WebViewActivity;
import org.zarroboogs.weibo.bean.WeiboWeiba;
import org.zarroboogs.weibo.setting.SettingUtils;
import org.zarroboogs.weibo.support.utils.BundleArgsConstants;

import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.ResponseHandlerInterface;

public class BaseLoginActivity extends SharedPreferenceActivity {
    private static final String TAG = "Beebo_Login: ";
    private SinaLoginHelper mSinaLoginHelper;

    private RequestResultParser mRequestResultParser;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSinaLoginHelper = new SinaLoginHelper();

        mRequestResultParser = new RequestResultParser();

        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getString(R.string.send_wei_ing));
        mDialog.setCancelable(false);

        Builder builder = new Builder(BaseLoginActivity.this);

    }

    public void showDialogForWeiBo() {
        if (!mDialog.isShowing()) {
            mDialog.show();
        }

    }

    public void hideDialogForWeiBo() {
        mDialog.cancel();
        mDialog.hide();
    }

    public RequestResultParser getRequestResultParser() {
        return mRequestResultParser;
    }

    public void executeSendWeibo(WaterMark mark, final String weiboCode, final String text,
            List<String> pics) {
        dosend(mark, weiboCode, text, pics);

    }

    private void dosend(WaterMark mark, final String weiboCode, final String text, List<String> pics) {
        if (pics == null || pics.isEmpty()) {
            sendWeiboWidthPids(weiboCode, text, null);
            // sendWeiboWidthPids("ZwpYj", "Test: " + SystemClock.uptimeMillis() + "", null);
            LogTool.D(TAG + "uploadFile     Not Upload");
        } else {
            LogTool.D(TAG + "uploadFile    upload");
            UploadHelper mUploadHelper = new UploadHelper(getApplicationContext(), getAsyncHttpClient());
            mUploadHelper.uploadFiles(buildMark(mark), pics, new OnUpFilesListener() {

                @Override
                public void onUpSuccess(String pids) {
                    LogTool.D(TAG + " UploadPic:  [onUpSuccess] " + pids);
                    sendWeiboWidthPids(weiboCode, text, pids);
                }

                @Override
                public void onUpLoadFailed() {
                    // TODO Auto-generated method stub
                    startWebLogin();
                    LogTool.D(TAG + " UploadPic:  [onUpLoadFailed] doPreLogin");
                }
            }, getCookieIfHave());
        }
    }

    public String buildMark(WaterMark mark) {
        if (SettingUtils.getEnableWaterMark()) {
            String markpos = SettingUtils.getWaterMarkPos();
            String logo = SettingUtils.isWaterMarkWeiboICONShow() ? "1" : "0";
            String nick = SettingUtils.isWaterMarkScreenNameShow() ? "%40" + mark.getNick() : "";
            String url = SettingUtils.isWaterMarkWeiboURlShow() ? mark.getUrl() : "";
            return "&marks=1&markpos=" + markpos + "&logo=" + logo + "&nick=" + nick + "&url=" + url;
        } else {
            return "&marks=0";
        }
    }

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
	            case Constaces.MSG_ENCODE_PWD_ERROR:{
	            	if (mLoginHandler != null) {
		            	((AsyncHttpResponseHandler)mLoginHandler).onFailure(0, null, null, null);
					}
	            	break;
	            }
                case Constaces.MSG_AFTER_LOGIN_DONE: {
                    doLogin();
                    break;
                }
                default:
                    break;
            }
        }
    };

    protected void sendWeibo(String pid) {
    	String cookie = getCookieIfHave();
		LogTool.D(TAG + "sendWeibo Cookie:     " + cookie);
        HttpEntity sendEntity = mSinaLoginHelper.sendWeiboEntity("ZwpYj", SystemClock.uptimeMillis() + "", cookie, pid);
        getAsyncHttpClient().post(getApplicationContext(), Constaces.ADDBLOGURL, mSinaLoginHelper.sendWeiboHeaders("ZwpYj", cookie),
                sendEntity,
                "application/x-www-form-urlencoded", new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                        SendResultBean sendResultBean = mRequestResultParser.parse(responseBody, SendResultBean.class);
                        LogTool.D("sendWeibo   onSuccess" + sendResultBean.getMsg());
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        LogTool.D("sendWeibo   onFailure" + error.getLocalizedMessage());
                    }
                });
    }

    /**
     * @param weiboCode "ZwpYj"
     * @param pid
     */
    protected void sendWeiboWidthPids(String weiboCode, String text, String pids) {
    	String cookie = getCookieIfHave();
		LogTool.D(TAG + "sendWeiboWidthPids Cookie:     " + cookie);
        HttpEntity sendEntity = mSinaLoginHelper.sendWeiboEntity(weiboCode, text, cookie, pids);
        getAsyncHttpClient().post(getApplicationContext(), Constaces.ADDBLOGURL, mSinaLoginHelper.sendWeiboHeaders(weiboCode, cookie),
                sendEntity,
                "application/x-www-form-urlencoded", this.mAutoSendWeiboListener);
    }

	private String getCookieIfHave() {
		String cookieInDB = GlobalContext.getInstance().getAccountBean().getCookieInDB();
		if (!TextUtils.isEmpty(cookieInDB)) {
			return cookieInDB;
		}
		return "";
	}

    private ResponseHandlerInterface mAutoSendWeiboListener;

    public void setAutoSendWeiboListener(ResponseHandlerInterface rhi) {
        this.mAutoSendWeiboListener = rhi;
    }

    public void repostWeibo(String app_src, String content, String cookie, String mid, boolean isComment) {
    	cookie = getCookieIfHave();
		
        List<Header> headerList = new ArrayList<Header>();
        headerList.add(new BasicHeader("Accept", "*/*"));
        headerList.add(new BasicHeader("Accept-Encoding", "gzip, deflate"));
        headerList.add(new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4"));
        headerList.add(new BasicHeader("Connection", "keep-alive"));
        headerList.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headerList.add(new BasicHeader("Host", "widget.weibo.com"));
        headerList.add(new BasicHeader("Origin", "http://widget.weibo.com"));
        headerList.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
        headerList.add(new BasicHeader("Referer",
                "http://widget.weibo.com/dialog/publish.php?button=forward&language=zh_cn&mid=" + mid +
                        "&app_src=" + app_src + "&refer=1&rnd=14128245"));
        headerList.add(new BasicHeader("User-Agent", Constaces.User_Agent));
        if (!TextUtils.isEmpty(cookie)) {
            headerList.add(new BasicHeader("Cookie", cookie));
		}

        
        Header[] repostHeaders = new Header[headerList.size()];
        headerList.toArray(repostHeaders);

        List<NameValuePair> nvs = new ArrayList<NameValuePair>();
        LogTool.D("RepostWeiboMainActivity : repost-content: " + content);
        nvs.add(new BasicNameValuePair("content", content));
        nvs.add(new BasicNameValuePair("visible", "0"));
        nvs.add(new BasicNameValuePair("refer", ""));

        nvs.add(new BasicNameValuePair("app_src", app_src));
        nvs.add(new BasicNameValuePair("mid", mid));
        nvs.add(new BasicNameValuePair("return_type", "2"));

        nvs.add(new BasicNameValuePair("vsrc", "publish_web"));
        nvs.add(new BasicNameValuePair("wsrc", "app_publish"));
        nvs.add(new BasicNameValuePair("ext", "login=>1;url=>"));
        nvs.add(new BasicNameValuePair("html_type", "2"));
        if (isComment) {
        	nvs.add(new BasicNameValuePair("is_comment", "1"));
		}else {
			nvs.add(new BasicNameValuePair("is_comment", "0"));
		}
        
        nvs.add(new BasicNameValuePair("_t", "0"));

        UrlEncodedFormEntity repostEntity = null;
        try {
            repostEntity = new UrlEncodedFormEntity(nvs, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        getAsyncHttpClient().post(getApplicationContext(), Constaces.REPOST_WEIBO, repostHeaders, repostEntity,
                "application/x-www-form-urlencoded", mAutoRepostHandler);
    }

    private ResponseHandlerInterface mAutoRepostHandler;

    public void setAutoRepostWeiboListener(ResponseHandlerInterface rhi) {
        this.mAutoRepostHandler = rhi;
    }

    private void doLogin() {
        getAsyncHttpClient().get(mRequestResultParser.getUserPageUrl(), mLoginHandler);
    }

    private ResponseHandlerInterface mLoginHandler;;

    public void setAutoLogInLoginListener(ResponseHandlerInterface rhi) {
        this.mLoginHandler = rhi;
    }


    public void startWebLogin() {
        Intent intent = new Intent();
        intent.putExtra(BundleArgsConstants.ACCOUNT_EXTRA, mAccountBean);
        intent.setClass(BaseLoginActivity.this, WebViewActivity.class);
        startActivity(intent);
    }
    public interface OnFetchAppSrcListener {
        public void onStart();

        public void onSuccess(List<WeiboWeiba> appsrcs);

        public void onFailure();
    }

    private OnFetchAppSrcListener mFetchAppSrcListener;

    protected void fetchWeiBa(OnFetchAppSrcListener listener) {
        this.mFetchAppSrcListener = listener;
        if (mFetchAppSrcListener != null) {
            mFetchAppSrcListener.onStart();
        }

        String url = "http://appsrc.sinaapp.com/appsrc_v2_0.txt";//"http://appsrc.sinaapp.com/";
        Header[] srcHeaders = new Header[] {
                new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"),
                new BasicHeader("Accept-Encoding", "gzip,deflate,sdch"),
                new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4"),
                new BasicHeader("Cache-Control", "no-cache"),
                new BasicHeader("Connection", "keep-alive"),
                new BasicHeader("Host", "appsrc.sinaapp.com"),
                new BasicHeader("Pragma", "no-cache"),
                new BasicHeader("User-Agent", Constaces.User_Agent),
        };
        getAsyncHttpClient().get(getApplicationContext(), url, srcHeaders, null, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String resp = new String(responseBody);
                String jsonString = resp;//.split("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">")[1];
                Gson gson = new Gson();
                
                Type listType = new TypeToken<List<WeiboWeiba>>(){}.getType();
                List<WeiboWeiba> mAppsrc = (List<WeiboWeiba>) gson.fromJson(jsonString, listType);
                
//                List<WeiboWeiba> mAppsrc = Arrays.asList(gson.fromJson(jsonString,WeiboWeiba.class));

                if (mFetchAppSrcListener != null) {
                    mFetchAppSrcListener.onSuccess(mAppsrc);
                }
                hideDialogForWeiBo();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (mFetchAppSrcListener != null) {
                    mFetchAppSrcListener.onFailure();
                }
            }
        });
    }
}
