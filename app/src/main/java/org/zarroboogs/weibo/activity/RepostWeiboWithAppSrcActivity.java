
package org.zarroboogs.weibo.activity;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import org.zarroboogs.keyboardlayout.smilepicker.SmileyPicker;
import org.zarroboogs.utils.Constants;
import org.zarroboogs.utils.Utility;
import org.zarroboogs.utils.WeiBaNetUtils;
import org.zarroboogs.weibo.ChangeWeibaAdapter;
import org.zarroboogs.weibo.BeeboApplication;
import org.zarroboogs.weibo.R;
import org.zarroboogs.weibo.WebViewActivity;
import org.zarroboogs.weibo.bean.AccountBean;
import org.zarroboogs.weibo.bean.MessageBean;
import org.zarroboogs.weibo.bean.RepostDraftBean;
import org.zarroboogs.weibo.bean.WeiboWeiba;
import org.zarroboogs.weibo.dao.RepostNewMsgDao;
import org.zarroboogs.weibo.db.AppsrcDatabaseManager;
import org.zarroboogs.weibo.selectphoto.ImgFileListActivity;
import org.zarroboogs.weibo.service.RepostWithAppSrcServices;
import org.zarroboogs.weibo.service.SendRepostService;
import org.zarroboogs.weibo.support.utils.SmileyPickerUtility;
import org.zarroboogs.weibo.support.utils.ViewUtility;
import org.zarroboogs.weibo.widget.pulltorefresh.PullToRefreshBase;
import org.zarroboogs.weibo.widget.pulltorefresh.PullToRefreshListView;
import org.zarroboogs.weibo.widget.pulltorefresh.PullToRefreshBase.OnRefreshListener;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class RepostWeiboWithAppSrcActivity extends BaseLoginActivity implements  OnClickListener, OnGlobalLayoutListener, OnItemClickListener {

    public static final int AT_USER = 0x1000;
    public static final String TAG = "RepostWeiboMainActivity ";

    private SmileyPicker mSmileyPicker = null;
    private MessageBean msg;

    private InputMethodManager imm = null;
    private MaterialEditText mEditText;
    private RelativeLayout mRootView;

    private RelativeLayout editTextLayout;
    private ImageButton mSelectPhoto;
    private ImageButton mSendBtn;
    private ImageButton smileButton;
    private ImageButton mTopicBtn;
    private ImageButton mAtButton;

    private Button appSrcBtn;
    private AccountBean mAccountBean;
    private ScrollView mEditPicScrollView;

    private TextView weiTextCountTV;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;

    private AppsrcDatabaseManager mDBmanager = null;
    private PullToRefreshListView listView;
    private ChangeWeibaAdapter listAdapter;
    
    private CheckBox mComments;

    public static Intent startBecauseSendFailed(Context context, AccountBean accountBean, String content,
            MessageBean oriMsg, RepostDraftBean repostDraftBean,
            String failedReason) {
        Intent intent = new Intent(context, RepostWeiboWithAppSrcActivity.class);
        intent.setAction(WriteRepostActivity.ACTION_SEND_FAILED);
        intent.putExtra(Constants.ACCOUNT, accountBean);
        intent.putExtra("content", content);
        intent.putExtra("oriMsg", oriMsg);
        intent.putExtra("failedReason", failedReason);
        intent.putExtra("repostDraftBean", repostDraftBean);
        return intent;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        setContentView(R.layout.repost_weibo_with_appsrc_activity_layout);

        mComments = (CheckBox) findViewById(R.id.repostCommentsCheck);
        
        mDrawerLayout = (DrawerLayout) findViewById(R.id.writeWeiboDrawerL);
        mToolbar = (Toolbar) findViewById(R.id.writeWeiboToolBar);

        if (Constants.isEnableAppsrc) {
            mDrawerToggle = new MyDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
            mDrawerToggle.syncState();
            mDrawerLayout.setDrawerListener(mDrawerToggle);
		}else {
	        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
	        disPlayHomeAsUp(mToolbar);
	        RelativeLayout editAppSrc = ViewUtility.findViewById(this, R.id.editAppSrc);
	        editAppSrc.setVisibility(View.GONE);
		}


        mAccountBean = BeeboApplication.getInstance().getAccountBean();
        // mAccountBean = getAccount();
        Log.d("RpostWeiBo_activity", "AccountBean == null ? : " + (mAccountBean == null));

        mEditPicScrollView = (ScrollView) findViewById(R.id.scrollView1);
        editTextLayout = (RelativeLayout) findViewById(R.id.editTextLayout);

        weiTextCountTV = (TextView) findViewById(R.id.weiTextCountTV);

        appSrcBtn = (Button) findViewById(R.id.appSrcBtn);
        appSrcBtn.setText(getWeiba().getText());

        mSelectPhoto = (ImageButton) findViewById(R.id.imageButton1);
        mSelectPhoto.setVisibility(View.GONE);
        mRootView = (RelativeLayout) findViewById(R.id.container);
        mEditText = (MaterialEditText) findViewById(R.id.weiboContentET);
        smileButton = (ImageButton) findViewById(R.id.smileImgButton);
        mSendBtn = (ImageButton) findViewById(R.id.sendWeiBoBtn);

        mTopicBtn = (ImageButton) findViewById(R.id.menu_topic);
        mAtButton = (ImageButton) findViewById(R.id.menu_at);
        mTopicBtn.setOnClickListener(this);
        mAtButton.setOnClickListener(this);

        // findAllEmotionImageView((ViewGroup) findViewById(R.id.emotionTL));
        mSelectPhoto.setOnClickListener(this);
        smileButton.setOnClickListener(this);
        mSendBtn.setOnClickListener(this);
        appSrcBtn.setOnClickListener(this);
        mEditPicScrollView.setOnClickListener(this);
        editTextLayout.setOnClickListener(this);
        mEditText.addTextChangedListener(watcher);

        mSmileyPicker = (SmileyPicker) findViewById(R.id.smileLayout_ref);
        mSmileyPicker.setEditText(mEditText);
        mEditText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSmileyPicker(true);
            }
        });

        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        Intent intent = getIntent();
        if (WriteRepostActivity.ACTION_SEND_FAILED.equals(intent.getAction())) {
			mAccountBean = intent.getParcelableExtra(Constants.ACCOUNT);
			mEditText.setText(intent.getStringExtra("content"));
			msg = intent.getParcelableExtra("oriMsg");
		}else {
	        handleNormalOperation(intent);
		}

        mDBmanager = new AppsrcDatabaseManager(getApplicationContext());
        listAdapter = new ChangeWeibaAdapter(this);
        listView = (PullToRefreshListView) findViewById(R.id.left_menu_list_view);
        listView.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                if (WeiBaNetUtils.isNetworkAvaliable(getApplicationContext())) {
                    listView.setRefreshing();
                    fetchAppSrc();
                } else {
                    listView.post(new Runnable() {
                        public void run() {
                            listView.onRefreshComplete();
                        }
                    });
                    Toast.makeText(getApplicationContext(), R.string.net_not_avaliable, Toast.LENGTH_SHORT).show();
                }

            }
        });
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);
        
        
    }

    private void showSmileyPicker(boolean showAnimation) {
//        this.mSmileyPicker.show(this, showAnimation);
    }

    public void hideSmileyPicker(boolean showKeyBoard) {
        if (this.mSmileyPicker.isShown()) {
            if (showKeyBoard) {
                // this time softkeyboard is hidden
                RelativeLayout.LayoutParams localLayoutParams = (RelativeLayout.LayoutParams) this.mEditText
                        .getLayoutParams();
                localLayoutParams.height = mSmileyPicker.getTop();
//                this.mSmileyPicker.hide(this);

                SmileyPickerUtility.showKeyBoard(mEditText);
                mEditText.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // unlockContainerHeightDelayed();
                    }
                }, 200L);
            } else {
//                this.mSmileyPicker.hide(this);
                // unlockContainerHeightDelayed();
            }
        }

    }

    class MyDrawerToggle extends ActionBarDrawerToggle {

        public MyDrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar, int openDrawerContentDescRes,
                int closeDrawerContentDescRes) {
            super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);

            List<WeiboWeiba> list = mDBmanager.fetchAllAppsrc();
            /*
             * if (isKeyBoardShowed) { imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
             * InputMethodManager.HIDE_NOT_ALWAYS); }
             */
            if (list.size() == 0) {
                if (WeiBaNetUtils.isNetworkAvaliable(getApplicationContext())) {
                    fetchAppSrc();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.net_not_avaliable, Toast.LENGTH_SHORT).show();
                }
            } else {
                listAdapter.setWeibas(list);
            }
        }
    }

    protected void fetchAppSrc() {
        fetchWeiBa(new OnFetchAppSrcListener() {

            @Override
            public void onSuccess(List<WeiboWeiba> appsrcs) {
                for (WeiboWeiba weiboWeiba : appsrcs) {
                    if (mDBmanager.searchAppsrcByCode(weiboWeiba.getCode()) == null) {
                        mDBmanager.insertCategoryTree(0, weiboWeiba.getCode(), weiboWeiba.getText());
                    }
                }
                listView.onRefreshComplete();
                listAdapter.setWeibas(mDBmanager.fetchAllAppsrc());
                hideDialogForWeiBo();
            }

            @Override
            public void onStart() {
                showDialogForWeiBo();
            }

            @Override
            public void onFailure() {
                hideDialogForWeiBo();
            }
        });
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        MobclickAgent.onPageStart(this.getClass().getName());
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getName());
        MobclickAgent.onPause(this);
    }

    private TextWatcher watcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // TODO Auto-generated method stub
        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            String charSequence = mEditText.getText().toString();
            int count = Utility.length(charSequence);
            String text = count <= 0 ? getString(R.string.send_weibo) : count + "";
            weiTextCountTV.setText(text);
            if (count > 140) {
                weiTextCountTV.setTextColor(Color.RED);
            } else {
                weiTextCountTV.setTextColor(Color.BLACK);
            }
        }
    };

    public boolean isMoreThan140(){
        String charSequence = mEditText.getText().toString();
        int count = Utility.length(charSequence);
        return count > 140;
    }
    public static int calculateWeiboLength(CharSequence c) {

        int len = 0;
        for (int i = 0; i < c.length(); i++) {
            int temp = (int) c.charAt(i);
            if (temp > 0 && temp < 127) {
                len += 0.5;
            } else {
                len++;
            }
        }
        return Math.round(len);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mRootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        ImageLoader.getInstance().stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ChangeWeibaActivity.REQUEST) {
            appSrcBtn.setText(getWeiba().getText());
        } else if (resultCode == RESULT_OK && requestCode == AT_USER && data != null) {
            String name = data.getStringExtra("name");
            String ori = mEditText.getText().toString();
            int index = mEditText.getSelectionStart();
            StringBuilder stringBuilder = new StringBuilder(ori);
            stringBuilder.insert(index, name);
            mEditText.setText(stringBuilder.toString());
            mEditText.setSelection(index + name.length());
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
    }

    public void startWebLogin() {
        hideDialogForWeiBo();
        Intent intent = new Intent();
        intent.setClass(RepostWeiboWithAppSrcActivity.this, WebViewActivity.class);
        startActivity(intent);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);
        appSrcBtn.setText(getWeiba().getText());
    }

    protected void insertTopic() {
        int currentCursor = mEditText.getSelectionStart();
        Editable editable = mEditText.getText();
        editable.insert(currentCursor, "##");
        mEditText.setSelection(currentCursor + 1);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
		if (id == R.id.menu_topic) {
			insertTopic();
		} else if (id == R.id.menu_at) {
			Intent intent = AtUserActivity.atUserIntent(this, BeeboApplication.getInstance().getAccountBean().getAccess_token());
			startActivityForResult(intent, AT_USER);
		} else if (id == R.id.editTextLayout) {
			mEditText.performClick();
		} else if (id == R.id.scrollView1) {
			mEditText.performClick();
		} else if (id == R.id.appSrcBtn) {
			if (WeiBaNetUtils.isNetworkAvaliable(getApplicationContext())) {
			    mDrawerLayout.openDrawer(Gravity.START);
			} else {
			    Toast.makeText(getApplicationContext(), R.string.net_not_avaliable, Toast.LENGTH_SHORT).show();
			}
		} else if (id == R.id.sendWeiBoBtn) {
			if (isMoreThan140()) {
				Toast.makeText(getApplicationContext(), "字数超出限制", Toast.LENGTH_SHORT).show();
				return;
			}
			if (WeiBaNetUtils.isNetworkAvaliable(getApplicationContext())) {
				repostWeibo();
			} else {
			    Toast.makeText(getApplicationContext(), R.string.net_not_avaliable, Toast.LENGTH_SHORT).show();
			}
		} else if (id == R.id.smileImgButton) {
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
			mHandler.postDelayed(new Runnable() {
			    public void run() {
			        if (mSmileyPicker.isShown()) {
			            hideSmileyPicker(true);
			        } else {
			            showSmileyPicker(SmileyPickerUtility.isKeyBoardShow(RepostWeiboWithAppSrcActivity.this));
			        }
			    }
			}, 100);
		} else if (id == R.id.imageButton1) {
			Intent mIntent = new Intent(getApplicationContext(), ImgFileListActivity.class);
			startActivityForResult(mIntent, ImgFileListActivity.REQUEST_CODE);
		} else {
		}

    }
    
    protected void repostWeibo() {
        	if (Constants.isEnableAppsrc) {
                Intent intent = new Intent(RepostWeiboWithAppSrcActivity.this, RepostWithAppSrcServices.class);
                
                intent.putExtra(RepostWithAppSrcServices.IS_COMMENT, mComments.isChecked());
                intent.putExtra(RepostWithAppSrcServices.TEXT_CONTENT,getRepostTextContent());
                intent.putExtra(RepostWithAppSrcServices.APP_SRC, getWeiba());
                intent.putExtra(RepostWithAppSrcServices.WEIBO_MID, msg.getId());
                startService(intent);
                finish();
			}else {
	            String is_comment = mComments.isChecked() ? "" :  RepostNewMsgDao.ENABLE_COMMENT;
	            Intent intent = new Intent(RepostWeiboWithAppSrcActivity.this, SendRepostService.class);
	            intent.putExtra("oriMsg", msg);
	            intent.putExtra("content", getRepostTextContent());
	            intent.putExtra("is_comment", is_comment);
	            intent.putExtra(Constants.TOKEN, BeeboApplication.getInstance().getAccessToken());
	            intent.putExtra(Constants.ACCOUNT, BeeboApplication.getInstance().getAccountBean());
	            startService(intent);
	            finish();
			}
    }

    @Override
    public void onGlobalLayout() {
        // TODO Auto-generated method stub
        //
        // Rect r = new Rect();
        // mEmotionRelativeLayout.getWindowVisibleDisplayFrame(r);
        //
        // int heightDiff = mEmotionRelativeLayout.getRootView().getHeight() - (r.bottom - r.top);
        // if (heightDiff > 100) {
        // // if more than 100 pixels, its probably a keyboard...
        // Log.d("WEIBO_INPUT", "++++++++");
        // mEmotionRelativeLayout.setVisibility(View.GONE);
        // } else {
        // Log.d("WEIBO_INPUT", "---------");
        // }

    }

    class WeiBaCacheFile implements FilenameFilter {

        @Override
        public boolean accept(File dir, String filename) {
            // TODO Auto-generated method stub
            return filename.startsWith("WEI-");
        }

    }

    private void handleNormalOperation(Intent intent) {

        msg = (MessageBean) intent.getParcelableExtra("msg");

        if (msg.getRetweeted_status() != null) {
            mEditText.setText("//@" + msg.getUser().getScreen_name() + ": " + msg.getText());
            mEditText.setHint("//@" + msg.getRetweeted_status().getUser().getScreen_name() + "："
                    + msg.getRetweeted_status().getText());
        } else {
            mEditText.setHint("@" + msg.getUser().getScreen_name() + "：" + msg.getText());
        }
        mEditText.setSelection(0);
    }

	private String getRepostTextContent() {
		String text = mEditText.getText().toString();
        if (TextUtils.isEmpty(text)) {
            text = "转发微博";
        }
        return text;
	}

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        WeiboWeiba weiba = ((WeiboWeiba) parent.getItemAtPosition(position));
        Log.d("CLICK", "" + weiba);
        saveWeiba(weiba);
        // menu.toggle();
        mDrawerLayout.closeDrawer(findViewById(R.id.drawerLeft));
    }
}
