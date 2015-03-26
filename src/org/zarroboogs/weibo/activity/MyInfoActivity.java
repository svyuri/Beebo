
package org.zarroboogs.weibo.activity;

import org.zarroboogs.weibo.R;

import com.umeng.analytics.MobclickAgent;

public class MyInfoActivity extends UserInfoActivity {
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        MobclickAgent.onPageStart(this.getClass().getName());
        MobclickAgent.onResume(this);
        disPlayHomeAsUp(R.id.userInfoToolBar);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getName());
        MobclickAgent.onPause(this);
    }
}
