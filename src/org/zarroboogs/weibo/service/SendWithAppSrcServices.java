package org.zarroboogs.weibo.service;

import java.util.ArrayList;
import java.util.List;

import lib.org.zarroboogs.weibo.login.httpclient.SinaLoginHelper;
import lib.org.zarroboogs.weibo.login.httpclient.UploadHelper;
import lib.org.zarroboogs.weibo.login.httpclient.WaterMark;
import lib.org.zarroboogs.weibo.login.httpclient.UploadHelper.OnUpFilesListener;
import lib.org.zarroboogs.weibo.login.utils.Constaces;
import lib.org.zarroboogs.weibo.login.utils.LogTool;

import org.apache.http.HttpEntity;
import org.zarroboogs.devutils.http.AbsAsyncHttpService;
import org.zarroboogs.utils.SendBitmapWorkerTask;
import org.zarroboogs.utils.SendBitmapWorkerTask.OnCacheDoneListener;
import org.zarroboogs.weibo.GlobalContext;
import org.zarroboogs.weibo.R;
import org.zarroboogs.weibo.WebViewActivity;
import org.zarroboogs.weibo.bean.AccountBean;
import org.zarroboogs.weibo.bean.UserBean;
import org.zarroboogs.weibo.bean.WeiboWeiba;
import org.zarroboogs.weibo.db.task.AccountDBTask;
import org.zarroboogs.weibo.selectphoto.SendImgData;
import org.zarroboogs.weibo.setting.SettingUtils;
import org.zarroboogs.weibo.support.utils.BundleArgsConstants;

import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

public class SendWithAppSrcServices extends AbsAsyncHttpService {

	private SendImgData sendImgData = SendImgData.getInstance();
	private AccountBean mAccountBean;
	private WeiboWeiba mAppSrc = null;
	private String mTextContent;
    private SinaLoginHelper mSinaLoginHelper;
    
	private String TAG = "SendWithAppSrcServices";
	public static final String APP_SRC = "mAppSrc";
	public static final String TEXT_CONTENT = "TEXT_CONTENT";
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mSinaLoginHelper = new SinaLoginHelper();
		mAccountBean = GlobalContext.getInstance().getAccountBean();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		mAppSrc = (WeiboWeiba) intent.getExtras().getSerializable(APP_SRC);
		mTextContent = intent.getExtras().getString(TEXT_CONTENT);
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void startPicCacheAndSendWeibo() {
		sendImgData.clearReSizeImgs();
		ArrayList<String> send = sendImgData.getSendImgs();
		final int count = send.size();

		if (count > 0) {
		    for (int i = 0; i < send.size(); i++) {
		        SendBitmapWorkerTask sendBitmapWorkerTask = new SendBitmapWorkerTask(getApplicationContext(),
		                new OnCacheDoneListener() {
		                    @Override
		                    public void onCacheDone(String newFile) {
		                        // TODO Auto-generated method stub
		                        sendImgData.addReSizeImg(newFile);
		                        if (sendImgData.getReSizeImgs().size() == count) {
		                            sendWeibo(sendImgData, mTextContent);
		                        }
		                    }
		                });
		        sendBitmapWorkerTask.execute(send.get(i));
		    }
		} else {

		    sendWeibo(sendImgData, mTextContent);
		}
	}
	
    private void sendWeibo(SendImgData sendImgData, String text) {
        if (TextUtils.isEmpty(text)) {
            LogTool.D("sendWeibo    text is empty");
            text = getString(R.string.default_text_pic_weibo);
        }

        UserBean userBean = AccountDBTask.getUserBean(mAccountBean.getUid());
        String url = "";
        if (!TextUtils.isEmpty(userBean.getDomain())) {
            url = "weibo.com/" + userBean.getDomain();
        } else {
            url = "weibo.com/u/" + mAccountBean.getUid();
        }
        WaterMark mark = new WaterMark(mAccountBean.getUsernick(), url);

        executeSendWeibo( mark, mAppSrc.getCode(), text, sendImgData.getReSizeImgs());
    }
    
    public void executeSendWeibo(WaterMark mark, final String weiboCode, final String text,
            List<String> pics) {
        dosend(mark, weiboCode, text, pics);
    }

    private void dosend(WaterMark mark, final String weiboCode, final String text, List<String> pics) {
        if (pics == null || pics.isEmpty()) {
            sendWeiboWidthPids(weiboCode, text, null);
        } else {
            UploadHelper mUploadHelper = new UploadHelper(getApplicationContext(), getAsyncHttpClient());
            mUploadHelper.uploadFiles(buildMark(mark), pics, new OnUpFilesListener() {

                @Override
                public void onUpSuccess(String pids) {
                    sendWeiboWidthPids(weiboCode, text, pids);
                }

                @Override
                public void onUpLoadFailed() {
                    // TODO Auto-generated method stub
                    startWebLogin();
                }
            }, getCookieIfHave());
        }
    }
    
    public void startWebLogin() {
        Intent intent = new Intent();
        intent.putExtra(BundleArgsConstants.ACCOUNT_EXTRA, mAccountBean);
        intent.setClass(SendWithAppSrcServices.this, WebViewActivity.class);
        startActivity(intent);
    }
    
    /**
     * @param weiboCode "ZwpYj"
     * @param pid
     */
    protected void sendWeiboWidthPids(String weiboCode, String text, String pids) {
    	String cookie = getCookieIfHave();
		LogTool.D(TAG  + "sendWeiboWidthPids Cookie:     " + cookie);
        HttpEntity sendEntity = mSinaLoginHelper.sendWeiboEntity(weiboCode, text, cookie, pids);
        asyncHttpPost(Constaces.ADDBLOGURL, mSinaLoginHelper.sendWeiboHeaders(weiboCode, cookie), sendEntity, "application/x-www-form-urlencoded");

    }
    
	private String getCookieIfHave() {
		String cookieInDB = GlobalContext.getInstance().getAccountBean().getCookieInDB();
		if (!TextUtils.isEmpty(cookieInDB)) {
			return cookieInDB;
		}
		return "";
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
    
	@Override
	public void onGetFailed(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetSuccess(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPostFailed(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPostSuccess(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRequestStart() {
		// TODO Auto-generated method stub
		
	}

}
