package org.zarroboogs.weibo.fragment;

import java.util.ArrayList;
import java.util.List;

import org.zarroboogs.msrl.widget.MaterialSwipeRefreshLayout;
import org.zarroboogs.senior.sdk.SeniorUrl;
import org.zarroboogs.weibo.BeeboApplication;
import org.zarroboogs.weibo.MyAnimationListener;
import org.zarroboogs.weibo.R;
import org.zarroboogs.weibo.activity.SearchTopicByNameActivity;
import org.zarroboogs.weibo.adapter.HotHuaTiAdapter;
import org.zarroboogs.weibo.hot.bean.huati.HotHuaTi;
import org.zarroboogs.weibo.hot.bean.huati.HotHuaTiCard;
import org.zarroboogs.weibo.hot.bean.huati.HotHuaTiCardGroup;
import org.zarroboogs.weibo.setting.SettingUtils;
import org.zarroboogs.weibo.support.asyncdrawable.MsgDetailReadWorker;
import org.zarroboogs.weibo.support.utils.Utility;
import org.zarroboogs.weibo.support.utils.ViewUtility;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HotHuaTiFragment extends BaseHotHuaTiFragment {

    private MsgDetailReadWorker picTask;
    
    private ListView listView;

    private MaterialSwipeRefreshLayout mSwipeRefreshLayout;

    private HotHuaTiAdapter adapter;

    private List<HotHuaTiCardGroup> repostList = new ArrayList<HotHuaTiCardGroup>();

    private static final int OLD_REPOST_LOADER_ID = 4;

    private ActionMode actionMode;

    private boolean canLoadOldRepostData = true;

    private int mPage = 1;
    
    private AsyncHttpClient mAsyncHttoClient = new AsyncHttpClient();

    private String mCtg;

    public HotHuaTiFragment(){
        super();
    }
    @SuppressLint("ValidFragment")
    public HotHuaTiFragment(String ctg) {
		super();
		// TODO Auto-generated constructor stub
		this.mCtg = ctg;
	}

	@Override
    public void onResume() {
        super.onResume();
        getListView().setFastScrollEnabled(SettingUtils.allowFastScroll());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout swipeFrameLayout = (RelativeLayout) inflater.inflate(R.layout.hotweibo_fragment_layout,container, false);

        mSwipeRefreshLayout = ViewUtility.findViewById(swipeFrameLayout,R.id.hotWeiboSRL);
        mSwipeRefreshLayout.setEnableSount(SettingUtils.getEnableSound());
        mSwipeRefreshLayout.noMore();

        listView = (ListView) swipeFrameLayout.findViewById(R.id.pullToFreshView);

//        pullToRefreshListView.setOnLastItemVisibleListener(onLastItemVisibleListener);
        listView.setOnScrollListener(listViewOnScrollListener);

        mSwipeRefreshLayout.setOnRefreshLoadMoreListener(new MaterialSwipeRefreshLayout.OnRefreshLoadMoreListener() {
            @Override
            public void onRefresh() {
                loadData(SeniorUrl.hotHuaTiApi(getGsid(), mCtg, mPage, Long.valueOf(BeeboApplication.getInstance().getAccountBean().getUid())));
            }

            @Override
            public void onLoadMore() {

            }
        });

        dismissFooterView();

//        repostTab.setOnClickListener(new RepostTabOnClickListener());

        listView.setFooterDividersEnabled(false);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        // listView.setOnItemLongClickListener(commentOnItemLongClickListener);

//        initView(swipeFrameLayout, savedInstanceState);
        adapter = new HotHuaTiAdapter(this.getActivity());
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        listView.setHeaderDividersEnabled(false);

		
//		pullToRefreshListView.setRefreshing();
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				HotHuaTiCardGroup g = (HotHuaTiCardGroup) (adapter).getItem(position);
                Intent intent = new Intent(getActivity(), SearchTopicByNameActivity.class);
                String str = g.getTitle_sub();
                intent.putExtra("q", str.substring(1, str.length() - 1));
                startActivity(intent);
			}
		});
        return swipeFrameLayout;
    }

    protected void showFooterView() {

    }

    protected void dismissFooterView() {

    }

    protected void showErrorFooterView() {

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

    public ListView getListView() {
        return listView;
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





//
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

    private void addNewDataAndRememberPosition(final List<HotHuaTiCardGroup> newValue) {

    	Utility.printLongLog("HUATI_", "newValue Size: " + newValue.size());
    	for (HotHuaTiCardGroup hotHuaTiCardGroup : newValue) {
    		Utility.printLongLog("HUATI_", "HuaTi Text: " + hotHuaTiCardGroup.getDesc1());
		}
        int initSize = getListView().getCount();

        if (getActivity() != null) {
            adapter.addNewData(newValue);
            int index = getListView().getFirstVisiblePosition();
            adapter.notifyDataSetChanged();
            int finalSize = getListView().getCount();
            final int positionAfterRefresh = index + finalSize - initSize + getListView().getHeaderViewsCount();
            // use 1 px to show newMsgTipBar
            Utility.setListViewSelectionFromTop(getListView(), positionAfterRefresh, 1, new Runnable() {

                @Override
                public void run() {

                }
            });

        }

    }
    

    public void loadOldRepostData() {
        if (getLoaderManager().getLoader(OLD_REPOST_LOADER_ID) != null || !canLoadOldRepostData) {
            return;
        }
        showFooterView();

    }

	@Override
	void onLoadDataSucess(String json) {
		// TODO Auto-generated method stub
		mPage++;
		String jsonStr = json.replaceAll("\"geo\":\"\"", "\"geo\": {}");
		Utility.printLongLog("READ_JSON_DONE-GET_DATE_FROM_NET", json);
		
		HotHuaTi huati = new Gson().fromJson(jsonStr, new TypeToken<HotHuaTi>() {}.getType());
		List<HotHuaTiCard> cards = huati.getCards();
		
		List<HotHuaTiCardGroup> result = new ArrayList<HotHuaTiCardGroup>();
		for (HotHuaTiCard c : cards) {
			List<HotHuaTiCardGroup> group = c.getCard_group();
			if (group != null) {
				result.addAll(group);
			}
		}
		
		if (SettingUtils.isReadStyleEqualWeibo()) {
			adapter.addNewData(result);
			adapter.notifyDataSetChanged();
		}else {
			addNewDataAndRememberPosition(result);
		}
		
        mSwipeRefreshLayout.setRefreshing(false);
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
		loadData(SeniorUrl.hotHuaTiApi(getGsid(), mCtg, mPage, Long.valueOf(BeeboApplication.getInstance().getAccountBean().getUid())));
	}

	@Override
	void onGsidLoadFailed(String errorStr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void onPageSelected() {
		// TODO Auto-generated method stub
        loadData(SeniorUrl.hotHuaTiApi(getGsid(), mCtg, mPage, Long.valueOf(BeeboApplication.getInstance().getAccountBean().getUid())));

    }

}
