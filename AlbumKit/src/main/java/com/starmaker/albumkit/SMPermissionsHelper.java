package com.starmaker.albumkit;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by zengkebin on 2016/12/22.
 */
public class SMPermissionsHelper {

    /**
     * 权限检测
     * @param activity
     * @return
     */
    public static boolean checkPermission(Activity activity) {
        boolean check = true;
        if (Build.VERSION.SDK_INT >= 23) { // Need to ask for permissions.
            String[] permissions = {
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    Manifest.permission.INTERNET,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    "com.samsung.android.sdk.professionalaudio.permission.START_MONITOR_SERVICE",
                    "com.samsung.android.providers.context.permission.WRITE_USE_APP_FEATURE_SURVEY"
            };
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(activity, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, permissions, 0);
                    check = false;
                    break;
                }
            }
        }
        return check;
    }
}


