package com.starmaker.albumkit;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.starmaker.albumkit.R;

import java.util.Timer;
import java.util.TimerTask;

public class AlbumKitActivity extends Activity {
	private AlbumKitView mGLSurfaceView;
    private AlbumKitRender mGLRender;

    private static final int REQUEST_PICK_IMAGE = 1;


    private AlbumKit mAlbumKit;
    private void handleImage(final Uri selectedImage) {
         try {
            mAlbumKit.addPhotoInAlbum(selectedImage);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    handleImage(data.getData());
                } else {
                    finish();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }


        @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //requestWindowFeature可以设置的值有：
        // 1.DEFAULT_FEATURES：系统默认状态，一般不需要指定
        // 2.FEATURE_CONTEXT_MENU：启用ContextMenu，默认该项已启用，一般无需指定
        // 3.FEATURE_CUSTOM_TITLE：自定义标题。当需要自定义标题时必须指定。如：标题是一个按钮时
        // 4.FEATURE_INDETERMINATE_PROGRESS：不确定的进度
        // 5.FEATURE_LEFT_ICON：标题栏左侧的图标
        // 6.FEATURE_NO_TITLE：无标题
        // 7.FEATURE_OPTIONS_PANEL：启用“选项面板”功能，默认已启用。
        // 8.FEATURE_PROGRESS：进度指示器功能
        // 9.FEATURE_RIGHT_ICON:标题栏右侧的图标

    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);

         setContentView(R.layout.gl_activity);

        SMPermissionsHelper.checkPermission(this);
        mGLSurfaceView = (AlbumKitView) findViewById(R.id.gpuimage);


        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        //获得当前设备的配置信息 ,进而获取openGL es的版本信息
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        if (info.reqGlEsVersion >= 0x20000) {
            mGLSurfaceView.setEGLContextClientVersion(2);
            //设置使用的Render为RuleTransitionGLES2Renderer
            mGLRender = new AlbumKitRender(this);
            mGLSurfaceView.setRenderer(mGLRender);
           // mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            mAlbumKit = new AlbumKit(mGLSurfaceView,mGLRender);

        } else {
        	Toast.makeText(this, 
        		"OpenGL ES 2.0 not supported on device.  Exiting...",
        		Toast.LENGTH_SHORT).show();
        	finish();
        }


            this.findViewById(R.id.button_choose_photo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, REQUEST_PICK_IMAGE);
                }
            });


            this.findViewById(R.id.button_start_watch).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAlbumKit.watchAlbum();

                }
            });
	}

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

}
