
package org.zarroboogs.weibo.activity;

import java.io.File;
import java.io.FilenameFilter;

import org.zarroboogs.keyboardlayout.KeyboardRelativeLayout;
import org.zarroboogs.keyboardlayout.OnKeyboardStateChangeListener;
import org.zarroboogs.keyboardlayout.smilepicker.SmileyPicker;
import org.zarroboogs.utils.Constants;
import org.zarroboogs.utils.Utility;
import org.zarroboogs.utils.WeiBaNetUtils;
import org.zarroboogs.weibo.BeeboApplication;
import org.zarroboogs.weibo.R;
import org.zarroboogs.weibo.WebViewActivity;
import org.zarroboogs.weibo.bean.AccountBean;
import org.zarroboogs.weibo.bean.MessageBean;
import org.zarroboogs.weibo.bean.RepostDraftBean;
import org.zarroboogs.weibo.dao.RepostNewMsgDao;
import org.zarroboogs.weibo.selectphoto.ImgFileListActivity;
import org.zarroboogs.weibo.service.RepostWithAppSrcServices;
import org.zarroboogs.weibo.service.SendRepostService;
import org.zarroboogs.weibo.support.utils.ViewUtility;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.umeng.analytics.MobclickAgent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class RepostWeiboWithAppSrcActivity extends BaseLoginActivity implements  OnClickListener, OnItemClickListener {

    public static final int AT_USER = 0x1000;
    public static final String TAG = "RepostWeiboMainActivity ";

    private SmileyPicker mSmileyPicker = null;
    private MessageBean msg;

    private MaterialEditText mEditText;

    private ImageButton mSelectPhoto;
    private ImageButton mSendBtn;
    private ImageButton smileButton;
    private ImageButton mTopicBtn;
    private ImageButton mAtButton;

    private AccountBean mAccountBean;
    private ScrollView mEditPicScrollView;

    private TextView weiTextCountTV;

    private Toolbar mToolbar;

    private KeyboardRelativeLayout keyboardLayout;
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
        setContentView(R.layout.repost_weibo_activity_layout_new);

        mComments = (CheckBox) findViewById(R.id.repostCommentsCheck);
        
        mToolbar = (Toolbar) findViewById(R.id.writeWeiboToolBar);

        keyboardLayout = ViewUtility.findViewById(this, R.id.keyboardLayout);
        keyboardLayout.setOnKeyboardStateListener(new OnKeyboardStateChangeListener() {
            @Override
            public void onKeyBoardShow(int height) {

            }

            @Override
            public void onKeyBoardHide() {

            }
        });
        disPlayHomeAsUp(mToolbar);

        mAccountBean = BeeboApplication.getInstance().getAccountBean();
        // mAccountBean = getAccount();
        Log.d("RpostWeiBo_activity", "AccountBean == null ? : " + (mAccountBean == null));

        mEditPicScrollView = (ScrollView) findViewById(R.id.scrollview);

        weiTextCountTV = (TextView) findViewById(R.id.weiTextCountTV);

        mSelectPhoto = (ImageButton) findViewById(R.id.imageButton1);
        mSelectPhoto.setVisibility(View.GONE);
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
        mEditPicScrollView.setOnClickListener(this);
        mEditText.addTextChangedListener(watcher);

        mSmileyPicker = (SmileyPicker) findViewById(R.id.smileLayout_ref);
        mSmileyPicker.setEditText(mEditText);

        Intent intent = getIntent();
        if (WriteRepostActivity.ACTION_SEND_FAILED.equals(intent.getAction())) {
			mAccountBean = intent.getParcelableExtra(Constants.ACCOUNT);
			mEditText.setText(intent.getStringExtra("content"));
			msg = intent.getParcelableExtra("oriMsg");
		}else {
	        handleNormalOperation(intent);
		}


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
        ImageLoader.getInstance().stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ChangeWeibaActivity.REQUEST) {
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
            // show or hide Keyboard
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

    }
}
