package org.zarroboogs.weibo.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.zarroboogs.utils.Constants;
import org.zarroboogs.weibo.GlobalContext;
import org.zarroboogs.weibo.R;
import org.zarroboogs.weibo.activity.RepostWeiboWithAppSrcActivity;
import org.zarroboogs.weibo.activity.WriteCommentActivity;
import org.zarroboogs.weibo.asynctask.MyAsyncTask;
import org.zarroboogs.weibo.bean.MessageBean;
import org.zarroboogs.weibo.support.utils.Utility;
import org.zarroboogs.weibo.support.utils.ViewUtility;
import org.zarroboogs.weibo.ui.task.FavAsyncTask;
import org.zarroboogs.weibo.widget.TimeLineAvatarImageView;
import org.zarroboogs.weibo.widget.TimeLineImageView;
import org.zarroboogs.weibo.widget.TimeTextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HotWeiboAdapter extends BaseAdapter {

	private LayoutInflater mInflater;

	private ImageLoader mImageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	private List<MessageBean> list = new ArrayList<MessageBean>();

	private Context mContext;
	private FavAsyncTask favTask = null;
	
	public HotWeiboAdapter(Context context) {
		super();
		// TODO Auto-generated constructor stub
		this.mContext = context;
		
		mInflater = LayoutInflater.from(context);
		options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true)
				.bitmapConfig(Bitmap.Config.RGB_565).build();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.hotweibo_item_layout, null);
			holder = buildHolder(convertView);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		// mImageLoader.displayImage("file://" +
		// SendImgData.getInstance().getSendImgs().get(position),
		// holder.mImageView , options);
		MessageBean blog = list.get(position);
		
		holder.username.setText(blog.getUser().getScreen_name());

		holder.weiboTextContent.setText(blog.getText());
		
		holder.repost_count.setText(blog.getReposts_count() + "");
		holder.comment_count.setText(blog.getComments_count()+ "");
		holder.count_layout.setVisibility(View.VISIBLE);
		
		Bitmap m = mImageLoader.loadImageSync(blog.getBmiddle_pic(), options);
		
		Log.d("HotWeiboAdapter_load: ", "middle url: " + blog.getBmiddle_pic() + "   bitmap is null? :" + (m == null));
		
		final TimeLineImageView content_pic = holder.content_pic;
		
		loadContentPic(holder, blog, m, content_pic);
		mImageLoader.displayImage(blog.getUser().getAvatar_large(), holder.avatar);
		
		final ViewHolder tmpHolder = holder; 
		final MessageBean msg = list.get(position);
        holder.popupMenuIb.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				PopupMenu popupMenu = new PopupMenu(mContext, tmpHolder.popupMenuIb);
				popupMenu.inflate(R.menu.time_line_popmenu);
				popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					
					@Override
					public boolean onMenuItemClick(MenuItem arg0) {
						// TODO Auto-generated method stub
						int id = arg0.getItemId();
						switch (id) {
						case R.id.menu_repost:{
							Intent intent = new Intent(mContext, RepostWeiboWithAppSrcActivity.class);
			                intent.putExtra(Constants.TOKEN, GlobalContext.getInstance().getAccessToken());
			                intent.putExtra("msg", msg);
			                mContext.startActivity(intent);
							break;
						}
						case R.id.menu_comment:{
							Intent intent = new Intent(mContext, WriteCommentActivity.class);
			                intent.putExtra(Constants.TOKEN, GlobalContext.getInstance().getAccessToken());
			                intent.putExtra("msg", msg);
			                mContext.startActivity(intent);
							break;
						}
						
						case R.id.menu_fav:{
							if (Utility.isTaskStopped(favTask)) {
							    favTask = new FavAsyncTask(GlobalContext.getInstance().getAccessToken(), msg.getId());
							    favTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
							}
							break;
						}
						
						case R.id.menu_copy:{
							ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
							cm.setPrimaryClip(ClipData.newPlainText("sinaweibo", msg.getText()));
							Toast.makeText(mContext, mContext.getResources().getString(R.string.copy_successfully), Toast.LENGTH_SHORT).show();
							break;
						}

						default:
							break;
						}
						return false;
					}
				});
				popupMenu.show();
			}
		});
		
		return convertView;
	}

	private void loadContentPic(ViewHolder holder, MessageBean blog, Bitmap m,
			final TimeLineImageView content_pic) {
		mImageLoader.loadImage(blog.getBmiddle_pic(),options, new SimpleImageLoadingListener(){

			@Override
			public void onLoadingComplete(String imageUri, View view,
					Bitmap loadedImage) {
				// TODO Auto-generated method stub
				super.onLoadingComplete(imageUri, view, loadedImage);
				
				content_pic.setImageBitmap(loadedImage);
			}
			
		});
		holder.content_pic.setImageBitmap(m);
		holder.content_pic.setVisibility(View.VISIBLE);
	}

    public static class ViewHolder {

        TextView username;

        TextView weiboTextContent;

        TextView repost_content;

        TimeTextView time;

        TimeLineAvatarImageView avatar;

        TimeLineImageView content_pic;

        GridLayout content_pic_multi;

        TimeLineImageView repost_content_pic;

        GridLayout repost_content_pic_multi;

        ViewGroup listview_root;

        View repost_layout;

        View repost_flag;

        LinearLayout count_layout;

        TextView repost_count;

        TextView comment_count;

        TextView source;

        ImageView timeline_gps;

        ImageView timeline_pic;

        ImageView replyIV;

        ImageButton commentBtn;
        
        ImageButton popupMenuIb;
    }

    private ViewHolder buildHolder(View convertView) {
        final ViewHolder holder = new ViewHolder();
        holder.username = ViewUtility.findViewById(convertView, R.id.username);
        TextPaint tp = holder.username.getPaint();
        if (tp != null) {
            tp.setFakeBoldText(true);
        }
        holder.weiboTextContent = ViewUtility.findViewById(convertView, R.id.weibo_text_content);
        holder.repost_content = ViewUtility.findViewById(convertView, R.id.repost_content);
        holder.time = ViewUtility.findViewById(convertView, R.id.time);
        holder.avatar = (TimeLineAvatarImageView) convertView.findViewById(R.id.avatar);

        holder.repost_content_pic = ViewUtility.findViewById(convertView, R.id.repost_content_pic);
        holder.repost_content_pic_multi = ViewUtility.findViewById(convertView, R.id.repost_content__pic_multi);

        holder.content_pic = ViewUtility.findViewById(convertView, R.id.content_pic);
        holder.content_pic_multi = ViewUtility.findViewById(convertView, R.id.content_pic_multi);

        holder.listview_root = ViewUtility.findViewById(convertView, R.id.listview_root);
        holder.repost_layout = ViewUtility.findViewById(convertView, R.id.repost_layout);
        holder.repost_flag = ViewUtility.findViewById(convertView, R.id.repost_flag);
        holder.count_layout = ViewUtility.findViewById(convertView, R.id.count_layout);
        holder.repost_count = ViewUtility.findViewById(convertView, R.id.repost_count);
        holder.comment_count = ViewUtility.findViewById(convertView, R.id.comment_count);
        holder.timeline_gps = ViewUtility.findViewById(convertView, R.id.timeline_gps_iv);
        holder.timeline_pic = ViewUtility.findViewById(convertView, R.id.timeline_pic_iv);
        holder.replyIV = ViewUtility.findViewById(convertView, R.id.replyIV);
        holder.source = ViewUtility.findViewById(convertView, R.id.source);
        holder.commentBtn = ViewUtility.findViewById(convertView, R.id.commentButton);
        
        holder.popupMenuIb = ViewUtility.findViewById(convertView, R.id.popupMenuIb);
        
        return holder;
    }

//	public void addNewData(List<HotMblogBean> list) {
//		this.list.addAll(list);
//		notifyDataSetChanged();
//	}
	
    public void addNewData(List<MessageBean> newValue) {

        if (newValue == null || newValue.size() == 0) {
            return;
        }

        this.list.addAll(0, newValue);
        
        // remove duplicate null flag, [x,y,null,null,z....]
 
        ListIterator<MessageBean> listIterator = this.list.listIterator();

        boolean isLastItemNull = false;
        while (listIterator.hasNext()) {
        	MessageBean msg = listIterator.next();
            if (msg == null) {
                if (isLastItemNull) {
                    listIterator.remove();
                }
                isLastItemNull = true;
            } else {
                isLastItemNull = false;
            }
        }
    }
}
