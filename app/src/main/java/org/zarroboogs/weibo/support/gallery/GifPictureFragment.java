
package org.zarroboogs.weibo.support.gallery;

import org.zarroboogs.utils.ImageUtility;
import org.zarroboogs.weibo.MyAnimationListener;
import org.zarroboogs.weibo.R;
import org.zarroboogs.weibo.setting.SettingUtils;
import org.zarroboogs.weibo.support.lib.AnimationRect;
import org.zarroboogs.weibo.support.utils.AnimationUtility;
import org.zarroboogs.weibo.support.utils.Utility;
import org.zarroboogs.weibo.support.utils.ViewUtility;
//import org.zarroboogs.weibo.widget.ClipImageView;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;

import java.io.File;
import java.io.IOException;

//import pl.droidsonroids.gif.GifDrawable;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class GifPictureFragment extends Fragment {

    private static final int NAVIGATION_BAR_HEIGHT_DP_UNIT = 48;

    private static final int ANIMATION_DURATION = 300;

    private static final int IMAGEVIEW_SOFT_LAYER_MAX_WIDTH = 2000;

    private static final int IMAGEVIEW_SOFT_LAYER_MAX_HEIGHT = 3000;

    private PhotoView gifImageView;

    public static GifPictureFragment newInstance(String path, AnimationRect rect, boolean animationIn) {
        GifPictureFragment fragment = new GifPictureFragment();
        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        bundle.putParcelable("rect", rect);
        bundle.putBoolean("animationIn", animationIn);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_gif_layout, container, false);

//        FrameLayout view = (FrameLayout)inflater.inflate(R.layout.gallery_gif_layout, container, false);
//
//        gifImageView = (PhotoView) view.findViewById(R.id.gif);
//
//        if (SettingUtils.allowClickToCloseGallery()) {
//            gifImageView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
//                @Override
//                public void onViewTap(View view, float x, float y) {
//                    getActivity().onBackPressed();
//                }
//            });
//        }
//
//        LongClickListener longClickListener = ((BigPicContainerFragment) getParentFragment()).getLongClickListener();
//        gifImageView.setOnLongClickListener(longClickListener);

        String path = getArguments().getString("path");
        boolean animateIn = getArguments().getBoolean("animationIn");
        final AnimationRect rect = getArguments().getParcelable("rect");

        File gifFile = new File(path);

        final DraweeView photoView = ViewUtility.findViewById(view,R.id.cover);////(ClipImageView) view.findViewById(R.id.cover);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(Uri.fromFile(gifFile)).setAutoPlayAnimations(true).build();
        photoView.setController(controller);


        return view;
    }

}
