
package org.zarroboogs.weibo.fragment;

import java.util.ArrayList;

import org.zarroboogs.weibo.GlobalContext;
import org.zarroboogs.weibo.R;
import org.zarroboogs.weibo.bean.UnreadTabIndex;
import org.zarroboogs.weibo.support.utils.BundleArgsConstants;
import org.zarroboogs.weibo.widget.viewpagerfragment.ChildPage;
import org.zarroboogs.weibo.widget.viewpagerfragment.ViewPagerFragment;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

public class AtMeTimeLineFragment extends ViewPagerFragment {


//	public static final int COMMENT_TO_ME = 0; 	// 收到评论
//	public static final int AT_ME_WEIBO = 1;	// 微博提及
//	public static final int AT_ME_COMMENT = 2;	// 评论提及
//	public static final int DM_INDEX = 3;		// 微博私心

//	public static final int COMMENT_BY_ME = 3;

    public static AtMeTimeLineFragment newInstance() {
        AtMeTimeLineFragment fragment = new AtMeTimeLineFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

	@Override
	public ArrayList<ChildPage> buildChildPage() {
		// TODO Auto-generated method stub
		ArrayList<ChildPage> sparseArray = new ArrayList<ChildPage>();
		
		Resources re = getActivity().getResources();
		
		sparseArray.add(new ChildPage(
				re.getString(R.string.mentions_weibo),getMentionsWeiboTimeLineFragment()));
		
		sparseArray.add(new ChildPage(
				re.getString(R.string.mentions_to_me), getMentionsCommentTimeLineFragment()));
        
		sparseArray.add(new ChildPage(
				re.getString(R.string.all_people_send_to_me), getCommentsToMeTimeLineFragment()));
		
		
		sparseArray.add( new ChildPage(
				re.getString(R.string.private_message), getDMFragment()));
		
//		sparseArray.append(AtMeTimeLineFragment.COMMENT_BY_ME,new ChildPage(
//				getActivity().getResources().getString(R.string.my_comment), getCommentsByMeTimeLineFragment()));
        
		return sparseArray;
	}
    
    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return;
        }
        UnreadTabIndex unreadTabIndex = (UnreadTabIndex) intent
                .getSerializableExtra(BundleArgsConstants.OPEN_NAVIGATION_INDEX_EXTRA);
        if (unreadTabIndex == null) {
            return;
        }
    }

    public MentionsCommentTimeLineFragment getMentionsCommentTimeLineFragment() {
        MentionsCommentTimeLineFragment fragment = ((MentionsCommentTimeLineFragment) getChildFragmentManager()
                .findFragmentByTag(
                        MentionsCommentTimeLineFragment.class.getName()));
        if (fragment == null) {
            fragment = new MentionsCommentTimeLineFragment(GlobalContext.getInstance().getAccountBean(),
                    GlobalContext.getInstance().getAccountBean().getInfo(), GlobalContext.getInstance().getAccessToken());
        }

        return fragment;
    }

    public MentionsWeiboTimeLineFragment getMentionsWeiboTimeLineFragment() {
        MentionsWeiboTimeLineFragment fragment = ((MentionsWeiboTimeLineFragment) getChildFragmentManager()
                .findFragmentByTag(
                        MentionsWeiboTimeLineFragment.class.getName()));
        if (fragment == null) {
            fragment = new MentionsWeiboTimeLineFragment(GlobalContext.getInstance().getAccountBean(), GlobalContext
                    .getInstance().getAccountBean().getInfo(),
                    GlobalContext.getInstance().getAccessToken());
        }

        return fragment;
    }

    public CommentsToMeTimeLineFragment getCommentsToMeTimeLineFragment() {
        CommentsToMeTimeLineFragment fragment = ((CommentsToMeTimeLineFragment) getChildFragmentManager().findFragmentByTag(
                CommentsToMeTimeLineFragment.class.getName()));
        if (fragment == null) {
            fragment = new CommentsToMeTimeLineFragment(GlobalContext.getInstance().getAccountBean(), GlobalContext
                    .getInstance().getAccountBean().getInfo(),
                    GlobalContext.getInstance().getAccessToken());
        }

        return fragment;
    }

    public DMUserListFragment getDMFragment() {
        DMUserListFragment fragment = ((DMUserListFragment) getChildFragmentManager().findFragmentByTag(
                DMUserListFragment.class.getName()));
        if (fragment == null) {
            fragment = DMUserListFragment.newInstance();
        }
        return fragment;
    }
    
    public CommentsByMeTimeLineFragment getCommentsByMeTimeLineFragment() {
        CommentsByMeTimeLineFragment fragment = ((CommentsByMeTimeLineFragment) getChildFragmentManager().findFragmentByTag(
                CommentsByMeTimeLineFragment.class.getName()));
        if (fragment == null) {
            fragment = new CommentsByMeTimeLineFragment(GlobalContext.getInstance().getAccountBean(), GlobalContext
                    .getInstance().getAccountBean().getInfo(),
                    GlobalContext.getInstance().getAccessToken());
        }
        return fragment;
    }
    

	@Override
	public void onViewPageSelected(int id) {
		// TODO Auto-generated method stub
		
	}

}
