package org.zarroboogs.weibo.widget;


import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import com.facebook.drawee.view.SimpleDraweeView;

import org.zarroboogs.devutils.DevLog;
import org.zarroboogs.weibo.R;
import org.zarroboogs.weibo.bean.UserBean;
import org.zarroboogs.weibo.support.utils.ViewUtility;

public class TimeLineAvatarView extends RelativeLayout{

    private SimpleDraweeView mSimpleDraweeView;
    private ImageView mVFlagView;

    public TimeLineAvatarView(Context context) {
        super(context);
    }

    public TimeLineAvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public TimeLineAvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setDraweeUri(Uri uri){
        mSimpleDraweeView.setImageURI(uri);
    }

    public void checkVerified(UserBean user) {
        if (user != null && user.isVerified() && !TextUtils.isEmpty(user.getVerified_reason())) {
            mVFlagView.setVisibility(View.VISIBLE);
            if (user.isPersonalV()) {
                mVFlagView.setImageResource(R.drawable.avatar_vip);
            } else {
                mVFlagView.setImageResource(R.drawable.avatar_enterprise_vip);
            }
        } else {
            mVFlagView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSimpleDraweeView = ViewUtility.findViewById(this, R.id.userAvatar);

        mVFlagView = (ImageView)getChildAt(1);//ViewUtility.findViewById(this, R.id.vFlag);
        DevLog.printLog("ChildCount  === : ", "" + this.getChildCount());
    }
}



