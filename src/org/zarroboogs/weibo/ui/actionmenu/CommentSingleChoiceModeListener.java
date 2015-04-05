
package org.zarroboogs.weibo.ui.actionmenu;

import org.zarroboogs.utils.Constants;
import org.zarroboogs.weibo.BeeboApplication;
import org.zarroboogs.weibo.R;
import org.zarroboogs.weibo.activity.BrowserCommentActivity;
import org.zarroboogs.weibo.activity.WriteReplyToCommentActivity;
import org.zarroboogs.weibo.bean.CommentBean;
import org.zarroboogs.weibo.dialogfragment.RemoveDialog;
import org.zarroboogs.weibo.fragment.BrowserWeiboMsgFragment;
import org.zarroboogs.weibo.fragment.base.AbsBaseTimeLineFragment;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import java.util.List;

/**
 * User: qii Date: 12-9-10
 */
public class CommentSingleChoiceModeListener implements ActionMode.Callback {

    private ListView listView;

    private BaseAdapter adapter;

    private Fragment fragment;

    private ActionMode mode;

    private CommentBean bean;

    private ShareActionProvider mShareActionProvider;

    public void finish() {
        if (mode != null) {
            mode.finish();
        }
    }

    public CommentSingleChoiceModeListener(ListView listView, BaseAdapter adapter, Fragment fragment, CommentBean bean) {
        this.listView = listView;
        this.fragment = fragment;
        this.adapter = adapter;
        this.bean = bean;
    }

    private Activity getActivity() {
        return fragment.getActivity();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (this.mode == null) {
            this.mode = mode;
        }

        return true;

    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        buildMenu(mode, menu);
        return true;

    }

    protected void buildMenu(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        menu.clear();

        boolean isMyComment = bean.getUser().getId().equals(BeeboApplication.getInstance().getCurrentAccountId());
        boolean isCommentUnderMyStatus = bean.getStatus().getUser().getId()
                .equals(BeeboApplication.getInstance().getCurrentAccountId());

        if (isMyComment || isCommentUnderMyStatus) {
            inflater.inflate(R.menu.contextual_menu_fragment_comment_listview_myself, menu);
        } else {
            inflater.inflate(R.menu.contextual_menu_fragment_comment_listview, menu);
        }

        mode.setTitle(bean.getUser().getScreen_name());

        MenuItem item = menu.findItem(R.id.menu_share);
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, bean.getText());
        PackageManager packageManager = getActivity().getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(sharingIntent, 0);
        boolean isIntentSafe = activities.size() > 0;
        if (isIntentSafe && mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(sharingIntent);
        }
        mShareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
            @Override
            public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {
                finish();
                return false;
            }
        });
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Intent intent;
        int itemId = item.getItemId();
		if (itemId == R.id.menu_view) {
			intent = new Intent(getActivity(), BrowserCommentActivity.class);
			intent.putExtra("comment", bean);
			intent.putExtra(Constants.TOKEN, BeeboApplication.getInstance().getAccessToken());
			getActivity().startActivity(intent);
			listView.clearChoices();
			mode.finish();
		} else if (itemId == R.id.menu_comment) {
			intent = new Intent(getActivity(), WriteReplyToCommentActivity.class);
			intent.putExtra(Constants.TOKEN, BeeboApplication.getInstance().getAccessToken());
			intent.putExtra("msg", bean);
			getActivity().startActivity(intent);
			listView.clearChoices();
			mode.finish();
		} else if (itemId == R.id.menu_share) {
			Intent sharingIntent = new Intent(Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			sharingIntent.putExtra(Intent.EXTRA_TEXT, bean.getText());
			PackageManager packageManager = getActivity().getPackageManager();
			List<ResolveInfo> activities = packageManager.queryIntentActivities(sharingIntent, 0);
			boolean isIntentSafe = activities.size() > 0;
			if (isIntentSafe && mShareActionProvider != null) {
			    mShareActionProvider.setShareIntent(sharingIntent);
			}
			mShareActionProvider
			        .setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
			            @Override
			            public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {
			                finish();
			                return false;
			            }
			        });
		} else if (itemId == R.id.menu_copy) {
			ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
			cm.setPrimaryClip(ClipData.newPlainText("sinaweibo", bean.getText()));
			Toast.makeText(getActivity(), getActivity().getString(R.string.copy_successfully), Toast.LENGTH_SHORT)
			        .show();
			listView.clearChoices();
			mode.finish();
		} else if (itemId == R.id.menu_remove) {
			int position = listView.getCheckedItemPosition() - listView.getHeaderViewsCount();
			RemoveDialog dialog = new RemoveDialog(position);
			dialog.setTargetFragment(fragment, 0);
			dialog.show(fragment.getFragmentManager(), "");
		}

        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        this.mode = null;
        listView.clearChoices();
        adapter.notifyDataSetChanged();
        if (fragment instanceof AbsBaseTimeLineFragment) {
            ((AbsBaseTimeLineFragment) fragment).setActionMode(null);
        }

        if (fragment instanceof BrowserWeiboMsgFragment) {
            ((BrowserWeiboMsgFragment) fragment).setActionMode(null);
        }

    }
}
