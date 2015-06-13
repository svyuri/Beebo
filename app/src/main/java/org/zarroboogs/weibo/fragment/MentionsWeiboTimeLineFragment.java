
package org.zarroboogs.weibo.fragment;

import org.zarroboogs.util.net.WeiboException;
import org.zarroboogs.utils.Constants;
import org.zarroboogs.weibo.BeeboApplication;
import org.zarroboogs.weibo.R;
import org.zarroboogs.weibo.activity.BrowserWeiboMsgActivity;
import org.zarroboogs.weibo.activity.MainTimeLineActivity;
import org.zarroboogs.weibo.adapter.StatusListAdapter;
import org.zarroboogs.weibo.asynctask.MyAsyncTask;
import org.zarroboogs.weibo.bean.AccountBean;
import org.zarroboogs.weibo.bean.AsyncTaskLoaderResult;
import org.zarroboogs.weibo.bean.MentionTimeLineData;
import org.zarroboogs.weibo.bean.MessageBean;
import org.zarroboogs.weibo.bean.MessageListBean;
import org.zarroboogs.weibo.bean.MessageReCmtCountBean;
import org.zarroboogs.weibo.bean.TimeLinePosition;
import org.zarroboogs.weibo.bean.UnreadBean;
import org.zarroboogs.weibo.bean.UserBean;
import org.zarroboogs.weibo.dao.ClearUnreadDao;
import org.zarroboogs.weibo.dao.TimeLineReCmtCountDao;
import org.zarroboogs.weibo.db.task.MentionWeiboTimeLineDBTask;
import org.zarroboogs.weibo.fragment.base.AbsTimeLineFragment;
import org.zarroboogs.weibo.loader.MentionsWeiboMsgLoader;
import org.zarroboogs.weibo.loader.MentionsWeiboTimeDBLoader;
import org.zarroboogs.weibo.service.NotificationServiceHelper;
import org.zarroboogs.weibo.support.utils.AppEventAction;
import org.zarroboogs.weibo.support.utils.BundleArgsConstants;
import org.zarroboogs.weibo.support.utils.Utility;
import org.zarroboogs.weibo.widget.TopTipsView;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MentionsWeiboTimeLineFragment extends AbsTimeLineFragment<MessageListBean> {

    private AccountBean accountBean;

    private UserBean userBean;

    private String token;

    private UnreadBean unreadBean;

    private TimeLinePosition timeLinePosition;

    private MessageListBean bean = new MessageListBean();

    private final int POSITION_IN_PARENT_FRAGMENT = 0;

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public MessageListBean getDataList() {
        return bean;
    }

    public MentionsWeiboTimeLineFragment() {

    }

    public MentionsWeiboTimeLineFragment(AccountBean accountBean, UserBean userBean) {
        this.accountBean = accountBean;
        this.userBean = userBean;
        this.token = accountBean.getAccess_token_hack();
    }

    @Override
    public void onResume() {
        super.onResume();
        setListViewPositionFromPositionsCache();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(newBroadcastReceiver,
                new IntentFilter(AppEventAction.NEW_MSG_BROADCAST));
        // setActionBarTabCount(newMsgTipBar.getValues().size());
        getNewMsgTipBar().setOnChangeListener(new TopTipsView.OnChangeListener() {
            @Override
            public void onChange(int count) {
//                ((MainTimeLineActivity) getActivity()).setMentionsWeiboCount(count);
                // setActionBarTabCount(count);
            }
        });
        checkUnreadInfo();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!getActivity().isChangingConfigurations()) {
            saveTimeLinePositionToDB();
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(newBroadcastReceiver);

    }

    private void saveTimeLinePositionToDB() {
        timeLinePosition = Utility.getCurrentPositionFromListView(getListView());
        timeLinePosition.newMsgIds = newMsgTipBar.getValues();
        MentionWeiboTimeLineDBTask.asyncUpdatePosition(timeLinePosition, accountBean.getUid());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRetainInstance(false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newMsgTipBar.setType(TopTipsView.Type.ALWAYS);

    }

    @Override
    protected void onListViewScrollStop() {
        super.onListViewScrollStop();
        timeLinePosition = Utility.getCurrentPositionFromListView(getListView());
    }

    @Override
    protected void buildListAdapter() {
        StatusListAdapter adapter = new StatusListAdapter(this, getDataList().getItemList(), getListView(), true, false);
        adapter.setTopTipBar(newMsgTipBar);
        timeLineAdapter = adapter;
        getListView().setAdapter(timeLineAdapter);
    }

    private void checkUnreadInfo() {
        Loader loader = getLoaderManager().getLoader(DB_CACHE_LOADER_ID);
        if (loader != null) {
            return;
        }
        Intent intent = getActivity().getIntent();
        AccountBean intentAccount = intent.getParcelableExtra(BundleArgsConstants.ACCOUNT_EXTRA);
        MessageListBean mentionsWeibo = intent.getParcelableExtra(BundleArgsConstants.MENTIONS_WEIBO_EXTRA);
        UnreadBean unreadBeanFromNotification = intent.getParcelableExtra(BundleArgsConstants.UNREAD_EXTRA);

        if (accountBean.equals(intentAccount) && mentionsWeibo != null) {
            addUnreadMessage(mentionsWeibo);
            clearUnreadMentions(unreadBeanFromNotification);
            MessageListBean nullObject = null;
            intent.putExtra(BundleArgsConstants.MENTIONS_WEIBO_EXTRA, nullObject);
            getActivity().setIntent(intent);
        }
    }

    // private void setActionBarTabCount(int count) {
    // MentionsTimeLineFragment parent = (MentionsTimeLineFragment) getParentFragment();
    // ActionBar.Tab tab = parent.getWeiboTab();
    // if (tab == null) {
    // return;
    // }
    // String tabTag = (String) tab.getTag();
    // if (MentionsWeiboTimeLineFragment.class.getName().equals(tabTag)) {
    // View customView = tab.getCustomView();
    // TextView countTV = (TextView) customView.findViewById(R.id.tv_home_count);
    // countTV.setText(String.valueOf(count));
    // if (count > 0) {
    // countTV.setVisibility(View.VISIBLE);
    // } else {
    // countTV.setVisibility(View.GONE);
    // }
    // }
    // }

    @Override
    protected void newMsgLoaderSuccessCallback(MessageListBean newValue, Bundle loaderArgs) {
        if (getActivity() != null && newValue.getSize() > 0) {
            addNewDataAndRememberPosition(newValue);
        }
        unreadBean = null;
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(
                Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NotificationServiceHelper.getMentionsWeiboNotificationId(BeeboApplication.getInstance()
                .getAccountBean()));

    }

    private void addNewDataAndRememberPosition(final MessageListBean newValue) {
        int initSize = getDataList().getSize();
        if (getActivity() != null && newValue.getSize() > 0) {
            final boolean jumpToTop = getDataList().getSize() == 0;

            getDataList().addNewData(newValue);
            if (!jumpToTop) {
                int index = getListView().getFirstVisiblePosition();
                getAdapter().notifyDataSetChanged();
                int finalSize = getDataList().getSize();
                final int positionAfterRefresh = index + finalSize - initSize + getListView().getHeaderViewsCount();
                // use 1 px to show newMsgTipBar
                Utility.setListViewSelectionFromTop(getListView(), positionAfterRefresh, 1, new Runnable() {

                    @Override
                    public void run() {
                        newMsgTipBar.setValue(newValue, jumpToTop);
                    }
                });

            } else {
                newMsgTipBar.setValue(newValue, jumpToTop);
                newMsgTipBar.clearAndReset();
                getAdapter().notifyDataSetChanged();
                getListView().setSelection(0);
            }
            MentionWeiboTimeLineDBTask.asyncReplace(getDataList(), accountBean.getUid());
            saveTimeLinePositionToDB();
        }
    }

    protected void middleMsgLoaderSuccessCallback(int position, MessageListBean newValue, boolean towardsBottom) {

        if (newValue != null) {
            int size = newValue.getSize();

            if (getActivity() != null && newValue.getSize() > 0) {
                getDataList().addMiddleData(position, newValue, towardsBottom);

                if (towardsBottom) {
                    getAdapter().notifyDataSetChanged();
                } else {

                    View v = Utility.getListViewItemViewFromPosition(getListView(), position + 1 + 1);
                    int top = (v == null) ? 0 : v.getTop();
                    getAdapter().notifyDataSetChanged();
                    int ss = position + 1 + size - 1;
                    getListView().setSelectionFromTop(ss, top);
                }
            }
        }
    }

    @Override
    protected void oldMsgLoaderSuccessCallback(MessageListBean newValue) {
        if (newValue != null && newValue.getSize() > 1) {
            getDataList().addOldData(newValue);
            MentionWeiboTimeLineDBTask.asyncReplace(getDataList(), accountBean.getUid());
        } else {
//            Toast.makeText(getActivity(), getString(R.string.older_message_empty), Toast.LENGTH_SHORT).show();
            mTimeLineSwipeRefreshLayout.noMore();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.ACCOUNT, accountBean);
        outState.putParcelable(Constants.USERBEAN, userBean);
        outState.putString(Constants.TOKEN, token);

        if (getActivity().isChangingConfigurations()) {
            outState.putParcelable(Constants.BEAN, bean);
            outState.putParcelable("unreadBean", unreadBean);
            outState.putSerializable("timeLinePosition", timeLinePosition);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        switch (getCurrentState(savedInstanceState)) {
            case FIRST_TIME_START:
                getLoaderManager().initLoader(DB_CACHE_LOADER_ID, null, dbCallback);
                break;
            case ACTIVITY_DESTROY_AND_CREATE:
                userBean = (UserBean) savedInstanceState.getParcelable(Constants.USERBEAN);
                accountBean = (AccountBean) savedInstanceState.getParcelable(Constants.ACCOUNT);
                token = savedInstanceState.getString(Constants.TOKEN);
                unreadBean = (UnreadBean) savedInstanceState.getParcelable("unreadBean");
                timeLinePosition = (TimeLinePosition) savedInstanceState.getSerializable("timeLinePosition");

                Loader<MentionTimeLineData> loader = getLoaderManager().getLoader(DB_CACHE_LOADER_ID);
                if (loader != null) {
                    getLoaderManager().initLoader(DB_CACHE_LOADER_ID, null, dbCallback);
                }

                MessageListBean savedBean = (MessageListBean) savedInstanceState.getParcelable(Constants.BEAN);
                if (savedBean != null && savedBean.getSize() > 0) {
                    getDataList().replaceData(savedBean);
                    timeLineAdapter.notifyDataSetChanged();
                    refreshLayout(getDataList());
                } else {
                    getLoaderManager().initLoader(DB_CACHE_LOADER_ID, null, dbCallback);
                }

                break;
        }
    }

    @Override
    protected void onTimeListViewItemClick(AdapterView parent, View view, int position, long id) {
        startActivityForResult(
                BrowserWeiboMsgActivity.newIntent(BeeboApplication.getInstance().getAccountBean(),
                        bean.getItemList().get(position), token),
                MainTimeLineActivity.REQUEST_CODE_UPDATE_MENTIONS_WEIBO_TIMELINE_COMMENT_REPOST_COUNT);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // use Up instead of Back to reach this fragment
        if (data == null) {
            return;
        }
        final MessageBean msg = (MessageBean) data.getParcelableExtra("msg");
        if (msg != null) {
            for (int i = 0; i < getDataList().getSize(); i++) {
                if (msg.equals(getDataList().getItem(i))) {
                    MessageBean ori = getDataList().getItem(i);
                    if (ori.getComments_count() != msg.getComments_count()
                            || ori.getReposts_count() != msg.getReposts_count()) {
                        ori.setReposts_count(msg.getReposts_count());
                        ori.setComments_count(msg.getComments_count());
                        MentionWeiboTimeLineDBTask.asyncReplace(getDataList(), accountBean.getUid());
                        getAdapter().notifyDataSetChanged();
                    }
                    break;
                }
            }
        }
    }

    private void setListViewPositionFromPositionsCache() {
        Utility.setListViewSelectionFromTop(getListView(), timeLinePosition != null ? timeLinePosition.position : 0,
                timeLinePosition != null ? timeLinePosition.top : 0, new Runnable() {
                    @Override
                    public void run() {
                        setListViewUnreadTipBar(timeLinePosition);

                    }
                });

    }

    private void setListViewUnreadTipBar(TimeLinePosition p) {
        if (p != null && p.newMsgIds != null) {
            newMsgTipBar.setValue(p.newMsgIds);
            // setActionBarTabCount(newMsgTipBar.getValues().size());
//            ((MainTimeLineActivity) getActivity()).setMentionsWeiboCount(newMsgTipBar.getValues().size());
        }
    }

    private LoaderManager.LoaderCallbacks<MentionTimeLineData> dbCallback = new LoaderManager.LoaderCallbacks<MentionTimeLineData>() {
        @Override
        public Loader<MentionTimeLineData> onCreateLoader(int id, Bundle args) {
            getListView().setVisibility(View.INVISIBLE);
            return new MentionsWeiboTimeDBLoader(getActivity(), BeeboApplication.getInstance().getCurrentAccountId());
        }

        @Override
        public void onLoadFinished(Loader<MentionTimeLineData> loader, MentionTimeLineData result) {
            getListView().setVisibility(View.VISIBLE);

            if (result != null) {
                getDataList().replaceData(result.msgList);
                timeLinePosition = result.position;
            }

            getAdapter().notifyDataSetChanged();
            setListViewPositionFromPositionsCache();
            refreshLayout(bean);

            /**
             * when this account first open app,if he don't have any data in database,fetch data
             * from server automally
             */

            if (bean.getSize() == 0) {
//                mPullToRefreshListView.setRefreshing();
                getSwipeRefreshLayout().setRefreshing(true);
                loadNewMsg();
            } else {
                new RefreshReCmtCountTask(token).executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            }

            getLoaderManager().destroyLoader(loader.getId());

            checkUnreadInfo();

        }

        @Override
        public void onLoaderReset(Loader<MentionTimeLineData> loader) {

        }
    };

    protected Loader<AsyncTaskLoaderResult<MessageListBean>> onCreateNewMsgLoader(int id, Bundle args) {
        String accountId = accountBean.getUid();
        String sinceId = null;
        if (getDataList().getItemList().size() > 0) {
            sinceId = getDataList().getItemList().get(0).getId();
        }
        return new MentionsWeiboMsgLoader(getActivity(), accountId, token, sinceId, null);
    }

    protected Loader<AsyncTaskLoaderResult<MessageListBean>> onCreateMiddleMsgLoader(int id, Bundle args,
            String middleBeginId, String middleEndId,
            String middleEndTag, int middlePosition) {
        String accountId = accountBean.getUid();
        return new MentionsWeiboMsgLoader(getActivity(), accountId, token, middleBeginId, middleEndId);
    }

    protected Loader<AsyncTaskLoaderResult<MessageListBean>> onCreateOldMsgLoader(int id, Bundle args) {
        String accountId = accountBean.getUid();
        String maxId = null;
        if (getDataList().getItemList().size() > 0) {
            maxId = getDataList().getItemList().get(getDataList().getItemList().size() - 1).getId();
        }
        return new MentionsWeiboMsgLoader(getActivity(), accountId, token, null, maxId);
    }

    private BroadcastReceiver newBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AccountBean intentAccount = intent.getParcelableExtra(BundleArgsConstants.ACCOUNT_EXTRA);
            final UnreadBean unreadBean = intent.getParcelableExtra(BundleArgsConstants.UNREAD_EXTRA);
            if (intentAccount == null || !accountBean.equals(intentAccount)) {
                return;
            }
            MessageListBean data = intent.getParcelableExtra(BundleArgsConstants.MENTIONS_WEIBO_EXTRA);
            addUnreadMessage(data);
            clearUnreadMentions(unreadBean);
        }
    };

    private void addUnreadMessage(MessageListBean data) {
        if (data != null && data.getSize() > 0) {
            MessageBean last = data.getItem(data.getSize() - 1);
            boolean dup = getDataList().getItemList().contains(last);
            if (!dup) {
                addNewDataAndRememberPosition(data);
            }
        }
    }

    private class RefreshReCmtCountTask extends MyAsyncTask<Void, List<MessageReCmtCountBean>, List<MessageReCmtCountBean>> {

    	private List<String> msgIds;
    	private String mToken ;

    	
        public RefreshReCmtCountTask(String token) {
			super();
			// TODO Auto-generated constructor stub
			mToken = token;
		}

		@Override
        protected void onPreExecute() {
            super.onPreExecute();
            msgIds = new ArrayList<String>();
            List<MessageBean> msgList = getDataList().getItemList();
            for (MessageBean msg : msgList) {
                if (msg != null) {
                    msgIds.add(msg.getId());
                }
            }
        }

        @Override
        protected List<MessageReCmtCountBean> doInBackground(Void... params) {
            try {
                return new TimeLineReCmtCountDao(mToken, msgIds).get();
            } catch (WeiboException e) {
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<MessageReCmtCountBean> value) {
            super.onPostExecute(value);
            if (getActivity() == null || value == null) {
                return;
            }

            for (int i = 0; i < value.size(); i++) {
                MessageBean msg = getDataList().getItem(i);
                MessageReCmtCountBean count = value.get(i);
                if (msg != null && msg.getId().equals(count.getId())) {
                    msg.setReposts_count(count.getReposts());
                    msg.setComments_count(count.getComments());
                }
            }
            MentionWeiboTimeLineDBTask.asyncReplace(getDataList(), accountBean.getUid());
            getAdapter().notifyDataSetChanged();

        }

    }

    private void clearUnreadMentions(final UnreadBean data) {
        new MyAsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    new ClearUnreadDao(token)
                            .clearMentionStatusUnread(data, BeeboApplication
                                    .getInstance().getAccountBean().getUid());
                } catch (WeiboException ignored) {

                }
                return null;
            }
        }.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    @Override
    protected void newMsgLoaderFailedCallback(WeiboException exception) {
    	if (exception.getError().trim().equals("用户请求超过上限")) {
    		token = accountBean.getAccess_token_hack();
		}
    	Toast.makeText(getActivity(), exception.getError(), Toast.LENGTH_SHORT).show();;
    }
}
