
package org.zarroboogs.weibo.support.gallery;

import org.zarroboogs.devutils.DevLog;
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
import android.widget.RelativeLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;
import com.facebook.drawee.view.SimpleDraweeView;

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


        gifImageView = (PhotoView) view.findViewById(R.id.gif);

        if (SettingUtils.allowClickToCloseGallery()) {
            gifImageView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float x, float y) {
                    getActivity().onBackPressed();
                }
            });
        }

        LongClickListener longClickListener = ((BigPicContainerFragment) getParentFragment()).getLongClickListener();
        gifImageView.setOnLongClickListener(longClickListener);

        String path = getArguments().getString("path");
        boolean animateIn = getArguments().getBoolean("animationIn");
        final AnimationRect rect = getArguments().getParcelable("rect");

        File gifFile = new File(path);

        final Bitmap bitmap = ImageUtility.decodeBitmapFromSDCard(path, IMAGEVIEW_SOFT_LAYER_MAX_WIDTH,
                IMAGEVIEW_SOFT_LAYER_MAX_HEIGHT);

        gifImageView.setImageBitmap(bitmap);
        DevLog.printLog("GifPictureFragment ","" + bitmap.getWidth() + " * " + bitmap.getHeight());

        final Runnable endAction = new Runnable() {
            @Override
            public void run() {
                Bundle bundle = getArguments();
                bundle.putBoolean("animationIn", false);
            }
        };

        SimpleDraweeView photoView = ViewUtility.findViewById(view,R.id.cover);////(ClipImageView) view.findViewById(R.id.cover);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(Uri.fromFile(gifFile)).setAutoPlayAnimations(true).build();
        photoView.setController(controller);
        photoView.setAspectRatio(bitmap.getWidth()/ bitmap.getHeight());
        photoView.setVisibility(View.INVISIBLE);

        gifImageView.setImageDrawable(photoView.getDrawable());
        int w = Utility.getScreenWidth();
        int h = bitmap.getHeight() * w / bitmap.getWidth();

        DevLog.printLog("GifPictureFragment ","" + bitmap.getWidth() + " * " + bitmap.getHeight() + "  " + w + "  " + h);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(w,h);
        gifImageView.setLayoutParams(layoutParams);

        // w * h1 = h * w1

        photoView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {

                if (rect == null) {
                    gifImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                    endAction.run();
                    return true;
                }

                final Rect startBounds = new Rect(rect.scaledBitmapRect);
                final Rect finalBounds = AnimationUtility.getBitmapRectFromImageView(gifImageView);

                if (finalBounds == null) {
                    gifImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                    endAction.run();
                    return true;
                }

                float startScale = (float) finalBounds.width() / startBounds.width();

                if (startScale * startBounds.height() > finalBounds.height()) {
                    startScale = (float) finalBounds.height() / startBounds.height();
                }

                int deltaTop = startBounds.top - finalBounds.top;
                int deltaLeft = startBounds.left - finalBounds.left;

                gifImageView.setPivotY((gifImageView.getHeight() - finalBounds.height()) / 2);
                gifImageView.setPivotX((gifImageView.getWidth() - finalBounds.width()) / 2);

                gifImageView.setScaleX(1 / startScale);
                gifImageView.setScaleY(1 / startScale);

                gifImageView.setTranslationX(deltaLeft);
                gifImageView.setTranslationY(deltaTop);

                gifImageView.animate().translationY(0).translationX(0).scaleY(1).scaleX(1).setDuration(ANIMATION_DURATION)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .setListener(new MyAnimationListener(endAction));

                AnimatorSet animationSet = new AnimatorSet();
                animationSet.setDuration(ANIMATION_DURATION);
                animationSet.setInterpolator(new AccelerateDecelerateInterpolator());

                animationSet.playTogether(ObjectAnimator.ofFloat(gifImageView, "clipBottom",
                        AnimationRect.getClipBottom(rect, finalBounds), 0));
                animationSet.playTogether(ObjectAnimator.ofFloat(gifImageView, "clipRight",
                        AnimationRect.getClipRight(rect, finalBounds), 0));
                animationSet.playTogether(ObjectAnimator.ofFloat(gifImageView, "clipTop",
                        AnimationRect.getClipTop(rect, finalBounds), 0));
                animationSet.playTogether(ObjectAnimator.ofFloat(gifImageView, "clipLeft",
                        AnimationRect.getClipLeft(rect, finalBounds), 0));

                animationSet.start();

                gifImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
        return view;
    }

    public void animationExit(ObjectAnimator backgroundAnimator) {

        if (Math.abs(gifImageView.getScale() - 1.0f) > 0.1f) {
            gifImageView.setScale(1, true);
            return;
        }

        getActivity().overridePendingTransition(0, 0);
        animateClose(backgroundAnimator);

    }

    private void animateClose(ObjectAnimator backgroundAnimator) {

        AnimationRect rect = getArguments().getParcelable("rect");

        if (rect == null) {
            gifImageView.animate().alpha(0);
            backgroundAnimator.start();
            return;
        }

        final Rect startBounds = rect.scaledBitmapRect;
        final Rect finalBounds = AnimationUtility.getBitmapRectFromImageView(gifImageView);

        if (finalBounds == null) {
            gifImageView.animate().alpha(0);
            backgroundAnimator.start();
            return;
        }

        if (Utility.isDevicePort() != rect.isScreenPortrait) {
            gifImageView.animate().alpha(0);
            backgroundAnimator.start();
            return;
        }

        float startScale;
        if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds.width() / startBounds.height()) {
            startScale = (float) startBounds.height() / finalBounds.height();

        } else {
            startScale = (float) startBounds.width() / finalBounds.width();
        }

        final float startScaleFinal = startScale;

        int deltaTop = startBounds.top - finalBounds.top;
        int deltaLeft = startBounds.left - finalBounds.left;

        gifImageView.setPivotY((gifImageView.getHeight() - finalBounds.height()) / 2);
        gifImageView.setPivotX((gifImageView.getWidth() - finalBounds.width()) / 2);

        gifImageView.animate().translationX(deltaLeft).translationY(deltaTop).scaleY(startScaleFinal).scaleX(startScaleFinal)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator()).setListener(new MyAnimationListener(new Runnable() {
            @Override
            public void run() {

                gifImageView.animate().alpha(0.0f).setDuration(200);

            }
        }));

        AnimatorSet animationSet = new AnimatorSet();
        animationSet.setDuration(ANIMATION_DURATION);
        animationSet.setInterpolator(new AccelerateDecelerateInterpolator());

        animationSet.playTogether(backgroundAnimator);

        animationSet.playTogether(ObjectAnimator.ofFloat(gifImageView, "clipBottom", 0,
                AnimationRect.getClipBottom(rect, finalBounds)));
        animationSet.playTogether(ObjectAnimator.ofFloat(gifImageView, "clipRight", 0,
                AnimationRect.getClipRight(rect, finalBounds)));
        animationSet.playTogether(ObjectAnimator.ofFloat(gifImageView, "clipTop", 0,
                AnimationRect.getClipTop(rect, finalBounds)));
        animationSet.playTogether(ObjectAnimator.ofFloat(gifImageView, "clipLeft", 0,
                AnimationRect.getClipLeft(rect, finalBounds)));

        animationSet.start();

    }
}
