package org.zarroboogs.weibo;

import lib.org.zarroboogs.weibo.login.httpclient.AssertLoader;

import org.zarroboogs.devutils.DevLog;
import org.zarroboogs.injectjs.InjectJS;
import org.zarroboogs.injectjs.JSCallJavaInterface;
import org.zarroboogs.injectjs.InjectJS.OnLoadListener;
import org.zarroboogs.utils.PatternUtils;
import org.zarroboogs.weibo.bean.AccountBean;
import org.zarroboogs.weibo.db.AccountDatabaseManager;
import org.zarroboogs.weibo.db.table.AccountTable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class JSAutoLogin {

	private Context mContext;
	private WebView mWebView;
	private InjectJS mInjectJS;
	private AccountBean mAccountBean;
	private WeiboWebViewClient mWeiboWebViewClient;

	private boolean isExecuted = false;
	private AutoLogInListener mListener;
	
	public static interface AutoLogInListener{
		public void onAutoLonin(boolean result);
	}
	
	public void setAutoLogInListener(AutoLogInListener l){
		this.mListener = l;
	}
	
	public JSAutoLogin(Context context,AccountBean ab) {
		this.mContext = context;
		this.mAccountBean = ab;
		
		this.mWebView = new WebView(mContext);
        mWeiboWebViewClient = new WeiboWebViewClient();
        mWebView.setWebViewClient(mWeiboWebViewClient);
        
		mInjectJS = new InjectJS(mWebView);

		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setBuiltInZoomControls(true);
		webSettings.setSaveFormData(true);
		webSettings.setSupportZoom(true);
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
	}
	
	
	
	static final String REDIRECT = "http://widget.weibo.com/dialog/PublishMobile.php";
    static String url = "https://passport.weibo.cn/signin/login?entry=mweibo&res=wel&wm=3349&r=http%3A%2F%2Fwidget.weibo.com%2Fdialog%2FPublishMobile.php%3Fbutton%3Dpublic";

    
    class JSCallBack extends JSCallJavaInterface{

		@Override
		public void onJSCallJava(String... arg0) {
			// TODO Auto-generated method stub
			DevLog.printLog("onJSCallJava Uname", "" + arg0[0]);
			DevLog.printLog("onJSCallJava Upassword", "" + arg0[1]);
		}
    	
    }
    
    public void exejs(){
        mInjectJS.addJSCallJavaInterface(new JSCallBack(), "loginName.value","loginPassword.value");
        mInjectJS.replaceDocument("<a href=\"javascript:;\" class=\"btn btnRed\" id = \"loginAction\">登录</a>", 
        		"<a href=\"javascript:doAutoLogIn();\" class=\"btn btnRed\" id = \"loginAction\">登录</a>");
        mInjectJS.removeDocument("<a href=\"javascript:history.go(-1);\" class=\"close\">关闭</a>");
        mInjectJS.removeDocument("<a href=\"http://m.weibo.cn/reg/index?&vt=4&wm=3349&backURL=http%3A%2F%2Fwidget.weibo.com%2Fdialog%2FPublishMobile.php%3Fbutton%3Dpublic\">注册帐号</a><a href=\"http://m.weibo.cn/setting/forgotpwd?vt=4\">忘记密码</a>");
        mInjectJS.removeDocument("<p class=\"label\"><a href=\"https://passport.weibo.cn/signin/other?r=http%3A%2F%2Fwidget.weibo.com%2Fdialog%2FPublishMobile.php%3Fbutton%3Dpublic\">使用其他方式登录</a></p>");
        mInjectJS.injectUrl(url, new AssertLoader(mContext).loadJs("inject.js"), "gb2312");
        

        mInjectJS.setOnLoadListener(new OnLoadListener() {
			
			@Override
			public void onLoad() {
				// TODO Auto-generated method stub
				if (mAccountBean != null && !TextUtils.isEmpty(mAccountBean.getUname()) && !TextUtils.isEmpty(mAccountBean.getPwd())) {
					mInjectJS.exeJsFunctionWithParam("fillAccount", mAccountBean.getUname(),mAccountBean.getPwd());
	            	if (!isExecuted) {
	            		mInjectJS.exeJsFunction("doAutoLogIn()");
	            		isExecuted = true;
					}
				}
			}
		});
    }
    
    private class WeiboWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            if (url.startsWith(REDIRECT)) {
                view.stopLoading();

                CookieManager cookieManager = CookieManager.getInstance();

                String cookie = cookieManager.getCookie(url);
                String pubCookie = cookieManager.getCookie("http://widget.weibo.com/dialog/PublishMobile.php");
                String longInCookie = cookieManager.getCookie("http://widget.weibo.com/dialog/LoginMobile.php");

                Log.d("Weibo-CookieStr", cookie + " \r\n\r\n PubCookie:" + pubCookie + "  \r\n\r\r LogInCookie:" + longInCookie);
                String uid = "";
                String uname = "";
                AccountDatabaseManager manager = new AccountDatabaseManager(mContext);
                if (true) {
                    String[] cookies = cookie.split("; ");
                    for (String string : cookies) {
                        String oneLine = Uri.decode(Uri.decode(string));
                        String uidtmp = PatternUtils.macthUID(oneLine);
                        if (!TextUtils.isEmpty(uidtmp)) {
                            uid = uidtmp;
                        }
                        uname = PatternUtils.macthUname(oneLine);
                        if (!TextUtils.isEmpty(uname)) {
                            manager.updateAccount(AccountTable.ACCOUNT_TABLE, uid, AccountTable.USER_NAME, uname);
                        }
                    }
                }

                Log.d("Weibo-Cookie", "after for : " + uid);
                if (uid.equals(mAccountBean.getUid())) {
                	if (mListener != null) {
						mListener.onAutoLonin(true);
					}
                    manager.updateAccount(AccountTable.ACCOUNT_TABLE, uid, AccountTable.COOKIE, pubCookie);
                } else if (!TextUtils.isEmpty(uid)) {
                    mWebView.loadUrl(url);
                }
                
                return;
            }

            super.onPageStarted(view, url, favicon);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

    }
}