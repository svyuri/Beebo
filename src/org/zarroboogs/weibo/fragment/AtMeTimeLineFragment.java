
package org.zarroboogs.weibo.fragment;

import org.zarroboogs.weibo.GlobalContext;
import org.zarroboogs.weibo.R;
import org.zarroboogs.weibo.activity.MainTimeLineActivity;
import org.zarroboogs.weibo.bean.UnreadTabIndex;
import org.zarroboogs.weibo.fragment.base.AbsBaseTimeLineFragment;
import org.zarroboogs.weibo.fragment.base.BaseStateFragment;
import org.zarroboogs.weibo.support.lib.LongClickableLinkMovementMethod;
import org.zarroboogs.weibo.support.utils.BundleArgsConstants;
import org.zarroboogs.weibo.support.utils.Utility;
import org.zarroboogs.weibo.widget.viewpagerfragment.ChildPage;
import org.zarroboogs.weibo.widget.viewpagerfragment.ViewPagerFragment;

import com.example.android.common.view.SlidingTabLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

public class AtMeTimeLineFragment extends ViewPagerFragment {


	public static final int AT_ME_WEIBO = 0;
	public static final int AT_ME_COMMENT = 1;

	public static final int COMMENT_TO_ME = 2;
	public static final int COMMENT_BY_ME = 3;

    public static AtMeTimeLineFragment newInstance() {
        AtMeTimeLineFragment fragment = new AtMeTimeLineFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

	@Override
	public SparseArray<ChildPage> buildChildPage() {
		// TODO Auto-generated method stub
		SparseArray<ChildPage> sparseArray = new SparseArray<ChildPage>();
		sparseArray.append(AtMeTimeLineFragment.AT_ME_WEIBO,new ChildPage(
				getActivity().getResources().getString(R.string.mentions_weibo),
				getMentionsWeiboTimeLineFragment()));
		
		sparseArray.append(AtMeTimeLineFragment.AT_ME_COMMENT,new ChildPage(
				getActivity().getResources().getString(R.string.mentions_to_me), getMentionsCommentTimeLineFragment()));
        
		sparseArray.append(AtMeTimeLineFragment.COMMENT_TO_ME,new ChildPage(
				getActivity().getResources().getString(R.string.all_people_send_to_me), getCommentsToMeTimeLineFragment()));
		
		
		sparseArray.append(AtMeTimeLineFragment.COMMENT_BY_ME,new ChildPage(
				getActivity().getResources().getString(R.string.my_comment), getCommentsByMeTimeLineFragment()));
        
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
//        switch (unreadTabIndex) {
//            case MENTION_WEIBO:
//                ((MainTimeLineActivity) getActivity()).getLeftMenuFragment().switchCategory(LeftMenuFragment.MENTIONS_INDEX);
//                viewPager.setCurrentItem(0);
//                intent.putExtra(BundleArgsConstants.OPEN_NAVIGATION_INDEX_EXTRA, UnreadTabIndex.NONE);
//                break;
//            case MENTION_COMMENT:
//                ((MainTimeLineActivity) getActivity()).getLeftMenuFragment().switchCategory(LeftMenuFragment.MENTIONS_INDEX);
//                viewPager.setCurrentItem(1);
//                intent.putExtra(BundleArgsConstants.OPEN_NAVIGATION_INDEX_EXTRA, UnreadTabIndex.NONE);
//                break;
//			case COMMENT_TO_ME:
//                ((MainTimeLineActivity) getActivity()).getLeftMenuFragment().switchCategory(LeftMenuFragment.MENTIONS_INDEX);
//                viewPager.setCurrentItem(2);
//                intent.putExtra(BundleArgsConstants.OPEN_NAVIGATION_INDEX_EXTRA, UnreadTabIndex.NONE);
//				break;
//			case NONE:
//				break;
//			default:
//				break;
//        }

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
