package org.zarroboogs.weibo.fragment;

import java.util.ArrayList;
import java.util.List;

import org.zarroboogs.devutils.DevLog;
import org.zarroboogs.msrl.widget.MaterialSwipeRefreshLayout;
import org.zarroboogs.senior.sdk.SeniorUrl;
import org.zarroboogs.weibo.BeeboApplication;
import org.zarroboogs.weibo.activity.BrowserWeiboMsgActivity;
import org.zarroboogs.weibo.adapter.HotWeiboStatusListAdapter;
import org.zarroboogs.weibo.bean.MessageBean;
import org.zarroboogs.weibo.bean.MessageListBean;
import org.zarroboogs.weibo.hot.bean.hotweibo.HotWeiboPicInfos;
import org.zarroboogs.weibo.hot.hean.HotWeiboBean;
import org.zarroboogs.weibo.hot.hean.HotWeiboErrorBean;
import org.zarroboogs.weibo.setting.SettingUtils;
import org.zarroboogs.weibo.support.asyncdrawable.MsgDetailReadWorker;
import org.zarroboogs.weibo.support.utils.Utility;
import org.zarroboogs.weibo.widget.TopTipsView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class HotWeiboFragment extends BaseHotWeiboFragment {

    private MsgDetailReadWorker picTask;
    
    private HotWeiboStatusListAdapter adapter;

    private List<MessageBean> repostList = new ArrayList<MessageBean>();
    
    private static final int OLD_REPOST_LOADER_ID = 4;

    private View footerView;

    private ActionMode actionMode;

    private boolean canLoadOldRepostData = true;

    private int mPage = 1;
    
    private AsyncHttpClient mAsyncHttoClient = new AsyncHttpClient();

    private String mCtg;

    public HotWeiboFragment( ) {
        super();
    }

    @SuppressLint("ValidFragment")
    public HotWeiboFragment(String ctg ) {
		super();
		// TODO Auto-generated constructor stub
		this.mCtg = ctg;
        if (this.mCtg.equals("8999")){
            this.mCtg = "-1";
        }
	}

	@Override
    public void onResume() {
        super.onResume();
        getListView().setFastScrollEnabled(SettingUtils.allowFastScroll());
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return super.onCreateView(inflater, container, savedInstanceState);
	}

    @Override
    public void scrollToTop() {
        Utility.stopListViewScrollingAndScrollToTop(getListView());
    }

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
		
        getListView().setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Intent intent = BrowserWeiboMsgActivity.newIntent(BeeboApplication.getInstance().getAccountBean(),
                        (MessageBean) adapter.getItem(position), BeeboApplication.getInstance().getAccessToken());
                startActivity(intent);
            }
        });

        getSwipeRefreshLayout().setOnRefreshLoadMoreListener(new MaterialSwipeRefreshLayout.OnRefreshLoadMoreListener() {
            @Override
            public void onRefresh() {
                long uid = Long.valueOf(BeeboApplication.getInstance().getAccountBean().getUid());
                String url = SeniorUrl.hotWeiboApi(BeeboApplication.getInstance().getAccountBean().getGsid(), mCtg, mPage, uid);
                DevLog.printLog("HotWeiboFragment_get: ", url);
                loadData(url);
            }

            @Override
            public void onLoadMore() {
//                if (msg.getReposts_count() > 0 && repostList.size() > 0) {
//                    loadOldRepostData();
//                }
            }
        });
//        getSwipeRefreshLayout().setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                long uid = Long.valueOf(BeeboApplication.getInstance().getAccountBean().getUid());
//                String url = SeniorUrl.hotWeiboApi(BeeboApplication.getInstance().getAccountBean().getGsid(), mCtg, mPage, uid);
//                DevLog.printLog("HotWeiboFragment_get: ", url);
//                loadData(url);
//            }
//        });
//        getPullToRefreshListView().setOnRefreshListener(new OnRefreshListener<ListView>() {
//
//			@Override
//			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
//				// TODO Auto-generated method stub
//                long uid = Long.valueOf(BeeboApplication.getInstance().getAccountBean().getUid());
//                String url = SeniorUrl.hotWeiboApi(BeeboApplication.getInstance().getAccountBean().getGsid(), mCtg, mPage, uid);
//                DevLog.printLog("HotWeiboFragment_get: ", url);
//                loadData(url);
//
//				getPullToRefreshListView().setRefreshing();
//			}
//		});
	}


    public void clearActionMode() {
        if (actionMode != null) {

            actionMode.finish();
            actionMode = null;
        }
        if (getListView() != null && getListView().getCheckedItemCount() > 0) {
            getListView().clearChoices();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void setActionMode(ActionMode mActionMode) {
        this.actionMode = mActionMode;
    }

    public boolean hasActionMode() {
        return actionMode != null;
    }

    private boolean resetActionMode() {
        if (actionMode != null) {
            getListView().clearChoices();
            actionMode.finish();
            actionMode = null;
            return true;
        } else {
            return false;
        }
    }

//    private PullToRefreshBase.OnLastItemVisibleListener onLastItemVisibleListener = new PullToRefreshBase.OnLastItemVisibleListener() {
//        @Override
//        public void onLastItemVisible() {
////        	if (msg.getReposts_count() > 0 && repostList.size() > 0) {
////                loadOldRepostData();
////            }
//        }
//    };

    private AbsListView.OnScrollListener listViewOnScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            if (hasActionMode()) {
                int position = getListView().getCheckedItemPosition();
                if (getListView().getFirstVisiblePosition() > position || getListView().getLastVisiblePosition() < position) {
                    clearActionMode();
                }
            }

            if (getListView().getLastVisiblePosition() > 7
                    && getListView().getFirstVisiblePosition() != getListView().getHeaderViewsCount()) {

            	if (getListView().getLastVisiblePosition() > repostList.size() - 3) {
                    loadOldRepostData();
                }
            }
        }
    };



    

    public void loadOldRepostData() {
        if (getLoaderManager().getLoader(OLD_REPOST_LOADER_ID) != null || !canLoadOldRepostData) {
            return;
        }
        showFooterView();

    }


	@Override
	protected void buildListAdapter() {
		// TODO Auto-generated method stub
        adapter = new HotWeiboStatusListAdapter(this, repostList, getListView(), true, false);
        adapter.setTopTipBar(newMsgTipBar);
        timeLineAdapter = adapter;
        getListView().setAdapter(timeLineAdapter);
	}

	@Override
	void onLoadDataSucess(String json) {
		// TODO Auto-generated method stub
		mPage++;
		String jsonStr = json.replaceAll("\"geo\":\"\"", "\"geo\": {}").replace("},\"mblogid\":", "],\"mblogid\":").replace("\"pic_infos\":{", "\"pic_infos\":[").replaceAll("\"[A-Za-z0-9]{32}\":", "");
		Utility.printLongLog("READ_JSON_DONE", jsonStr);
		
		Gson gson = new Gson();
		
		HotWeiboErrorBean error = gson.fromJson(jsonStr, HotWeiboErrorBean.class);
		if (error != null && TextUtils.isEmpty(error.getErrmsg())) {
			HotWeiboBean result = gson.fromJson(jsonStr, new TypeToken<HotWeiboBean>() {}.getType());
			
			
			MessageListBean mslBean = result.getMessageListBean();
			getDataList().addNewData(mslBean);
			List<MessageBean> list = result.getMessageBeans();
			
			
			for (MessageBean messageBean : list) {
				List<HotWeiboPicInfos> picInfos = messageBean.getPic_infos();
				DevLog.printLog("getOriginal_pic ", "HotWeiboPicInfos:" + picInfos.size() + "  Original: " + messageBean.getOriginal_pic() + "     Middle: " +  messageBean.getBmiddle_pic());
			}
			if (SettingUtils.isReadStyleEqualWeibo()) {
				newMsgTipBar.setValue(mslBean, true);
				adapter.addNewData(list);
				adapter.notifyDataSetChanged();
			}else {
				addNewDataAndRememberPosition(list, mslBean);
			}

		}else {
            DevLog.printLog("ERROR", error.getErrmsg());
		}
		
        getSwipeRefreshLayout().setRefreshing(false);
	}

    private void addNewDataAndRememberPosition(final List<MessageBean> newValue, final MessageListBean mb) {

        int initSize = getListView().getCount();

        if (getActivity() != null && newValue.size() > 0) {
            adapter.addNewData(newValue);
            int index = getListView().getFirstVisiblePosition();
            adapter.notifyDataSetChanged();
            int finalSize = getListView().getCount();
            final int positionAfterRefresh = index + finalSize - initSize + getListView().getHeaderViewsCount();
            // use 1 px to show newMsgTipBar
            Utility.setListViewSelectionFromTop(getListView(), positionAfterRefresh, 1, new Runnable() {

                @Override
                public void run() {
                	 newMsgTipBar.setValue(mb, false);
                     newMsgTipBar.setType(TopTipsView.Type.AUTO);
                }
            });

        }

    }
    
	@Override
	void onLoadDataFailed(String errorStr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void onLoadDataStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void onGsidLoadSuccess(String gsid) {
		// TODO Auto-generated method stub
        long uid = Long.valueOf(BeeboApplication.getInstance().getAccountBean().getUid());
		loadData(SeniorUrl.hotWeiboApi(BeeboApplication.getInstance().getAccountBean().getGsid(), mCtg, mPage, uid));
	}

	@Override
	void onGsidLoadFailed(String errorStr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void onPageSelected() {
		// TODO Auto-generated method stub
		DevLog.printLog("onViewPageSelected", "onPageSelected-----");
        long uid = Long.valueOf(BeeboApplication.getInstance().getAccountBean().getUid());
        loadData(SeniorUrl.hotWeiboApi(BeeboApplication.getInstance().getAccountBean().getGsid(), mCtg, mPage, uid));
	}


}
