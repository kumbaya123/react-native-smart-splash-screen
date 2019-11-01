package com.reactnativecomponent.splashscreen;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class RCTSplashScreenModule extends ReactContextBaseJavaModule {
    public static String ImgPath_start = null;
    public static String ImgPath_icon = null;

    public static int CODE = 11000;
    public static String Url_start = "";
    public static String Url_icon = "";

    public RCTSplashScreenModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "SplashScreen";
    }

    public static void requestPermissionsCallback() {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    downLoadImage(Url_start, Url_icon);
                }
            }).start();
        } catch (Exception e) {
        }

    }

    /**
     * 清除图片
     */
    @ReactMethod
    public void cleanScreenImage() {
        File file = new File(RCTSplashScreenModule.ImgPath_start);
        if (file.exists()) {
            file.delete();
        }
        File file_icon = new File(RCTSplashScreenModule.ImgPath_icon);
        if (file_icon.exists()) {
            file_icon.delete();
        }
    }

    public static void getImgPath(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            RCTSplashScreenModule.ImgPath_start = context.getExternalFilesDir(null).getAbsolutePath() + "/mg_open_image.jpg";
            RCTSplashScreenModule.ImgPath_icon = context.getExternalFilesDir(null).getAbsolutePath() + "/mg_open_icon.jpg";

        } else {
            RCTSplashScreenModule.ImgPath_start = context.getFilesDir().getAbsolutePath() + "/mg_open_image.jpg";
            RCTSplashScreenModule.ImgPath_icon = context.getFilesDir().getAbsolutePath() + "/mg_open_icon.jpg";

        }
    }

    /**
     * 图片下载方法
     */
    @ReactMethod
    public void loadLaunchScreenImage(String start_url, String icon_url) {
        Url_start = start_url;
        Url_icon = icon_url;

        try {
            /**权限申请 */
            String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            int permission_result = PermissionChecker.checkPermission(getCurrentActivity(), perms[0], android.os.Process.myPid(), android.os.Process.myUid(), getCurrentActivity().getPackageName());
            if (permission_result != PermissionChecker.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) getCurrentActivity(), perms, RCTSplashScreenModule.CODE);
                return;
            }

            downLoadImage(Url_start, Url_icon);
        } catch (Exception e) {

        }
    }

    public static void downLoadImage(String start_url, String icon_url) {
        Bitmap mBitmap_start = BitmapFactory.decodeStream(getImageStream(start_url));
        saveBitmapToSDCard(mBitmap_start, 1);

        Bitmap mBitmap_icon = BitmapFactory.decodeStream(getImageStream(icon_url));
        saveBitmapToSDCard(mBitmap_icon, 2);
    }

    /**
     * 保存到本地
     *
     * @param bitmap
     */
    public static void saveBitmapToSDCard(Bitmap bitmap, int type) {
        FileOutputStream fos = null;
        try {
            String ImgPath = "";
            if (type == 1) {
                ImgPath = ImgPath_start;
            }
            if (type == 2) {
                ImgPath = ImgPath_icon;
            }
            fos = new FileOutputStream(ImgPath);//picPath为保存SD卡路径
            if (fos != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static InputStream getImageStream(String path) {
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return conn.getInputStream();
            }
            return null;

        } catch (Exception e) {
            return null;
        }
    }


    @ReactMethod
    public void close(ReadableMap options) {

        int animationType = RCTSplashScreen.UIAnimationNone;
        int duration = 0;
        int delay = 0;

        if (options != null) {
            if (options.hasKey("animationType")) {
                animationType = options.getInt("animationType");
            }
            if (options.hasKey("duration")) {
                duration = options.getInt("duration");
            }
            if (options.hasKey("delay")) {
                delay = options.getInt("delay");
            }
        }

        if (animationType == RCTSplashScreen.UIAnimationNone) {
            delay = 0;
        }

        final int final_animationType = animationType;
        final int final_duration = duration;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                RCTSplashScreen.removeSplashScreen(getCurrentActivity(), final_animationType, final_duration);
            }
        }, delay);
    }


    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
            {
                put("animationType", getAnimationTypes());
            }

            private Map<String, Object> getAnimationTypes() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("none", RCTSplashScreen.UIAnimationNone);
                        put("fade", RCTSplashScreen.UIAnimationFade);
                        put("scale", RCTSplashScreen.UIAnimationScale);
                    }
                });
            }
        });
    }

}
