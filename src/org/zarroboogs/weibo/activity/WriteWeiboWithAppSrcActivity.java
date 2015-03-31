
package org.zarroboogs.weibo.activity;

import java.util.ArrayList;
import java.util.List;

import org.zarroboogs.util.net.LoginWeiboAsyncTask.LoginWeiboCallack;
import org.zarroboogs.utils.Constants;
import org.zarroboogs.utils.Utility;
import org.zarroboogs.utils.WeiBaNetUtils;
import org.zarroboogs.weibo.ChangeWeibaAdapter;
import org.zarroboogs.weibo.GlobalContext;
import org.zarroboogs.weibo.R;
import org.zarroboogs.weibo.bean.AccountBean;
import org.zarroboogs.weibo.bean.GeoBean;
import org.zarroboogs.weibo.bean.StatusDraftBean;
import org.zarroboogs.weibo.bean.WeiboWeiba;
import org.zarroboogs.weibo.db.AppsrcDatabaseManager;
import org.zarroboogs.weibo.selectphoto.ImgFileListActivity;
import org.zarroboogs.weibo.selectphoto.SendImgData;
import org.zarroboogs.weibo.service.SendWeiboService;
import org.zarroboogs.weibo.service.SendWithAppSrcServices;
import org.zarroboogs.weibo.support.utils.BundleArgsConstants;
import org.zarroboogs.weibo.support.utils.SmileyPickerUtility;
import org.zarroboogs.weibo.support.utils.ViewUtility;
import org.zarroboogs.weibo.widget.SmileyPicker;
import org.zarroboogs.weibo.widget.galleryview.ViewPagerActivity;
import org.zarroboogs.weibo.widget.pulltorefresh.PullToRefreshBase;
import org.zarroboogs.weibo.widget.pulltorefresh.PullToRefreshBase.OnRefreshListener;
import org.zarroboogs.weibo.widget.pulltorefresh.PullToRefreshListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class WriteWeiboWithAppSrcActivity extends BaseLoginActivity implements LoginWeiboCallack, OnClickListener,
         OnItemClickListener, OnSharedPreferenceChangeListener {

    public static final int AT_USER = 0x1000;

    public static final String LOGIN_TAG = "START_SEND_WEIBO ";
    protected static final String TAG = "WeiboMainActivity  ";
    private SmileyPicker mSmileyPicker = null;

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

    private boolean isKeyBoardShowed = false;
    private ScrollView mEditPicScrollView;

    private TextView weiTextCountTV;

    private Toast mEmptyToast;
    private PullToRefreshListView listView;
    private ChangeWeibaAdapter listAdapter;
    private AppsrcDatabaseManager mDBmanager = null;
    private String atContent = "";

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;

    private GridView mNinePicGridView;
    private NinePicGriViewAdapter mNinePicAdapter;
    
    public static Intent startBecauseSendFailed(Context context, AccountBean accountBean, String content, String picPath,
            GeoBean geoBean,
            StatusDraftBean statusDraftBean, String failedReason) {
        Intent intent = new Intent(context, WriteWeiboWithAppSrcActivity.class);
        intent.setAction(WriteWeiboActivity.ACTION_SEND_FAILED);
        intent.putExtra(Constants.ACCOUNT, accountBean);
        intent.putExtra("content", content);
        intent.putExtra("failedReason", failedReason);
        intent.putExtra("picPath", picPath);
        intent.putExtra("geoBean", geoBean);
        intent.putExtra("statusDraftBean", statusDraftBean);
        return intent;
    }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSPs().registerOnSharedPreferenceChangeListener(this);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        setContentView(R.layout.write_weibo_with_appsrc_activity_layout);
        // drawerLayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.writeWeiboDrawerL);
        mToolbar = (Toolbar) findViewById(R.id.writeWeiboToolBar);

        if (Constants.isBeeboPlus) {
            mDrawerToggle = new MyDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
            mDrawerToggle.syncState();
            mDrawerLayout.setDrawerListener(mDrawerToggle);
		}else {
	        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
	        disPlayHomeAsUp(mToolbar);
	        RelativeLayout editAppSrc = ViewUtility.findViewById(this, R.id.editAppSrc);
	        editAppSrc.setVisibility(View.GONE);
		}


        mAccountBean = getIntent().getParcelableExtra(BundleArgsConstants.ACCOUNT_EXTRA);
        atContent = getIntent().getStringExtra("content");

        mEmptyToast = Toast.makeText(getApplicationContext(), R.string.text_is_empty, Toast.LENGTH_SHORT);

        mEditPicScrollView = (ScrollView) findViewById(R.id.scrollView1);
        editTextLayout = (RelativeLayout) findViewById(R.id.editTextLayout);

        weiTextCountTV = (TextView) findViewById(R.id.weiTextCountTV);
        
        mNinePicAdapter = new NinePicGriViewAdapter(getApplicationContext());
        mNinePicGridView = (GridView) findViewById(R.id.ninePicGridView);
        mNinePicGridView.setAdapter(mNinePicAdapter);

        mNinePicGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
					Intent intent = new Intent(WriteWeiboWithAppSrcActivity.this, ViewPagerActivity.class);
					intent.putStringArrayListExtra(ViewPagerActivity.IMG_LIST, SendImgData.getInstance().getSendImgs());
					intent.putExtra(ViewPagerActivity.IMG_ID, position);
					startActivity(intent);
					
			}
		});

        appSrcBtn = (Button) findViewById(R.id.appSrcBtn);
        
        appSrcBtn.setText(getWeiba().getText());

        mSelectPhoto = (ImageButton) findViewById(R.id.imageButton1);
        mRootView = (RelativeLayout) findViewById(R.id.container);
        mEditText = (com.rengwuxian.materialedittext.MaterialEditText) findViewById(R.id.weiboContentET);
        mSmileyPicker = (SmileyPicker) findViewById(R.id.smileLayout_ref);
        mSmileyPicker.setEditText(this, mRootView, mEditText);
        mEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSmileyPicker(true);
            }
        });

        mTopicBtn = (ImageButton) findViewById(R.id.menu_topic);
        mAtButton = (ImageButton) findViewById(R.id.menu_at);

        smileButton = (ImageButton) findViewById(R.id.smileImgButton);
        mSendBtn = (ImageButton) findViewById(R.id.sendWeiBoBtn);

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

        if (!TextUtils.isEmpty(atContent)) {
            mEditText.setText(atContent + " ");
            mEditText.setSelection(mEditText.getEditableText().toString().length());
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

        Intent intent = getIntent();
        if (WriteWeiboActivity.ACTION_SEND_FAILED.equals(intent.getAction())) {
        	mEditText.setText(intent.getStringExtra("content"));
        	String picUrl = intent.getStringExtra("picPath");
        	if (!TextUtils.isEmpty(picUrl)) {
        		SendImgData.getInstance().clearSendImgs();
        		SendImgData.getInstance().clearReSizeImgs();
        		
        		SendImgData.getInstance().addSendImg(picUrl);
				refreshNineGridView();
			}
        	
        	mAccountBean = intent.getParcelableExtra(Constants.ACCOUNT);
		}

    }
    
    protected void fetchAppSrc() {
		fetchWeiBa(new OnFetchAppSrcListener() {

			@Override
			public void onSuccess(List<WeiboWeiba> appsrcs) {
				for (WeiboWeiba weiboWeiba : appsrcs) {
					if (mDBmanager.searchAppsrcByCode(weiboWeiba.getCode()) == null) {
						mDBmanager.insertCategoryTree(0, weiboWeiba.getCode(),
								weiboWeiba.getText());
					}
				}
				listView.onRefreshComplete();
				listAdapter.setWeibas(mDBmanager.fetchAllAppsrc());
			}

			@Override
			public void onStart() {
				listView.setRefreshing();
			}

			@Override
			public void onFailure() {
				listView.onRefreshComplete();
			}
		});
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
            if (isKeyBoardShowed) {
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
            }
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

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        MobclickAgent.onPageStart(this.getClass().getName());
        MobclickAgent.onResume(this);
        
        refreshNineGridView();
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
        ImageLoader.getInstance().stop();
        getSPs().unregisterOnSharedPreferenceChangeListener(this);
        
        SendImgData.getInstance().clearSendImgs();
        SendImgData.getInstance().clearReSizeImgs();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ChangeWeibaActivity.REQUEST) {
            appSrcBtn.setText(getWeiba().getText());
        } else if (resultCode == RESULT_OK && requestCode == ImgFileListActivity.REQUEST_CODE) {
            refreshNineGridView();

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

	private void refreshNineGridView() { 
		mNinePicAdapter.notifyDataSetChanged();
	}

    @Override
    public void onBackPressed() {
        if (mSmileyPicker.isShown()) {
            hideSmileyPicker(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onLonginWeiboCallback(boolean isSuccess) {
        if (!isSuccess) {
            startWebLogin();
        }
    }



	private String getWeiboTextContent() {
		String text = mEditText.getEditableText().toString();
        if (TextUtils.isEmpty(text)) {
            text = getString(R.string.default_text_pic_weibo);
        }
		return text;
	}

    private boolean checkDataEmpty() {
        if (TextUtils.isEmpty(mEditText.getText().toString()) && SendImgData.getInstance().getSendImgs().size() < 1) {
            return true;
        }
        return false;
    }

    public boolean isMoreThan140(){
        String charSequence = mEditText.getText().toString();
        int count = Utility.length(charSequence);
        return count > 140;
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
			Intent intent = AtUserActivity.atUserIntent(this, GlobalContext.getInstance().getAccessTokenHack());
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
				Toast.makeText(getApplicationContext(), R.string.weibo_text_large_error, Toast.LENGTH_SHORT).show();
				return;
			}
			if (WeiBaNetUtils.isNetworkAvaliable(getApplicationContext())) {
			    if (checkDataEmpty()) {
			        mEmptyToast.show();
			    } else {
			    	
		    		ArrayList<String> send = SendImgData.getInstance().getSendImgs(); 
		    		
		    		if (send.size() >1) {
						Intent intent = new Intent(getApplicationContext(), SendWithAppSrcServices.class);
						intent.putExtra(SendWithAppSrcServices.APP_SRC, getWeiba());
						intent.putExtra(SendWithAppSrcServices.TEXT_CONTENT, getWeiboTextContent());
						startService(intent);
						finish();
					}else {
			    		String charSequence = getWeiboTextContent();
			    		if (send != null && send.size() > 0) {
			    			executeTask(charSequence,send.get(0));
						}else {
							executeTask(charSequence,null);
						}
					}
			    }
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
			            showSmileyPicker(SmileyPickerUtility.isKeyBoardShow(WriteWeiboWithAppSrcActivity.this));
			        }
			        // if (mEmotionRelativeLayout.getVisibility() == View.GONE) {
			        // mEmotionRelativeLayout.setVisibility(View.VISIBLE);
			        // } else {
			        // mEmotionRelativeLayout.setVisibility(View.GONE);
			        // }
			    }
			}, 100);
		} else if (id == R.id.imageButton1) {
			Intent mIntent = new Intent(getApplicationContext(), ImgFileListActivity.class);
			startActivityForResult(mIntent, ImgFileListActivity.REQUEST_CODE);
		} 
		
    }

    Handler mHandler = new Handler();
    
    protected void executeTask(String contentString, String picPath) {
        Intent intent = new Intent(WriteWeiboWithAppSrcActivity.this, SendWeiboService.class);
        intent.putExtra(Constants.TOKEN, mAccountBean.getAccess_token());
        if (picPath != null) {
            intent.putExtra("picPath", picPath);
		}
        intent.putExtra(Constants.ACCOUNT, mAccountBean);
        intent.putExtra("content", contentString);
//        intent.putExtra("geo", null);
//        intent.putExtra("draft", statusDraftBean);
        startService(intent);
        finish();
    }
    
    private void showSmileyPicker(boolean showAnimation) {
        this.mSmileyPicker.show(this, showAnimation);
    }

    public void hideSmileyPicker(boolean showKeyBoard) {
        if (this.mSmileyPicker.isShown()) {
            if (showKeyBoard) {
                // this time softkeyboard is hidden
                RelativeLayout.LayoutParams localLayoutParams = (RelativeLayout.LayoutParams) this.mEditText
                        .getLayoutParams();
                localLayoutParams.height = mSmileyPicker.getTop();
                this.mSmileyPicker.hide(this);

                SmileyPickerUtility.showKeyBoard(mEditText);
                mEditText.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // unlockContainerHeightDelayed();
                    }
                }, 200L);
            } else {
                this.mSmileyPicker.hide(this);
                // unlockContainerHeightDelayed();
            }
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // TODO Auto-generated method stub
        super.onSharedPreferenceChanged(sharedPreferences, key);
        appSrcBtn.setText(getWeiba().getText());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        WeiboWeiba weiba = ((WeiboWeiba) parent.getItemAtPosition(position));
        Log.d("CLICK", "" + weiba);
        saveWeiba(weiba);
        // menu.toggle();
        mDrawerLayout.closeDrawer(findViewById(R.id.drawerLeft));
    }
}
