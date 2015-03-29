
package org.zarroboogs.weibo.activity;

import org.zarroboogs.weibo.R;
import org.zarroboogs.weibo.adapter.SearchSuggestionProvider;
import org.zarroboogs.weibo.fragment.SearchStatusFragment;
import org.zarroboogs.weibo.fragment.SearchUserFragment;
import org.zarroboogs.weibo.fragment.base.AbsTimeLineFragment;
import org.zarroboogs.weibo.support.lib.AppFragmentPagerAdapter;
import org.zarroboogs.weibo.support.utils.ViewUtility;

import com.umeng.analytics.MobclickAgent;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

public class SearchMainActivity extends SharedPreferenceActivity {
	private static final String KEY_SEARCH = "search_key";
	public enum SearchWhat{
		user,
		status
	}
	
	private Toolbar mToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_main_activity_layout);
        mToolbar = ViewUtility.findViewById(this, R.id.searchToolbar);
        
        SearchWhat sw = getSearchWhat();
        if (sw == SearchWhat.status) {
            mToolbar.setTitle("搜索微博");
		}else {
			mToolbar.setTitle("搜索用户");
		}

        
        buildViewPager();
        buildActionBarAndViewPagerTitles();

        handleIntent(getIntent());
        
        buildContent();
        disPlayHomeAsUp(R.id.searchToolbar);
        
        
        
    }

	private SearchWhat getSearchWhat() {
		String title = getSPs().getString(KEY_SEARCH, SearchWhat.status.toString());
        SearchWhat sw = Enum.valueOf(SearchWhat.class, title);
		return sw;
	}

    private void buildContent() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (getSupportFragmentManager().findFragmentByTag(SearchStatusFragment.class.getName()) == null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.searchContent, new SearchStatusFragment(),SearchStatusFragment.class.getName())
                            .commitAllowingStateLoss();
                    getSupportFragmentManager().executePendingTransactions();
                }
            }
        });
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

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SearchSuggestionProvider.AUTHORITY,
                    SearchSuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            search(query);
        }
    }

    private void buildViewPager() {
//        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        SearchTabPagerAdapter adapter = new SearchTabPagerAdapter(getSupportFragmentManager());
//        mViewPager.setOffscreenPageLimit(2);
//        mViewPager.setAdapter(adapter);
//        mViewPager.setOnPageChangeListener(onPageChangeListener);
    }

    private void buildActionBarAndViewPagerTitles() {

//        actionBar.setTitle(getString(R.string.search));
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//
//        actionBar.addTab(actionBar.newTab().setText(getString(R.string.status)).setTabListener(tabListener));
//
//        actionBar.addTab(actionBar.newTab().setText(getString(R.string.user)).setTabListener(tabListener));

    }

//    ActionBar.TabListener tabListener = new ActionBar.TabListener() {
//
//        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
//
//            if (mViewPager.getCurrentItem() != tab.getPosition()) {
//                mViewPager.setCurrentItem(tab.getPosition());
//            }
//
//        }
//
//        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
//
//        }
//
//        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
//
//        }
//    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_searchmainactivity, menu);
//        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
//        searchView.setIconifiedByDefault(false);
//        searchView.setSubmitButtonEnabled(false);
//        searchView.requestFocus();
        return super.onCreateOptionsMenu(menu);

    }

    public String getSearchWord() {
        return this.q;
    }

    private String q;

    private void search(final String q) {
//        if (!TextUtils.isEmpty(q)) {
//            this.q = q;
//            switch (mViewPager.getCurrentItem()) {
//                case 0:
//                    ((SearchStatusFragment) getSearchStatusFragment()).search();
//                    break;
//                case 1:
//                    ((SearchUserFragment) getSearchUserFragment()).search();
//                    break;
//            }
//        }
    }

    private void showInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = MainTimeLineActivity.newIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }

        return true;
    }

    private Fragment getSearchUserFragment() {
        return getSupportFragmentManager().findFragmentByTag(SearchUserFragment.class.getName());
    }

    private AbsTimeLineFragment getSearchStatusFragment() {
        return (AbsTimeLineFragment) getSupportFragmentManager().findFragmentByTag(SearchStatusFragment.class.getName());
    }

    ViewPager.SimpleOnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            // getActionBar().setSelectedNavigationItem(position);
        }
    };

    private class SearchTabPagerAdapter extends AppFragmentPagerAdapter {

        List<Fragment> list = new ArrayList<Fragment>();

        public SearchTabPagerAdapter(FragmentManager fm) {
            super(fm);
            if (getSearchStatusFragment() == null) {
                list.add(new SearchStatusFragment());
            } else {
                list.add(getSearchStatusFragment());
            }

            if (getSearchUserFragment() == null) {
                list.add(new SearchUserFragment());
            } else {
                list.add(getSearchUserFragment());
            }

        }

        public Fragment getItem(int position) {
            return list.get(position);
        }

        @Override
        protected String getTag(int position) {
            List<String> tagList = new ArrayList<String>();
            tagList.add(SearchStatusFragment.class.getName());
            tagList.add(SearchUserFragment.class.getName());
            return tagList.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }

    }
}
