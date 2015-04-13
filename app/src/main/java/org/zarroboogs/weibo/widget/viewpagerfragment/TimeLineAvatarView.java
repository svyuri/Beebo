package org.zarroboogs.weibo.widget.viewpagerfragment;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;

import org.zarroboogs.weibo.R;

public class TimeLineAvatarView extends RelativeLayout{

    private SimpleDraweeView mSimpleDrawee;
    private View view;
    public TimeLineAvatarView(Context context) {
        super(context);
    }

    public TimeLineAvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflate(context, R.layout.time_line_avatar_layout, null);
    }

    public TimeLineAvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}
