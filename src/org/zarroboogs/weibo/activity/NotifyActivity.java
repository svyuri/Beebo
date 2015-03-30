package org.zarroboogs.weibo.activity;

import org.zarroboogs.weibo.R;
import org.zarroboogs.weibo.fragment.AtMeTimeLineFragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class NotifyActivity extends TranslucentStatusBarActivity {

    private Toolbar mToolbar;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	 setContentView(R.layout.hotweibo_activity_layout);
         mToolbar = (Toolbar) findViewById(R.id.hotWeiboToolbar);
         
         buildContent();
         mToolbar.setTitle("与我相关");
         disPlayHomeAsUp(mToolbar);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// TODO Auto-generated method stub
    	getMenuInflater().inflate(R.menu.write_dm_menu, menu);
    	return super.onCreateOptionsMenu(menu);
    }
    
    public static final int REQUEST_CODE = 0x1010;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
		if (itemId == R.id.menu_write_dm) {
			Intent intent = new Intent(NotifyActivity.this, DMSelectUserActivity.class);
			startActivityForResult(intent, REQUEST_CODE);
		}
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (REQUEST_CODE == requestCode) {
            Intent intent = new Intent(NotifyActivity.this, DMActivity.class);
            intent.putExtra("user", data.getParcelableExtra("user"));
            startActivity(intent);
		}

    }
    
    private void buildContent() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (getSupportFragmentManager().findFragmentByTag(AtMeTimeLineFragment.class.getName()) == null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.hotWeiboContent, new AtMeTimeLineFragment(),AtMeTimeLineFragment.class.getName())
                            .commitAllowingStateLoss();
                    getSupportFragmentManager().executePendingTransactions();
                }
            }
        });
    }
}
