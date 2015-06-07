
package org.zarroboogs.weibo.fragment;

import org.zarroboogs.util.net.WeiboException;
import org.zarroboogs.utils.Constants;
import org.zarroboogs.utils.ImageUtility;
import org.zarroboogs.utils.file.FileLocationMethod;
import org.zarroboogs.utils.file.FileManager;
import org.zarroboogs.weibo.BeeboApplication;
import org.zarroboogs.weibo.R;
import org.zarroboogs.weibo.activity.BrowserWeiboMsgActivity;
import org.zarroboogs.weibo.activity.EditMyProfileActivity;
import org.zarroboogs.weibo.activity.FanListActivity;
import org.zarroboogs.weibo.activity.FriendListActivity;
import org.zarroboogs.weibo.activity.MainTimeLineActivity;
import org.zarroboogs.weibo.activity.UserInfoActivity;
import org.zarroboogs.weibo.activity.UserTimeLineActivity;
import org.zarroboogs.weibo.activity.UserTopicListActivity;
import org.zarroboogs.weibo.asynctask.MyAsyncTask;
import org.zarroboogs.weibo.bean.AsyncTaskLoaderResult;
import org.zarroboogs.weibo.bean.MessageBean;
import org.zarroboogs.weibo.bean.MessageListBean;
import org.zarroboogs.weibo.bean.MyStatusTimeLineData;
import org.zarroboogs.weibo.bean.TimeLinePosition;
import org.zarroboogs.weibo.bean.UserBean;
import org.zarroboogs.weibo.dao.ShowUserDao;
import org.zarroboogs.weibo.dao.UserTopicListDao;
import org.zarroboogs.weibo.db.task.AccountDao;
import org.zarroboogs.weibo.db.task.MyStatusDBTask;
import org.zarroboogs.weibo.db.task.TopicDBTask;
import org.zarroboogs.weibo.dialogfragment.UserAvatarDialog;
import org.zarroboogs.weibo.fragment.base.AbsTimeLineFragment;
import org.zarroboogs.weibo.loader.StatusesByIdLoader;
import org.zarroboogs.weibo.support.asyncdrawable.TimeLineBitmapDownloader;
import org.zarroboogs.weibo.support.utils.AnimationUtility;
import org.zarroboogs.weibo.support.utils.TimeLineUtility;
import org.zarroboogs.weibo.support.utils.Utility;
import org.zarroboogs.weibo.support.utils.ViewUtility;
import org.zarroboogs.weibo.widget.BlurImageView;
import org.zarroboogs.weibo.widget.SwipeFrameLayout;
import org.zarroboogs.weibo.widget.TimeLineAvatarImageView;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class UserInfoFragment extends AbsTimeLineFragment<MessageListBean> implements
        MainTimeLineActivity.ScrollableListFragment,
        Animator.AnimatorListener {

    private static final String LIMITED_READ_MESSAGE_COUNT = "10";

    protected UserBean userBean;

    protected String token;

    private MessageListBean bean = new MessageListBean();

    private ViewPager viewPager;

    private ImageView cover;

    private BlurImageView blur;

    private TextView friendsCount;

    private TextView fansCount;

    private TextView topicsCount;

    private TextView weiboCount;

    public View header;

    private View headerFirst;

    private View headerSecond;

    private View headerThird;

    private TimeLineAvatarImageView avatar;

    private TextView nickname;

    private TextView bio;

    private TextView location;

    private TextView url;

    private TextView verifiedReason;

    private TextView followsYou;

    private ImageView leftPoint;

    private ImageView centerPoint;

    private ImageView rightPoint;

    private View progressFooter;

    private View moreFooter;

    private MenuItem refreshItem;

    private ArrayList<String> topicList;

    private TopicListTask topicListTask;

    private RefreshTask refreshTask;

    private DBCacheTask dbTask;

    private AtomicInteger finishedWatcher;

    private TimeLinePosition position;
    
    private Toolbar mUserToolbar;

    public static UserInfoFragment newInstance(Toolbar toolbar,UserBean userBean, String token) {
        UserInfoFragment fragment = new UserInfoFragment(userBean, token, toolbar);
        fragment.setArguments(new Bundle());
        return fragment;
    }

    public UserInfoFragment() {

    }

    @Override
    public MessageListBean getDataList() {
        return bean;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        MessageBean msg = (MessageBean) data.getParcelableExtra("msg");
        if (msg != null) {
            for (int i = 0; i < getDataList().getSize(); i++) {
                if (msg.equals(getDataList().getItem(i))) {
                    getDataList().getItem(i).setReposts_count(msg.getReposts_count());
                    getDataList().getItem(i).setComments_count(msg.getComments_count());
                    break;
                }
            }
            getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
//        view.setBackgroundDrawable(null);
        header = inflater.inflate(R.layout.newuserinfofragment_header_layout, getListView(), false);
        getListView().addHeaderView(header);

//        footerView.setVisibility(View.GONE);

        progressFooter = inflater.inflate(R.layout.newuserinfofragment_progress_footer, getListView(), false);
        progressFooter.setVisibility(View.GONE);
        getListView().addFooterView(progressFooter);

        moreFooter = inflater.inflate(R.layout.newuserinfofragment_more_footer, getListView(), false);
        moreFooter.setVisibility(View.GONE);
        getListView().addFooterView(moreFooter);

        viewPager = ViewUtility.findViewById(header, R.id.viewpager);
        cover = ViewUtility.findViewById(header, R.id.cover);
        blur = ViewUtility.findViewById(header, R.id.blur);
        friendsCount = ViewUtility.findViewById(header, R.id.friends_count);
        fansCount = ViewUtility.findViewById(header, R.id.fans_count);
        topicsCount = ViewUtility.findViewById(header, R.id.topics_count);
        weiboCount = ViewUtility.findViewById(header, R.id.weibo_count);

        headerFirst = inflater.inflate(R.layout.newuserinfofragment_header_viewpager_first_layout, null, false);
        headerSecond = inflater.inflate(R.layout.newuserinfofragment_header_viewpager_second_layout, null, false);
        headerThird = inflater.inflate(R.layout.newuserinfofragment_header_viewpager_third_layout, null, false);

        avatar = ViewUtility.findViewById(headerFirst, R.id.avatar);
        nickname = ViewUtility.findViewById(headerFirst, R.id.nickname);
        location = ViewUtility.findViewById(headerFirst, R.id.location);
        followsYou = ViewUtility.findViewById(headerFirst, R.id.follows_you);

        bio = ViewUtility.findViewById(headerSecond, R.id.bio);
        url = ViewUtility.findViewById(headerSecond, R.id.url);
        verifiedReason = ViewUtility.findViewById(headerThird, R.id.verified_reason);

        leftPoint = ViewUtility.findViewById(header, R.id.left_point);
        centerPoint = ViewUtility.findViewById(header, R.id.center_point);
        rightPoint = ViewUtility.findViewById(header, R.id.right_point);
        leftPoint.getDrawable().setLevel(1);

        View weiboCountLayout = ViewUtility.findViewById(header, R.id.weibo_count_layout);
        View friendsCountLayout = ViewUtility.findViewById(header, R.id.friends_count_layout);
        View fansCountLayout = ViewUtility.findViewById(header, R.id.fans_count_layout);
        View topicCountLayout = ViewUtility.findViewById(header, R.id.topics_count_layout);

        weiboCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = UserTimeLineActivity.newIntent(BeeboApplication.getInstance().getAccessTokenHack(), userBean);
                startActivity(intent);
            }
        });

        friendsCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = FriendListActivity.newIntent(BeeboApplication.getInstance().getAccessTokenHack(), userBean);
                startActivity(intent);
            }
        });

        fansCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = FanListActivity.newIntent(BeeboApplication.getInstance().getAccessTokenHack(), userBean);
                startActivity(intent);
            }
        });

        topicCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = UserTopicListActivity.newIntent(userBean, topicList);
                startActivity(intent);
            }
        });

        View result = view;

        if (!isOpenedFromMainPage()) {
            SwipeFrameLayout swipeFrameLayout = new SwipeFrameLayout(getActivity());
            swipeFrameLayout.addView(result, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            result = swipeFrameLayout;
        }

        return result;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	// TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu, inflater);

    	if (userBean.getId().equals(BeeboApplication.getInstance().getAccountBean().getUid())) {
    		inflater.inflate(R.menu.actionbar_menu_newuserinfofragment_main_page, menu);
            MenuItem edit = menu.findItem(R.id.menu_edit);
            edit.setVisible(false);
            editMyProFile(menu);
		}else {
			inflater.inflate(R.menu.actionbar_menu_infofragment, menu);
            if (userBean.isFollowing()) {
                menu.findItem(R.id.menu_follow).setVisible(false);
                menu.findItem(R.id.menu_unfollow).setVisible(true);
                menu.findItem(R.id.menu_manage_group).setVisible(true);
            } else {
                menu.findItem(R.id.menu_follow).setVisible(true);
                menu.findItem(R.id.menu_unfollow).setVisible(false);
                menu.findItem(R.id.menu_manage_group).setVisible(false);
            }

            if (!userBean.isFollowing() && userBean.isFollow_me()) {
                menu.findItem(R.id.menu_remove_fan).setVisible(true);
            } else {
                menu.findItem(R.id.menu_remove_fan).setVisible(false);
            }
		}

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        getPullToRefreshListView().setMode(PullToRefreshBase.Mode.DISABLED);
//        getPullToRefreshListView().setPullToRefreshOverScrollEnabled(false);
//
//        getPullToRefreshListView().setOnLastItemVisibleListener(null);
//        getPullToRefreshListView().getRefreshableView().setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        viewPager.setOnTouchListener(new View.OnTouchListener() {

            float rawX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        rawX = event.getRawX();
                        return false;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        viewPager.getParent().requestDisallowInterceptTouchEvent(false);
                        rawX = 0f;
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(rawX - event.getRawX()) > ViewConfiguration.get(getActivity()).getScaledTouchSlop()) {
                            viewPager.getParent().requestDisallowInterceptTouchEvent(true);
                        }

                        break;
                }

                return false;
            }
        });

        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        leftPoint.getDrawable().setLevel(1);
                        centerPoint.getDrawable().setLevel(0);
                        rightPoint.getDrawable().setLevel(0);
                        break;
                    case 1:
                        leftPoint.getDrawable().setLevel(0);
                        centerPoint.getDrawable().setLevel(1);
                        rightPoint.getDrawable().setLevel(0);
                        break;
                    case 2:
                        leftPoint.getDrawable().setLevel(0);
                        centerPoint.getDrawable().setLevel(0);
                        rightPoint.getDrawable().setLevel(1);
                        break;
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (position == 0) {
                    if (positionOffset > 0) {
                        blur.setAlpha(positionOffset);
                    }
                }
            }
        });

    }

    private void displayBasicInfo() {
        HeaderPagerAdapter adapter = new HeaderPagerAdapter();
        viewPager.setAdapter(adapter);

        friendsCount.setText(Utility.convertStateNumberToString(getActivity(), userBean.getFriends_count()));
        fansCount.setText(Utility.convertStateNumberToString(getActivity(), userBean.getFollowers_count()));
        weiboCount.setText(Utility.convertStateNumberToString(getActivity(), userBean.getStatuses_count()));

        TextPaint tp = nickname.getPaint();
        tp.setFakeBoldText(true);
        if (TextUtils.isEmpty(userBean.getRemark())) {
            nickname.setText(userBean.getScreen_name());
        } else {
            nickname.setText(userBean.getScreen_name() + "(" + userBean.getRemark() + ")");
        }

        // getBaseToolbar().setTitle(userBean.getScreen_name());

        avatar.checkVerified(userBean);

        if (!userBean.isVerified()) {
            rightPoint.setVisibility(View.GONE);
        } else {
            rightPoint.setVisibility(View.VISIBLE);
        }

        avatar.getImageView().post(new Runnable() {
            @Override
            public void run() {

                TimeLineBitmapDownloader.getInstance().display(avatar.getImageView(), avatar.getImageView().getWidth(),
                        avatar.getImageView().getHeight(),
                        userBean.getAvatar_large(), FileLocationMethod.avatar_large);

            }
        });

        // TimeLineBitmapDownloader.getInstance().downloadAvatar(avatar.getImageView(),
        // userBean, (AbstractTimeLineFragment) this);
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = FileManager.getFilePathFromUrl(userBean.getAvatar_large(), FileLocationMethod.avatar_large);
                if (!ImageUtility.isThisBitmapCanRead(path)) {

                    path = FileManager.getFilePathFromUrl(userBean.getProfile_image_url(), FileLocationMethod.avatar_small);

                    if (!ImageUtility.isThisBitmapCanRead(path)) {
                        return;
                    }
                }
                Rect rect = AnimationUtility.getBitmapRectFromImageView(avatar);
                UserAvatarDialog dialog = UserAvatarDialog.newInstance(path, rect);
                dialog.show(getFragmentManager(), "");
            }
        });

        if (!TextUtils.isEmpty(userBean.getDescription())) {
            bio.setText(userBean.getDescription());
            bio.setVisibility(View.VISIBLE);
        } else {
            bio.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(userBean.getLocation())) {
            location.setText(userBean.getLocation());
            location.setVisibility(View.VISIBLE);
        } else {
            location.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(userBean.getUrl())) {
            url.setText(userBean.getUrl());
            TimeLineUtility.addLinks(url);
            url.setVisibility(View.VISIBLE);
        } else {
            url.setVisibility(View.GONE);
        }

        if (userBean.isVerified()) {
            verifiedReason.setVisibility(View.VISIBLE);
            verifiedReason.setText(userBean.getVerified_reason());
        } else {
            verifiedReason.setVisibility(View.GONE);
        }

        if (userBean.isFollow_me()) {
            followsYou.setVisibility(View.VISIBLE);
            followsYou.setText(getString(R.string.is_following_me) + "@"
                    + BeeboApplication.getInstance().getCurrentAccountName());
        } else {
            followsYou.setVisibility(View.GONE);
        }

    }

    private void displayCoverPicture() {

        if (cover.getDrawable() != null) {
            return;
        }

        // final int height = viewPager.getHeight();
        final int height = Utility.dip2px(200);
        final int width = Utility.getMaxLeftWidthOrHeightImageViewCanRead(height);
        String picPath = userBean.getCover_image();
        if (TextUtils.isEmpty(picPath)) {
			picPath = "http://img.t.sinajs.cn/t5/skin/public/profile_cover/062.jpg";
		}
        blur.setAlpha(0f);
        blur.setOriImageUrl(picPath);
        ArrayList<ImageView> imageViewArrayList = new ArrayList<ImageView>();
        imageViewArrayList.add(cover);
        imageViewArrayList.add(blur);
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, -100f,
                Animation.RELATIVE_TO_SELF, 0f);
        animation.setDuration(3000);
        animation.setInterpolator(new DecelerateInterpolator());
        ArrayList<Animation> animationArray = new ArrayList<Animation>();
        animationArray.add(animation);
        TimeLineBitmapDownloader.getInstance().display(imageViewArrayList, width, height, picPath, FileLocationMethod.cover,
                animationArray);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (userBean != null && userBean.getId() != null
                && userBean.getId().equals(BeeboApplication.getInstance().getCurrentAccountId())) {
            BeeboApplication.getInstance().registerForAccountChangeListener(myProfileInfoChangeListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utility.cancelTasks(refreshTask, topicListTask);
        BeeboApplication.getInstance().unRegisterForAccountChangeListener(myProfileInfoChangeListener);
    }

    private BeeboApplication.AccountChangeListener myProfileInfoChangeListener = new BeeboApplication.AccountChangeListener() {
        @Override
        public void onChange(UserBean newUserBean) {

            if (getActivity() == null) {
                return;
            }

            userBean = newUserBean;
            displayBasicInfo();
            displayCoverPicture();
            for (MessageBean msg : getDataList().getItemList()) {
                msg.setUser(newUserBean);
            }
            getAdapter().notifyDataSetChanged();
        }
    };

    @SuppressLint("ValidFragment")
    public UserInfoFragment(UserBean userBean, String token, Toolbar toolbar) {
    	this.mUserToolbar = toolbar;
        this.userBean = userBean;
        this.token = token;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.BEAN, getDataList());
        outState.putParcelable(Constants.USERBEAN, userBean);
        outState.putString(Constants.TOKEN, token);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        switch (getCurrentState(savedInstanceState)) {
            case FIRST_TIME_START:
                displayBasicInfo();
                break;
            case SCREEN_ROTATE:
                // nothing
                refreshLayout(getDataList());
                displayBasicInfo();
                displayCoverPicture();
                if (bean.getSize() > 0) {
                    moreFooter.setVisibility(View.VISIBLE);
                    getListView().removeFooterView(progressFooter);
                }
                break;
            case ACTIVITY_DESTROY_AND_CREATE:
                getDataList().replaceData((MessageListBean) savedInstanceState.getParcelable(Constants.BEAN));
                userBean = (UserBean) savedInstanceState.getParcelable(Constants.USERBEAN);
                token = savedInstanceState.getString(Constants.TOKEN);
                getAdapter().notifyDataSetChanged();
                refreshLayout(getDataList());
                displayBasicInfo();
                displayCoverPicture();
                break;
        }

        super.onActivityCreated(savedInstanceState);
        
    }
    
	public boolean editMyFileOnItemClick() {
		if (isMyself() && isOpenedFromMainPage()) {
		    Intent intent = new Intent(getActivity(), EditMyProfileActivity.class);
		    intent.putExtra(Constants.USERBEAN, BeeboApplication.getInstance().getAccountBean().getInfo());
		    startActivity(intent);
		    return true;
		} else {
		    return false;
		}
	}
	
	public void refreshMyProFile() {
		startRefreshMenuAnimation();
		finishedWatcher = new AtomicInteger(3);
		fetchLastestUserInfoFromServer();
		fetchTopicInfoFromServer();
		loadNewMsg();
	}
	

	public void editMyProFile(Menu menu) {
		MenuItem edit = menu.findItem(R.id.menu_edit);
		edit.setVisible(BeeboApplication.getInstance().getAccountBean().isBlack_magic());
		refreshItem = menu.findItem(R.id.menu_refresh_my_profile);
	}
    
    private void fetchTopicInfoFromServer() {
        topicListTask = new TopicListTask();
        topicListTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if ((getActivity() instanceof MainTimeLineActivity) && !hidden) {
            buildActionBarAndViewPagerTitles();
        }
    }

    public void buildActionBarAndViewPagerTitles() {
        ((MainTimeLineActivity) getActivity()).setCurrentFragment(this);

        if (Utility.isDevicePort()) {
            ((MainTimeLineActivity) getActivity()).setTitle(getString(R.string.weibo_change_account));
            // getBaseToolbar().setLogo(R.drawable.ic_menu_profile);
        } else {
            ((MainTimeLineActivity) getActivity()).setTitle(getString(R.string.weibo_change_account));
            // getBaseToolbar().setLogo(R.drawable.beebo_launcher);
        }
    }

    protected void onTimeListViewItemClick(AdapterView parent, View view, int position, long id) {

        startActivityForResult(BrowserWeiboMsgActivity.newIntent(BeeboApplication.getInstance().getAccountBean(), getDataList()
                .getItem(position), BeeboApplication
                .getInstance().getAccessTokenHack()), 0);

    }

    private boolean isMyself() {

        if (!TextUtils.isEmpty(userBean.getId())) {
            return userBean.getId().equals(BeeboApplication.getInstance().getCurrentAccountId());
        }

        if (!TextUtils.isEmpty(userBean.getScreen_name())) {
            return userBean.getScreen_name().equals(BeeboApplication.getInstance().getCurrentAccountName());
        }

        return false;

    }

    private boolean isOpenedFromMainPage() {
        return getActivity() instanceof MainTimeLineActivity;
    }

    @Override
    protected boolean allowLoadOldMsgBeforeReachListBottom() {
        return false;
    }

    @Override
    protected void newMsgLoaderSuccessCallback(MessageListBean newValue, Bundle loaderArgs) {
        stopRefreshMenuAnimationIfPossible();
        getListView().removeFooterView(progressFooter);
        if (getActivity() != null && newValue.getSize() > 0) {
            getDataList().addNewData(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
            getActivity().invalidateOptionsMenu();
            moreFooter.setVisibility(View.VISIBLE);
            if (isMyself()) {
                MyStatusDBTask.asyncReplace(getDataList(), userBean.getId());
            }
        }

    }

    @Override
    protected void newMsgLoaderFailedCallback(WeiboException exception) {
        super.newMsgLoaderFailedCallback(exception);
        stopRefreshMenuAnimationIfPossible();
        getListView().removeFooterView(progressFooter);
    }

    @Override
    protected void oldMsgLoaderSuccessCallback(MessageListBean newValue) {

    }

    private void readDBCache() {
        if (Utility.isTaskStopped(dbTask) && getDataList().getSize() == 0) {
            dbTask = new DBCacheTask();
            dbTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void loadNewMsg() {
        progressFooter.setVisibility(View.VISIBLE);
        moreFooter.setVisibility(View.GONE);
        getLoaderManager().destroyLoader(MIDDLE_MSG_LOADER_ID);
        getLoaderManager().destroyLoader(OLD_MSG_LOADER_ID);
        dismissFooterView();
        getLoaderManager().restartLoader(NEW_MSG_LOADER_ID, null, msgAsyncTaskLoaderCallback);
    }

    @Override
    protected void loadOldMsg(View view) {
        Intent intent = UserTimeLineActivity.newIntent(BeeboApplication.getInstance().getAccessTokenHack(), userBean);
        startActivity(intent);
    }

    protected Loader<AsyncTaskLoaderResult<MessageListBean>> onCreateNewMsgLoader(int id, Bundle args) {
        String uid = userBean.getId();
        String screenName = userBean.getScreen_name();
        String sinceId = null;
        if (getDataList().getItemList().size() > 0) {
            sinceId = getDataList().getItemList().get(0).getId();
        }
        return new StatusesByIdLoader(getActivity(), uid, screenName, token, sinceId, null, LIMITED_READ_MESSAGE_COUNT);
    }

    @Override
    public void scrollToTop() {
        Utility.stopListViewScrollingAndScrollToTop(getListView());
    }

   

    private void startRefreshMenuAnimation() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.newuserinfofragment_refresh_actionbar_view_layout, null);
        refreshItem.setActionView(v);
    }

    private void stopRefreshMenuAnimation() {
        if (refreshItem.getActionView() != null) {
            refreshItem.setActionView(null);
        }
    }

    private void stopRefreshMenuAnimationIfPossible() {
        if (!isMyself() || !isOpenedFromMainPage()) {
            return;
        }

        if (finishedWatcher == null) {
            return;
        }

        finishedWatcher.getAndDecrement();
        if (finishedWatcher.get() == 0) {
            stopRefreshMenuAnimation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!getActivity().isChangingConfigurations() && isMyself() && isOpenedFromMainPage()) {
            savePositionToDB();
        }
    }

    @Override
    protected void onListViewScrollStop() {
        savePositionToPositionsCache();

    }

    private void savePositionToDB() {
        if (position == null) {
            savePositionToPositionsCache();
        }
        MyStatusDBTask.asyncUpdatePosition(position, BeeboApplication.getInstance().getCurrentAccountId());
    }

    private void savePositionToPositionsCache() {
        position = Utility.getCurrentPositionFromListView(getListView());
    }

    private void setListViewPositionFromPositionsCache() {

        Utility.setListViewSelectionFromTop(getListView(), position != null ? position.position : 0,
                position != null ? position.top : 0);

    }

    @Override
    public void onAnimationStart(Animator animation) {
        AnimationUtility.forceConvertActivityToTranslucent(getActivity());
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (getActivity() == null) {
            return;
        }

        AnimationUtility.forceConvertActivityFromTranslucent(getActivity());

        displayCoverPicture();

        if (isMyself() && isOpenedFromMainPage()) {
            readDBCache();
        } else {
            fetchLastestUserInfoFromServer();
            loadNewMsg();
            fetchTopicInfoFromServer();

        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    class HeaderPagerAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = null;
            switch (position) {
                case 0:
                    view = headerFirst;

                    break;
                case 1:
                    view = headerSecond;
                    break;
                case 2:
                    view = headerThird;
                    break;

            }
            container.addView(view, 0);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }

        @Override
        public int getCount() {
            return userBean.isVerified() ? 3 : 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == (View) object;
        }
    }

    private class TopicListTask extends MyAsyncTask<Void, ArrayList<String>, ArrayList<String>> {

        WeiboException e;

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            UserTopicListDao dao = new UserTopicListDao(BeeboApplication.getInstance().getAccessTokenHack(), userBean.getId());
            try {
                return dao.getGSONMsgList();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onCancelled(ArrayList<String> strings) {
            super.onCancelled(strings);
            stopRefreshMenuAnimationIfPossible();
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            super.onPostExecute(result);
            stopRefreshMenuAnimationIfPossible();

            if (isCancelled()) {
                return;
            }
            if (result == null || result.size() == 0) {
                return;
            }
            topicList = result;
            topicsCount.setText(Utility.convertStateNumberToString(getActivity(), String.valueOf(result.size())));
            ArrayList<String> dbCache = new ArrayList<String>();
            dbCache.addAll(topicList);
            TopicDBTask.asyncReplace(userBean.getId(), dbCache);
        }
    }

    // sina api has bug,so must refresh to get actual data
    public void forceReloadData(UserBean bean) {
        this.userBean = bean;
        fetchLastestUserInfoFromServer();
    }

    private void fetchLastestUserInfoFromServer() {
        if (Utility.isTaskStopped(refreshTask)) {
            refreshTask = new RefreshTask();
            refreshTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private class RefreshTask extends MyAsyncTask<Object, UserBean, UserBean> {

        WeiboException e;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected UserBean doInBackground(Object... params) {
            if (!isCancelled()) {
                ShowUserDao dao = new ShowUserDao(BeeboApplication.getInstance().getAccessTokenHack());
                boolean haveId = !TextUtils.isEmpty(userBean.getId());
                boolean haveName = !TextUtils.isEmpty(userBean.getScreen_name());
                if (haveId) {
                    dao.setUid(userBean.getId());
                } else if (haveName) {
                    dao.setScreen_name(userBean.getScreen_name());
                } else {
                    cancel(true);
                    return null;
                }

                UserBean user = null;
                try {
                    user = dao.getUserInfo();
                } catch (WeiboException e) {
                    this.e = e;
                    cancel(true);
                }
                if (user != null) {
                    userBean = user;
                } else {
                    cancel(true);
                }
                return user;
            } else {
                return null;
            }
        }

        @Override
        protected void onCancelled(UserBean userBean) {
            super.onCancelled(userBean);
            if (Utility.isAllNotNull(getActivity(), this.e)) {
                newMsgTipBar.setError(e.getError());
            }
            stopRefreshMenuAnimationIfPossible();
        }

        @Override
        protected void onPostExecute(UserBean o) {
            if (o == null || getActivity() == null) {
                return;
            }
            displayBasicInfo();
            displayCoverPicture();
            if (getActivity() instanceof UserInfoActivity) {
                ((UserInfoActivity) getActivity()).setUser(o);
                getActivity().invalidateOptionsMenu();
            }
            for (MessageBean msg : bean.getItemList()) {
                msg.setUser(o);
            }
            if (isMyself()) {
                BeeboApplication.getInstance().updateUserInfo(o);
                AccountDao.asyncUpdateMyProfile(BeeboApplication.getInstance().getAccountBean(), o);
            }
            getAdapter().notifyDataSetChanged();
            stopRefreshMenuAnimationIfPossible();
            super.onPostExecute(o);
        }

    }

    private class DBCacheTask extends MyAsyncTask<Void, ArrayList<String>, MyStatusTimeLineData> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressFooter.setVisibility(View.VISIBLE);

        }

        @Override
        protected MyStatusTimeLineData doInBackground(Void... params) {

            ArrayList<String> topicList = TopicDBTask.get(userBean.getId());
            publishProgress(topicList);
            return MyStatusDBTask.get(userBean.getId());
        }

        @Override
        protected void onProgressUpdate(ArrayList<String>... values) {
            super.onProgressUpdate(values);
            ArrayList<String> result = values[0];
            if (result == null || result.size() == 0) {
                return;
            }
            topicList = result;
            topicsCount.setText(Utility.convertStateNumberToString(getActivity(), String.valueOf(result.size())));
        }

        @Override
        protected void onPostExecute(MyStatusTimeLineData result) {
            super.onPostExecute(result);
            if (getActivity() == null) {
                return;
            }

            if (result != null && getActivity() != null) {
                getListView().removeFooterView(progressFooter);
                getDataList().addNewData(result.msgList);
                getAdapter().notifyDataSetChanged();
                position = result.position;
                setListViewPositionFromPositionsCache();
                getActivity().invalidateOptionsMenu();
                moreFooter.setVisibility(View.VISIBLE);

            }

            refreshLayout(getDataList());

            if (getDataList().getSize() == 0) {
                loadNewMsg();
            }
        }
    }

}
