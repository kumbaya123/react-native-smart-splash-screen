package com.reactnativecomponent.splashscreen;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.lang.ref.WeakReference;


public class RCTSplashScreen {

    public static final int UIAnimationNone = 0;
    public static final int UIAnimationFade = 1;
    public static final int UIAnimationScale = 2;

    private static Dialog dialog;
    private static ImageView imageView;

    private static WeakReference<Activity> wr_activity;

    protected static Activity getActivity() {
        return wr_activity.get();
    }

    public static void openSplashScreen(Activity activity) {
        openSplashScreen(activity, false);
    }

    public static void openSplashScreen(Activity activity, boolean isFullScreen) {
        openSplashScreen(activity, isFullScreen, ImageView.ScaleType.CENTER_CROP);
    }

    public static boolean checkFileExists(String start_url, String icon_url) {
        boolean file_exists = false;
        boolean start = new File(RCTSplashScreenModule.ImgPath_start).exists();
        boolean file_icon = new File(RCTSplashScreenModule.ImgPath_icon).exists();
        if (start) {
            file_exists = true;
        }
        return file_exists;
    }

    public static void openSplashScreen(final Activity activity, final boolean isFullScreen, final ImageView.ScaleType scaleType) {
        if (activity == null) return;
        /** add by david at 2019-10-17 start  */
        // 查看本地是否存在图片
        RCTSplashScreenModule.getImgPath(activity);
        final boolean file_exists = checkFileExists(RCTSplashScreenModule.ImgPath_start, RCTSplashScreenModule.ImgPath_icon);
        /** add by david at 2019-10-17 end  */

        wr_activity = new WeakReference<>(activity);
        final int drawableId = getImageId();
        if ((dialog != null && dialog.isShowing()) || (drawableId == 0)) {
            return;
        }
        activity.runOnUiThread(new Runnable() {
            public void run() {

                if (!getActivity().isFinishing()) {
                    Context context = getActivity();
                    if (file_exists) {
                        View view = View.inflate(context, R.layout.start_view, null);
                        ImageView start_image = view.findViewById(R.id.start_image);
                        ImageView icon_image = view.findViewById(R.id.icon_image);
                        Bitmap bitmap_start = BitmapFactory.decodeFile(RCTSplashScreenModule.ImgPath_start);
                        start_image.setImageBitmap(bitmap_start);

                        Bitmap bitmap_icon = BitmapFactory.decodeFile(RCTSplashScreenModule.ImgPath_icon);
                        icon_image.setImageBitmap(bitmap_icon);

                        dialog = new Dialog(context, isFullScreen ? android.R.style.Theme_Translucent_NoTitleBar_Fullscreen : android.R.style.Theme_Translucent_NoTitleBar);
                        dialog.setContentView(view);
                        dialog.setCancelable(false);
                        dialog.show();
                        return;
                    }
                    imageView = new ImageView(context);

//                    imageView.setImageResource(drawableId);

                    LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                    imageView.setLayoutParams(layoutParams);

                    /** add by david at 2019-10-18 start */
                    imageView.setImageResource(drawableId);
                    /** add by david at 2019-10-18 end */


                    imageView.setScaleType(scaleType);

                    dialog = new Dialog(context, isFullScreen ? android.R.style.Theme_Translucent_NoTitleBar_Fullscreen : android.R.style.Theme_Translucent_NoTitleBar);

//                    if ((getActivity().getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN)
//                            == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
//                        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                    }
                    dialog.setContentView(imageView);
                    dialog.setCancelable(false);
                    dialog.show();
                }

            }
        });
    }

    public static void removeSplashScreen(Activity activity, final int animationType, final int duration) {
        if (activity == null) {
            activity = getActivity();
            if (activity == null) return;
        }
        activity.runOnUiThread(new Runnable() {
            public void run() {
                if (dialog != null && dialog.isShowing()) {
                    AnimationSet animationSet = new AnimationSet(true);

                    if (animationType == UIAnimationScale) {
                        AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
                        fadeOut.setDuration(duration);
                        animationSet.addAnimation(fadeOut);

                        ScaleAnimation scale = new ScaleAnimation(1, 1.5f, 1, 1.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.65f);
                        scale.setDuration(duration);
                        animationSet.addAnimation(scale);
                    } else if (animationType == UIAnimationFade) {
                        AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
                        fadeOut.setDuration(duration);
                        animationSet.addAnimation(fadeOut);
                    } else {
                        AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
                        fadeOut.setDuration(0);
                        animationSet.addAnimation(fadeOut);
                    }

                    final View view = ((ViewGroup) dialog.getWindow().getDecorView()).getChildAt(0);
                    view.startAnimation(animationSet);

                    animationSet.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            view.post(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                    dialog = null;
                                    imageView = null;
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private static int getImageId() {
        int drawableId = getActivity().getResources().getIdentifier("splash", "drawable", getActivity().getClass().getPackage().getName());
        if (drawableId == 0) {
            drawableId = getActivity().getResources().getIdentifier("splash", "drawable", getActivity().getPackageName());
        }
        return drawableId;
    }


}
