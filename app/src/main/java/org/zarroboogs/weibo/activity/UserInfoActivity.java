
package org.zarroboogs.weibo.activity;

import org.zarroboogs.util.net.WeiboException;
import org.zarroboogs.utils.AppLoggerUtils;
import org.zarroboogs.utils.Constants;
import org.zarroboogs.utils.ErrorCode;
import org.zarroboogs.weibo.BeeboApplication;
import org.zarroboogs.weibo.R;
import org.zarroboogs.weibo.asynctask.MyAsyncTask;
import org.zarroboogs.weibo.bean.AsyncTaskLoaderResult;
import org.zarroboogs.weibo.bean.UserBean;
import org.zarroboogs.weibo.dao.FanDao;
import org.zarroboogs.weibo.dao.FriendshipsDao;
import org.zarroboogs.weibo.dao.ModifyGroupMemberDao;
import org.zarroboogs.weibo.dao.RemarkDao;
import org.zarroboogs.weibo.dao.ShowUserDao;
import org.zarroboogs.weibo.db.task.FilterDBTask;
import org.zarroboogs.weibo.dialogfragment.CommonErrorDialogFragment;
import org.zarroboogs.weibo.dialogfragment.CommonProgressDialogFragment;
import org.zarroboogs.weibo.dialogfragment.ManageGroupDialog;
import org.zarroboogs.weibo.dialogfragment.UpdateRemarkDialog;
import org.zarroboogs.weibo.fragment.UserInfoFragment;
import org.zarroboogs.weibo.loader.AbstractAsyncNetRequestTaskLoader;
import org.zarroboogs.weibo.support.utils.AnimationUtility;
import org.zarroboogs.weibo.support.utils.BundleArgsConstants;
import org.zarroboogs.weibo.support.utils.Utility;

import com.umeng.analytics.MobclickAgent;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

public class UserInfoActivity extends AbstractAppActivity {

    private String token;

    private UserBean bean;

    private UserInfoFragment userInfoFragment;
    private MyAsyncTask<Void, UserBean, UserBean> followOrUnfollowTask;

    private ModifyGroupMemberTask modifyGroupMemberTask;

    private static final int REFRESH_LOADER_ID = 0;

    private Toolbar mUserInfoToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userinfoactivity_layout);
        mUserInfoToolbar = (Toolbar) findViewById(R.id.userInfoToolBar);
        
        initLayout();
        token =  BeeboApplication.getInstance().getAccessTokenHack();
        bean = getIntent().getParcelableExtra("user");
        if (bean == null) {
            String id = getIntent().getStringExtra("id");
            if (!TextUtils.isEmpty(id)) {
                bean = new UserBean();
                bean.setId(id);
            } else {
                String domain = getIntent().getStringExtra("domain");
                if (!TextUtils.isEmpty(domain)) {
                    bean = new UserBean();
                    bean.setDomain(domain);
                } else {
                    Uri data = getIntent().getData();
                    if (data != null) {
                        String d = data.toString();
                        int index = d.lastIndexOf("@");
                        String newValue = d.substring(index + 1);
                        bean = new UserBean();
                        bean.setScreen_name(newValue);
                    }
                }
            }
            fetchUserInfoFromServer();
            // findViewById(android.R.id.content).setBackgroundDrawable(ThemeUtility.getDrawable(android.R.attr.windowBackground));
        } else {
            // findViewById(android.R.id.content).setBackgroundDrawable(ThemeUtility.getDrawable(android.R.attr.windowBackground));
            buildContent();
        }

        if (isMyselfProfile()) {
            if (getClass() == MyInfoActivity.class) {
                return;
            }
            Intent intent = new Intent(this, MyInfoActivity.class);
            intent.putExtra(Constants.TOKEN, getToken());

            UserBean userBean = new UserBean();
            userBean.setId(BeeboApplication.getInstance().getCurrentAccountId());
            intent.putExtra("user", bean);
            intent.putExtra(Constants.ACCOUNT, BeeboApplication.getInstance().getAccountBean());
            startActivity(intent);
            finish();
        }

        mUserInfoToolbar.setTitle("个人信息");
        
        disPlayHomeAsUp(R.id.userInfoToolBar);
        
        
    }
    
    public Toolbar getToolbar(){
    	return mUserInfoToolbar;
    }

    public String getToken() {
        if (TextUtils.isEmpty(token)) {
            token = BeeboApplication.getInstance().getAccessTokenHack();
        }
        return token;
    }

    public UserBean getUser() {
        return bean;
    }

    public void setUser(UserBean bean) {
        this.bean = bean;
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getName());
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utility.cancelTasks(followOrUnfollowTask, modifyGroupMemberTask);
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        MobclickAgent.onPageStart(this.getClass().getName());
        MobclickAgent.onResume(this);
    }

    public boolean isMyselfProfile() {
        boolean screenNameEqualCurrentAccount = bean.getScreen_name() != null
                && bean.getScreen_name().equals(BeeboApplication.getInstance().getCurrentAccountName());
        boolean idEqualCurrentAccount = bean.getId() != null
                && bean.getId().equals(BeeboApplication.getInstance().getCurrentAccountId());
        return screenNameEqualCurrentAccount || idEqualCurrentAccount;
    }

    private void fetchUserInfoFromServer() {

        CommonProgressDialogFragment dialog = CommonProgressDialogFragment
                .newInstance(getString(R.string.fetching_user_info));
        getSupportFragmentManager().beginTransaction().add(dialog, CommonProgressDialogFragment.class.getName()).commit();
        getSupportLoaderManager().initLoader(REFRESH_LOADER_ID, null, refreshCallback);
    }

    private void initLayout() {
        getWindow().setBackgroundDrawable(getResources().getDrawable(R.color.transparent));
        mUserInfoToolbar.setTitle(getString(R.string.personal_info));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// TODO Auto-generated method stub
    	Intent intent;
        int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			intent = MainTimeLineActivity.newIntent();
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			return true;
		} else if (itemId == R.id.menu_edit) {
			intent = new Intent(UserInfoActivity.this, EditMyProfileActivity.class);
			intent.putExtra(Constants.USERBEAN, BeeboApplication.getInstance().getAccountBean().getInfo());
			startActivity(intent);
			return true;
		} else if (itemId == R.id.menu_at) {
			intent = new Intent(UserInfoActivity.this, WriteWeiboWithAppSrcActivity.class);
			intent.putExtra(Constants.TOKEN, getToken());
			intent.putExtra("content", "@" + bean.getScreen_name());
			intent.putExtra(BundleArgsConstants.ACCOUNT_EXTRA, BeeboApplication.getInstance().getAccountBean());
			startActivity(intent);
		} else if (itemId == R.id.menu_modify_remark) {
			UpdateRemarkDialog dialog = new UpdateRemarkDialog();
			dialog.show(getFragmentManager(), "");
		} else if (itemId == R.id.menu_follow) {
			if (followOrUnfollowTask == null || followOrUnfollowTask.getStatus() == MyAsyncTask.Status.FINISHED) {
			    followOrUnfollowTask = new FollowTask();
			    followOrUnfollowTask.execute();
			}
		} else if (itemId == R.id.menu_unfollow) {
			if (followOrUnfollowTask == null || followOrUnfollowTask.getStatus() == MyAsyncTask.Status.FINISHED) {
			    followOrUnfollowTask = new UnFollowTask();
			    followOrUnfollowTask.execute();
			}
		} else if (itemId == R.id.menu_remove_fan) {
			if (followOrUnfollowTask == null || followOrUnfollowTask.getStatus() == MyAsyncTask.Status.FINISHED) {
			    followOrUnfollowTask = new RemoveFanTask();
			    followOrUnfollowTask.execute();
			}
		} else if (itemId == R.id.menu_add_to_app_filter) {
			if (!TextUtils.isEmpty(bean.getScreen_name())) {
			    FilterDBTask.addFilterKeyword(FilterDBTask.TYPE_USER, bean.getScreen_name());
			    Toast.makeText(UserInfoActivity.this, getString(R.string.filter_successfully), Toast.LENGTH_SHORT).show();
			}
		} else if (itemId == R.id.menu_manage_group) {
			manageGroup();
		} else if (itemId == R.id.menu_refresh_my_profile) {
			userInfoFragment.refreshMyProFile();
			return true;
		}
    	return super.onOptionsItemSelected(item);
    }
    private void buildContent() {
        // if you open this activity with user id, must set title with nickname
        // again
        // getActionBar().setTitle(bean.getScreen_name());
//        mUserInfoToolbar.setTitle(bean.getScreen_name());
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (getSupportFragmentManager().findFragmentByTag(UserInfoFragment.class.getName()) == null) {
                    userInfoFragment = UserInfoFragment.newInstance(mUserInfoToolbar,getUser(), getToken());
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content, userInfoFragment, UserInfoFragment.class.getName()).commit();
                    getSupportFragmentManager().executePendingTransactions();

                    AnimationUtility.translateFragmentY(userInfoFragment, -400, 0, userInfoFragment);

                }
                
            }
        });

    }

    public void updateRemark(String remark) {

        new UpdateRemarkTask(remark).executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
    }

    private UserInfoFragment getInfoFragment() {
        return ((UserInfoFragment) getSupportFragmentManager().findFragmentByTag(UserInfoFragment.class.getName()));
    }

    private void manageGroup() {
        ManageGroupDialog dialog = new ManageGroupDialog(BeeboApplication.getInstance().getGroup(), bean.getId());
        dialog.show(getSupportFragmentManager(), "");

    }

    public void handleGroup(List<String> add, List<String> remove) {
        if (Utility.isTaskStopped(modifyGroupMemberTask)) {
            modifyGroupMemberTask = new ModifyGroupMemberTask(add, remove);
            modifyGroupMemberTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private class ModifyGroupMemberTask extends MyAsyncTask<Void, Void, Void> {

        List<String> add;

        List<String> remove;

        public ModifyGroupMemberTask(List<String> add, List<String> remove) {
            this.add = add;
            this.remove = remove;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ModifyGroupMemberDao dao = new ModifyGroupMemberDao(token, bean.getId());
            for (String id : add) {
                try {
                    dao.add(id);
                } catch (WeiboException e) {
                    AppLoggerUtils.e(e.getMessage());
                    cancel(true);
                }
            }
            for (String id : remove) {
                try {
                    dao.delete(id);
                } catch (WeiboException e) {
                    AppLoggerUtils.e(e.getMessage());
                    cancel(true);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(UserInfoActivity.this, getString(R.string.modify_successfully), Toast.LENGTH_SHORT).show();
        }
    }

    private class UnFollowTask extends MyAsyncTask<Void, UserBean, UserBean> {

        WeiboException e;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected UserBean doInBackground(Void... params) {

            FriendshipsDao dao = new FriendshipsDao(BeeboApplication.getInstance().getAccessTokenHack());
            if (!TextUtils.isEmpty(bean.getId())) {
                dao.setUid(bean.getId());
            } else {
                dao.setScreen_name(bean.getScreen_name());
            }

            try {
                return dao.unFollowIt();
            } catch (WeiboException e) {
                AppLoggerUtils.e(e.getError());
                this.e = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onCancelled(UserBean userBean) {
            super.onCancelled(userBean);
        }

        @Override
        protected void onPostExecute(UserBean o) {
            super.onPostExecute(o);
            Toast.makeText(UserInfoActivity.this, getString(R.string.unfollow_successfully), Toast.LENGTH_SHORT).show();
            bean = o;
            bean.setFollowing(false);
            invalidateOptionsMenu();
        }
    }

    private class FollowTask extends MyAsyncTask<Void, UserBean, UserBean> {

        WeiboException e;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected UserBean doInBackground(Void... params) {

            FriendshipsDao dao = new FriendshipsDao(BeeboApplication.getInstance().getAccessTokenHack());
            if (!TextUtils.isEmpty(bean.getId())) {
                dao.setUid(bean.getId());
            } else {
                dao.setScreen_name(bean.getScreen_name());
            }
            try {
                return dao.followIt();
            } catch (WeiboException e) {
                AppLoggerUtils.e(e.getError());
                this.e = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onCancelled(UserBean userBean) {
            super.onCancelled(userBean);
            if (e != null) {
                Toast.makeText(UserInfoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                switch (e.getError_code()) {
                    case ErrorCode.ALREADY_FOLLOWED:

                        break;
                }

            }
        }

        @Override
        protected void onPostExecute(UserBean o) {
            super.onPostExecute(o);
            Toast.makeText(UserInfoActivity.this, getString(R.string.follow_successfully), Toast.LENGTH_SHORT).show();
            bean = o;
            bean.setFollowing(true);
            invalidateOptionsMenu();
            manageGroup();
        }
    }

    private class RemoveFanTask extends MyAsyncTask<Void, UserBean, UserBean> {

        WeiboException e;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected UserBean doInBackground(Void... params) {

            FanDao dao = new FanDao(getToken(), bean.getId());

            try {
                return dao.removeFan();
            } catch (WeiboException e) {
                AppLoggerUtils.e(e.getError());
                this.e = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onCancelled(UserBean userBean) {
            super.onCancelled(userBean);
            if (e != null) {
                Toast.makeText(UserInfoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }

        }

        @Override
        protected void onPostExecute(UserBean o) {
            super.onPostExecute(o);
            Toast.makeText(UserInfoActivity.this, getString(R.string.remove_fan_successfully), Toast.LENGTH_SHORT).show();
            bean = o;
            getInfoFragment().forceReloadData(o);
        }
    }

    class UpdateRemarkTask extends MyAsyncTask<Void, UserBean, UserBean> {

        WeiboException e;

        String remark;

        UpdateRemarkTask(String remark) {
            this.remark = remark;
        }

        @Override
        protected UserBean doInBackground(Void... params) {
            try {
                return new RemarkDao(getToken(), bean.getId(), remark).updateRemark();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onCancelled(UserBean userBean) {
            super.onCancelled(userBean);
            if (this.e != null) {
                Toast.makeText(UserInfoActivity.this, this.e.getError(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(UserBean userBean) {
            super.onPostExecute(userBean);
            bean = userBean;
            if (getInfoFragment() != null) {
                getInfoFragment().forceReloadData(userBean);
            }

        }
    }

    private static class RefreshLoader extends AbstractAsyncNetRequestTaskLoader<UserBean> {

        private UserBean bean;

        public RefreshLoader(Context context, UserBean userBean) {
            super(context);
            this.bean = userBean;
        }

        @Override
        protected UserBean loadData() throws WeiboException {
            ShowUserDao dao = new ShowUserDao(BeeboApplication.getInstance().getAccessTokenHack());
            boolean haveId = !TextUtils.isEmpty(bean.getId());
            boolean haveName = !TextUtils.isEmpty(bean.getScreen_name());
            boolean haveDomain = !TextUtils.isEmpty(bean.getDomain());

            if (haveId) {
                dao.setUid(bean.getId());
            } else if (haveName) {
                dao.setScreen_name(bean.getScreen_name());
            } else if (haveDomain) {
                dao.setDomain(bean.getDomain());
            } else {
                return null;
            }

            return dao.getUserInfo();
        }
    }

    private LoaderManager.LoaderCallbacks<AsyncTaskLoaderResult<UserBean>> refreshCallback = new LoaderManager.LoaderCallbacks<AsyncTaskLoaderResult<UserBean>>() {
        @Override
        public Loader<AsyncTaskLoaderResult<UserBean>> onCreateLoader(int id, Bundle args) {
            return new RefreshLoader(UserInfoActivity.this, bean);
        }

        @Override
        public void onLoadFinished(Loader<AsyncTaskLoaderResult<UserBean>> loader, AsyncTaskLoaderResult<UserBean> result) {
            UserBean data = result != null ? result.data : null;
            final WeiboException exception = result != null ? result.exception : null;

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    CommonProgressDialogFragment dialog = (CommonProgressDialogFragment) getSupportFragmentManager()
                            .findFragmentByTag(
                                    CommonProgressDialogFragment.class.getName());
                    if (dialog != null) {
                        dialog.dismissAllowingStateLoss();
                    }

                    if (exception != null) {
                        CommonErrorDialogFragment userInfoActivityErrorDialog = CommonErrorDialogFragment
                                .newInstance(exception.getError());
                        getSupportFragmentManager().beginTransaction()
                                .add(userInfoActivityErrorDialog, CommonErrorDialogFragment.class.getName()).commit();
                    }
                }
            });

            if (data != null) {
                bean = data;
                buildContent();
            }
            getLoaderManager().destroyLoader(loader.getId());
        }

        @Override
        public void onLoaderReset(Loader<AsyncTaskLoaderResult<UserBean>> loader) {

        }
    };

}
